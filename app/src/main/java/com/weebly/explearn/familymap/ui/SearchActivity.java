package com.weebly.explearn.familymap.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.weebly.explearn.familymap.R;
import com.weebly.explearn.familymap.model.DataCache;

import java.util.ArrayList;
import java.util.Map;

import dbModels.Event;
import dbModels.Person;

/**
 * An activity which allows the user to query all people and filtered events and displays results
 * Can transfer to: event activity, person activity
 */
public class SearchActivity extends AppCompatActivity {

    private static final int PERSON_ITEM_VIEW_TYPE = 0;
    private static final int EVENT_ITEM_VIEW_TYPE = 1;

    RecyclerView recyclerView;
    FamilyTreeAdapter adapter;
    private EditText searchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Required empty function call
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.updateList(searchBar.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Required empty function call
            }
        };

        searchBar = (EditText) findViewById(R.id.searchEditText);
        searchBar.addTextChangedListener(textWatcher);

        ImageView searchIcon = (ImageView) findViewById(R.id.searchImageView);
        searchIcon.setImageDrawable(new IconDrawable(this, FontAwesomeIcons.fa_search).
                colorRes(R.color.black).actionBarSize());

        recyclerView = findViewById(R.id.RecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(SearchActivity.this));

        adapter = new FamilyTreeAdapter();
        recyclerView.setAdapter(adapter);
    }

    private class FamilyTreeAdapter extends RecyclerView.Adapter<FamilyTreeViewHolder> {
        private ArrayList<Person> people;
        private ArrayList<Event> events;

        FamilyTreeAdapter() {
            this.people = new ArrayList<>();
            this.events = new ArrayList<>();
            updateList("");
        }

        @Override
        public int getItemViewType(int position) {
            return position < people.size() ? PERSON_ITEM_VIEW_TYPE : EVENT_ITEM_VIEW_TYPE;
        }

        @NonNull
        @Override
        public FamilyTreeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;

            if(viewType == PERSON_ITEM_VIEW_TYPE) {
                view = getLayoutInflater().inflate(R.layout.person_item, parent, false);
            } else {
                view = getLayoutInflater().inflate(R.layout.event_item, parent, false);
            }

            return new FamilyTreeViewHolder(view, viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull FamilyTreeViewHolder holder, int position) {
            if(position < people.size()) {
                holder.bind(people.get(position));
            } else {
                holder.bind(events.get(position - people.size()));
            }
        }

        @Override
        public int getItemCount() {
            return people.size() + events.size();
        }

        private void updateList(String search) {
            recyclerView.removeAllViews();
            recyclerView.refreshDrawableState();
            recyclerView.scrollToPosition(0);
            people.clear();
            events.clear();
            people = DataCache.getInstance().getSearchedPeople(search);
            events = DataCache.getInstance().getSearchedEvents(search);
        }
    }

    private class FamilyTreeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final int viewType;
        private final ImageView icon;
        private final TextView name;
        private final TextView details;

        private Person person;
        private Event event;

        FamilyTreeViewHolder(View view, int viewType) {
            super(view);
            this.viewType = viewType;

            itemView.setOnClickListener(this);

            if(viewType == PERSON_ITEM_VIEW_TYPE) {
                icon = itemView.findViewById(R.id.personImageView);
                name = itemView.findViewById(R.id.personNameTextView);
                details = null;
            } else {
                icon = itemView.findViewById(R.id.eventImageView);
                name = itemView.findViewById(R.id.eventNameTextView);
                details = itemView.findViewById(R.id.eventDetailsTextView);
            }
        }

        private void bind(Person person) {
            this.person = person;

            // set corresponding icon for person's gender
            Drawable genderIcon;
            if (person.getGender().equals(getString(R.string.gender_f))) {
                genderIcon = new IconDrawable(SearchActivity.this,
                        FontAwesomeIcons.fa_female).colorRes(R.color.female_icon).sizeDp(40);
            }
            else if (person.getGender().equals(getString(R.string.gender_m))) {
                genderIcon = new IconDrawable(SearchActivity.this,
                        FontAwesomeIcons.fa_male).colorRes(R.color.male_icon).sizeDp(40);
            }
            else {
                genderIcon = new IconDrawable(SearchActivity.this,
                        FontAwesomeIcons.fa_android).colorRes(R.color.gray).sizeDp(40);
            }
            icon.setImageDrawable(genderIcon);

            // set person's full name
            String fullName = DataCache.getInstance().getPersonFullName(person.getPersonID());
            name.setText(fullName);
        }

        private void bind(Event event) {
            this.event = event;

            // set corresponding color for event marker
            String colorName = DataCache.getInstance().getCorrespondingColor(event.getEventType());
            int color = getResources().getIdentifier(colorName, "color", getPackageName());
            Drawable markerIcon = new IconDrawable(SearchActivity.this,
                    FontAwesomeIcons.fa_map_marker).colorRes(color).sizeDp(40);
            icon.setImageDrawable(markerIcon);

            // set corresponding name and details for the event
            name.setText(DataCache.getInstance().getPersonFullName(event.getPersonID()));
            details.setText(DataCache.getInstance().getEventDetails(event.getEventID()));
        }

        @Override
        public void onClick(View view) {
            Intent intent;
            if (viewType == PERSON_ITEM_VIEW_TYPE) {
                //create a new person activity
                intent = new Intent(SearchActivity.this, PersonActivity.class);
                intent.putExtra(PersonActivity.PERSON_ID, person.getPersonID());
            } else {
                //create a new event activity
                intent = new Intent(SearchActivity.this, EventActivity.class);
                intent.putExtra(EventActivity.EVENT_ID, event.getEventID());
            }
            startActivity(intent);
        }
    }
}