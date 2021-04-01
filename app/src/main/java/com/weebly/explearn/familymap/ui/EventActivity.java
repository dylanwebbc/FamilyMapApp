package com.weebly.explearn.familymap.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;

import com.weebly.explearn.familymap.R;

/**
 * An activity which holds a new map fragment centered on a single event
 * Can transfer to: person activity
 */
public class EventActivity extends AppCompatActivity {

    public static String EVENT_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FragmentManager fm = this.getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) fm.findFragmentById(R.id.mapRelativeLayout);
        if (mapFragment == null) {
            mapFragment = new MapFragment();
            Bundle args = new Bundle();
            args.putString(EVENT_ID, getIntent().getStringExtra(EVENT_ID));
            mapFragment.setArguments(args);
            fm.beginTransaction().add(R.id.eventFragmentContainer, mapFragment).commit();
        }
    }
}