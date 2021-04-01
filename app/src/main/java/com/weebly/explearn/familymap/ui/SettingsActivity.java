package com.weebly.explearn.familymap.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import com.weebly.explearn.familymap.R;
import com.weebly.explearn.familymap.model.DataCache;

import java.util.ArrayList;

/**
 * An activity containing a field to logout and several switches to control event and line filters
 * Can transfer to: map fragment (which immediately transfers to login fragment on logout)
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        DataCache dc = DataCache.getInstance();
        ArrayList<String> eventFilters = dc.getEventFilters();
        ArrayList<String> lineFilters = dc.getLineFilters();

        SwitchCompat lifeStoryLinesSwitch = findViewById(R.id.lifeStoryLinesSwitch);
        if (lineFilters.contains(getString(R.string.life_story_line_filter))) {
            lifeStoryLinesSwitch.setChecked(true);
        }
        lifeStoryLinesSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                dc.changeLineFilters(getString(R.string.life_story_line_filter), isChecked);
            }
        });

        SwitchCompat familyTreeLinesSwitch = findViewById(R.id.familyTreeLinesSwitch);
        if (lineFilters.contains(getString(R.string.family_tree_line_filter))) {
            familyTreeLinesSwitch.setChecked(true);
        }
        familyTreeLinesSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                dc.changeLineFilters(getString(R.string.family_tree_line_filter), isChecked);
            }
        });

        SwitchCompat spouseLinesSwitch = findViewById(R.id.spouseLinesSwitch);
        if (lineFilters.contains(getString(R.string.spouse_line_filter))) {
            spouseLinesSwitch.setChecked(true);
        }
        spouseLinesSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                dc.changeLineFilters(getString(R.string.spouse_line_filter), isChecked);
            }
        });

        SwitchCompat fatherSideSwitch = findViewById(R.id.fatherSideSwitch);
        if (eventFilters.contains(getString(R.string.father_event_filter))) {
            fatherSideSwitch.setChecked(true);
        }
        fatherSideSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                dc.changeEventFilters(getString(R.string.father_event_filter), isChecked);
            }
        });

        SwitchCompat motherSideSwitch = findViewById(R.id.motherSideSwitch);
        if (eventFilters.contains(getString(R.string.mother_event_filter))) {
            motherSideSwitch.setChecked(true);
        }
        motherSideSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                dc.changeEventFilters(getString(R.string.mother_event_filter), isChecked);
            }
        });

        SwitchCompat maleEventsSwitch = findViewById(R.id.maleEventsSwitch);
        if (eventFilters.contains(getString(R.string.male_event_filter))) {
            maleEventsSwitch.setChecked(true);
        }
        maleEventsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                dc.changeEventFilters(getString(R.string.male_event_filter), isChecked);
            }
        });

        SwitchCompat femaleEventsSwitch = findViewById(R.id.femaleEventsSwitch);
        if (eventFilters.contains(getString(R.string.female_event_filter))) {
            femaleEventsSwitch.setChecked(true);
        }
        femaleEventsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                dc.changeEventFilters(getString(R.string.female_event_filter), isChecked);
            }
        });

        LinearLayout logoutLinearLayout = findViewById(R.id.logoutLinearLayout);
        logoutLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dc.logout();
                finish();
            }
        });
    }
}