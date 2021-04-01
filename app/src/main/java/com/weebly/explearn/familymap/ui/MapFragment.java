package com.weebly.explearn.familymap.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.*;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.weebly.explearn.familymap.R;
import com.weebly.explearn.familymap.model.DataCache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import dbModels.Event;
import dbModels.Person;

/**
 * A fragment containing an interactive google map for the user's family tree
 * Can transfer to: login fragment, person activity, search activity, settings activity
 */
public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback {
    private GoogleMap map;
    public static String EVENT_ID;
    String bottomEventID;
    private LinearLayout bottomBar;
    private TextView bottomText;
    private ImageView bottomIcon;
    private HashMap<Polyline, String> lines;

    public MapFragment() {
        // Required empty public constructor
    }

    public static MapFragment newInstance(String username) {
        MapFragment fragment = new MapFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataCache.getInstance().login();
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        bottomEventID = "";
        lines = new HashMap<>();

        bottomBar = (LinearLayout) view.findViewById(R.id.mapBottomBar);
        bottomBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBottomBarClicked();
            }
        });

        bottomText = (TextView) view.findViewById(R.id.mapTextView);
        bottomIcon = (ImageView) view.findViewById(R.id.mapImageView);
        setBottomIcon("");

        SupportMapFragment googleMapFragment = (SupportMapFragment) getChildFragmentManager().
                findFragmentById(R.id.map);
        googleMapFragment.getMapAsync(this);
        return view;
    }

    @Override
    public void onResume() {
        DataCache dc = DataCache.getInstance();
        super.onResume();

        // loads correct events and lines if the user is logged in
        if (DataCache.getInstance().isLoggedIn()) {
            if (map != null) {
                if (!dc.isUpdatedEvents()) { //update events
                    map.clear();
                    onMapReady(map);
                    bottomText.setText(R.string.map_fragment_message);
                    setBottomIcon("");
                }
                else if (!dc.isUpdatedLines()) { //update polylines
                    setEventInfo(bottomEventID);
                }
                dc.confirmUpdate();
            }
        }
        // switches to the login fragment if the user is logged out
        else {
            FragmentManager fm = getFragmentManager();
            LoginFragment loginFragment = (LoginFragment) fm.findFragmentById(R.id.loginLinearLayout);
            if (loginFragment == null) {
                loginFragment = new LoginFragment();
                fm.beginTransaction().replace(R.id.mainFragmentContainer, loginFragment).commit();
            }
        }

    }
    
    private void onBottomBarClicked() {
        if (!bottomEventID.isEmpty()) {
            String personID = DataCache.getInstance().getEvents().get(bottomEventID).getPersonID();
            Intent intent = new Intent(getActivity(), PersonActivity.class);
            intent.putExtra(PersonActivity.PERSON_ID, personID);
            startActivity(intent);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (getArguments().getString(EVENT_ID) == null) {
            inflater.inflate(R.menu.menu, menu);
            super.onCreateOptionsMenu(menu, inflater);
            menu.findItem(R.id.menuSearch).setIcon(new IconDrawable(getActivity(),
                    FontAwesomeIcons.fa_search).colorRes(R.color.white).actionBarSize());
            menu.findItem(R.id.menuSettings).setIcon(new IconDrawable(getActivity(),
                    FontAwesomeIcons.fa_gear).colorRes(R.color.white).actionBarSize());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menuSearch) {
            startActivity(new Intent(getActivity(), SearchActivity.class));
            return true;
        }
        else if (item.getItemId() == R.id.menuSettings) {
            startActivity(new Intent(getActivity(), SettingsActivity.class));
            return true;
        }
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setOnMapLoadedCallback(this);

        Map<String, Event> events = DataCache.getInstance().getEvents();

        //move camera to current event if in an event activity
        if (getArguments().getString(EVENT_ID) != null) {
            Event event = events.get(getArguments().getString(EVENT_ID));
            float zoomLevel = 4.0f;
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(event.getLatitude(),
                    event.getLongitude()), zoomLevel));
        }

        // Add markers for every event in the user's family tree
        for (Event event : events.values()) {
            String colorName = DataCache.getInstance().getCorrespondingColor(event.getEventType());
            int color = getContext().getResources().getIdentifier(colorName, "color",
                    getContext().getPackageName());
            Drawable markerIcon = new IconDrawable(getActivity(), FontAwesomeIcons.fa_map_marker).
                    colorRes(color).sizeDp(50);

            Marker marker = map.addMarker(new MarkerOptions().position(
                    new LatLng(event.getLatitude(), event.getLongitude())).
                    icon(getBitmapFromDrawable(markerIcon)));
            marker.setTag(event);
            map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    String eventID = ((Event) marker.getTag()).getEventID();
                    setEventInfo(eventID);
                    return false;
                }
            });
        }

        //get the event to display first if an event activity
        if (getArguments().getString(EVENT_ID) != null) {
            setEventInfo(getArguments().getString(EVENT_ID));
        }
    }

    @Override
    public void onMapLoaded() {
        // Required empty function call
    }

    private void setEventInfo(String eventID) {
        bottomEventID = eventID;
        int defaultWidth = 13;
        DataCache dc = DataCache.getInstance();
        Event event = dc.getEvents().get(eventID);
        Person person = dc.getPeople().get(event.getPersonID());

        //match bottom bar to the new event
        String gender = person.getGender();
        setBottomIcon(gender);
        String text = dc.getPersonFullName(event.getPersonID()) + "\n" +
                dc.getEventDetails(eventID);
        bottomText.setText(text);

        //remove old polylines
        for (Polyline line : lines.keySet()) {
            line.remove();
        }
        lines.clear();

        //add new polylines connecting important events
        ArrayList<String> lineFilters = dc.getLineFilters();
        if (lineFilters.contains(getString(R.string.life_story_line_filter))) {
            ArrayList<Event> lifeEvents = dc.getLifeEvents(person.getPersonID());
            for (int i = 0; i < lifeEvents.size(); i++) {
                if (i < lifeEvents.size() - 1) {
                    addLine(lifeEvents.get(i), lifeEvents.get(i + 1),
                            getString(R.string.life_story_line_filter), defaultWidth);
                }
            }
        }
        if (lineFilters.contains(getString(R.string.family_tree_line_filter))) {
            traverseAncestors(event, person.getPersonID(), defaultWidth);
        }
        if (lineFilters.contains(getString(R.string.spouse_line_filter))
                && person.getSpouseID() != null) {
            if (!dc.getLifeEvents(person.getSpouseID()).isEmpty()) {
                Event spouseEvent = dc.getLifeEvents(person.getSpouseID()).get(0);
                addLine(event, spouseEvent, getString(R.string.spouse_line_filter), defaultWidth);
            }
        }
    }

    private void addLine(Event currEvent, Event connEvent, String type, int width) {
        LatLng currPos = new LatLng(currEvent.getLatitude(), currEvent.getLongitude());
        LatLng connPos = new LatLng(connEvent.getLatitude(), connEvent.getLongitude());

        String colorName = DataCache.getInstance().getCorrespondingColor(type);
        int color = getContext().getResources().getIdentifier(colorName, "color",
                getContext().getPackageName());

        Polyline line = map.addPolyline(new PolylineOptions().add(currPos,
                connPos).width(width).color(ContextCompat.getColor(getContext(), color)));
        lines.put(line, type);
    }

    private void traverseAncestors(Event currEvent, String currPersonID, int width) {
        String type = getString(R.string.family_tree_line_filter);
        DataCache dc = DataCache.getInstance();
        Person currPerson = dc.getPeople().get(currPersonID);

        if (currPerson.getMotherID() != null) {
            ArrayList<Event> motherEvents = dc.getLifeEvents(currPerson.getMotherID());
            if (!motherEvents.isEmpty()) {
                addLine(currEvent, motherEvents.get(0), type, width);
                traverseAncestors(motherEvents.get(0), currPerson.getMotherID(), width - 3);
            }
        }
        if (currPerson.getFatherID() != null) {
            ArrayList<Event> fatherEvents = dc.getLifeEvents(currPerson.getFatherID());
            if (!fatherEvents.isEmpty()) {
                addLine(currEvent, fatherEvents.get(0), type, width);
                traverseAncestors(fatherEvents.get(0), currPerson.getFatherID(), width - 3);
            }
        }
    }

    private BitmapDescriptor getBitmapFromDrawable(Drawable drawable) {
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void setBottomIcon(String gender) {
        FontAwesomeIcons icon;
        int color;
        if (gender.equals(getString(R.string.gender_f))) {
            icon = FontAwesomeIcons.fa_female;
            color = R.color.female_icon;
        }
        else if (gender.equals(getString(R.string.gender_m))){
            icon = FontAwesomeIcons.fa_male;
            color = R.color.male_icon;
        }
        else {
            icon = FontAwesomeIcons.fa_android;
            color = R.color.gray;
        }
        Drawable genderIcon = new IconDrawable(getActivity(), icon).colorRes(color).sizeDp(40);
        bottomIcon.setImageDrawable(genderIcon);
    }
}