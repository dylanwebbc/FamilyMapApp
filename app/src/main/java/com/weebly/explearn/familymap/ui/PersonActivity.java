package com.weebly.explearn.familymap.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.weebly.explearn.familymap.R;
import com.weebly.explearn.familymap.model.DataCache;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dbModels.Event;
import dbModels.Person;

/**
 * An activity which displays details about a person's life events and family members
 * Can transfer to: event activity, person activity (new)
 */
public class PersonActivity extends AppCompatActivity {

    public static String PERSON_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // retrieve correlated person and set textviews accordingly
        Person person = DataCache.getInstance().getPeople().get(getIntent().
                getStringExtra(PERSON_ID));
        TextView firstName = findViewById(R.id.personFirstNameTextView);
        firstName.setText(person.getFirstName());
        TextView lastName = findViewById(R.id.personLastNameTextView);
        lastName.setText(person.getLastName());
        TextView gender = findViewById(R.id.personGenderTextView);
        if (person.getGender().equals(getString(R.string.gender_f))) {
            gender.setText(R.string.female);
        }
        else if (person.getGender().equals(getString(R.string.gender_m))){
            gender.setText(R.string.male);
        }

        // get the necessary family and life event information
        ArrayList<Person> relevantPeople = DataCache.getInstance().
                getFamily(person.getPersonID());
        ArrayList<Event> relevantEvents = DataCache.getInstance().
                getLifeEvents(person.getPersonID());
        ExpandableListView expandableListView = findViewById(R.id.expandableListView);
        expandableListView.setAdapter(new ExpandableListAdapter(relevantPeople, relevantEvents));
    }

    private class ExpandableListAdapter extends BaseExpandableListAdapter {

        private static final int PEOPLE_GROUP_POSITION = 1;
        private static final int EVENTS_GROUP_POSITION = 0;

        private final ArrayList<Person> people;
        private final ArrayList<Event> events;

        ExpandableListAdapter(ArrayList<Person> people, ArrayList<Event> events) {
            this.people = people;
            this.events = events;
        }

        @Override
        public int getGroupCount() {
            return 2;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            switch (groupPosition) {
                case PEOPLE_GROUP_POSITION:
                    return people.size();
                case EVENTS_GROUP_POSITION:
                    return events.size();
                default:
                    throw new IllegalArgumentException("Unrecognized group pos: " + groupPosition);
            }
        }

        @Override
        public Object getGroup(int groupPosition) {
            switch (groupPosition) {
                case PEOPLE_GROUP_POSITION:
                    return getString(R.string.person_activity_people_title);
                case EVENTS_GROUP_POSITION:
                    return getString(R.string.person_activity_events_title);
                default:
                    throw new IllegalArgumentException("Unrecognized group pos: " + groupPosition);
            }
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            switch (groupPosition) {
                case PEOPLE_GROUP_POSITION:
                    return people.get(childPosition);
                case EVENTS_GROUP_POSITION:
                    return events.get(childPosition);
                default:
                    throw new IllegalArgumentException("Unrecognized group pos: " + groupPosition);
            }
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                                 ViewGroup parent) {
            if(convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_item_group, parent, false);
            }

            TextView titleView = convertView.findViewById(R.id.listTitle);

            switch (groupPosition) {
                case PEOPLE_GROUP_POSITION:
                    titleView.setText(R.string.person_activity_people_title);
                    break;
                case EVENTS_GROUP_POSITION:
                    titleView.setText(R.string.person_activity_events_title);
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognized group pos: " + groupPosition);
            }

            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                                 View convertView, ViewGroup parent) {
            View itemView;

            switch(groupPosition) {
                case PEOPLE_GROUP_POSITION:
                    itemView = getLayoutInflater().inflate(R.layout.person_item, parent, false);
                    initializePersonView(itemView, childPosition);
                    break;
                case EVENTS_GROUP_POSITION:
                    itemView = getLayoutInflater().inflate(R.layout.event_item, parent, false);
                    initializeEventView(itemView, childPosition);
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognized group pos: " + groupPosition);
            }

            return itemView;
        }

        private void initializePersonView(View personItemView, final int childPosition) {
            Person relative = people.get(childPosition);

            // set corresponding icon for relative's gender
            Drawable genderIcon;
            if (relative.getGender().equals(getString(R.string.gender_f))) {
                genderIcon = new IconDrawable(PersonActivity.this,
                        FontAwesomeIcons.fa_female).colorRes(R.color.female_icon).sizeDp(40);
            }
            else if (relative.getGender().equals(getString(R.string.gender_m))){
                genderIcon = new IconDrawable(PersonActivity.this,
                        FontAwesomeIcons.fa_male).colorRes(R.color.male_icon).sizeDp(40);
            }
            else {
                genderIcon = new IconDrawable(PersonActivity.this,
                        FontAwesomeIcons.fa_android).colorRes(R.color.gray).sizeDp(40);
            }
            ImageView icon = personItemView.findViewById(R.id.personImageView);
            icon.setImageDrawable(genderIcon);

            // set relative's full name
            String fullName = DataCache.getInstance().getPersonFullName(relative.getPersonID());
            TextView name = personItemView.findViewById(R.id.personNameTextView);
            name.setText(fullName);

            // set relative's relationship to the main person
            TextView details = personItemView.findViewById(R.id.personDetailsTextView);
            details.setText(getRelationship(relative));

            personItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //create a new person activity
                    Intent intent = new Intent(PersonActivity.this, PersonActivity.class);
                    intent.putExtra(PersonActivity.PERSON_ID, relative.getPersonID());
                    startActivity(intent);
                }
            });
        }

        private void initializeEventView(View eventItemView, final int childPosition) {
            Event event = events.get(childPosition);

            // set corresponding color for event marker
            String colorName = DataCache.getInstance().getCorrespondingColor(event.getEventType());
            int color = getResources().getIdentifier(colorName, "color", getPackageName());
            Drawable markerIcon = new IconDrawable(PersonActivity.this,
                    FontAwesomeIcons.fa_map_marker).colorRes(color).sizeDp(40);
            ImageView icon = eventItemView.findViewById(R.id.eventImageView);
            icon.setImageDrawable(markerIcon);

            // set corresponding name for the event
            String fullName = DataCache.getInstance().getPersonFullName(event.getPersonID());
            TextView name = eventItemView.findViewById(R.id.eventNameTextView);
            name.setText(fullName);

            // set event details
            String eventDetails = DataCache.getInstance().getEventDetails(event.getEventID());
            TextView details = eventItemView.findViewById(R.id.eventDetailsTextView);
            details.setText(eventDetails);

            eventItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //create a new event activity
                    Intent intent = new Intent(PersonActivity.this, EventActivity.class);
                    intent.putExtra(EventActivity.EVENT_ID, event.getEventID());
                    startActivity(intent);
                }
            });
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        private String getRelationship(Person relative) {
            String relationship = "";
            String mainPersonID = getIntent().getStringExtra(PERSON_ID);
            if (relative.getSpouseID() != null) {
                if (relative.getSpouseID().equals(mainPersonID)) {
                    relationship = getString(R.string.spouse);
                }
            }
            if (relative.getMotherID() != null) {
                if (relative.getMotherID().equals(mainPersonID)) {
                    relationship = getString(R.string.child);
                }
            }
            if (relative.getFatherID() != null) {
                if (relative.getFatherID().equals(mainPersonID)) {
                    relationship = getString(R.string.child);
                }
            }
            if (relationship.isEmpty()) {
                if (relative.getGender().equals(getString(R.string.gender_f))) {
                    relationship = getString(R.string.mother);
                }
                else if (relative.getGender().equals(getString(R.string.gender_m))) {
                    relationship = getString(R.string.father);
                }
            }
            return relationship;
        }
    }
}