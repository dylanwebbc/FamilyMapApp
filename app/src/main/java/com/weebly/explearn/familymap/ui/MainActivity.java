package com.weebly.explearn.familymap.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.weebly.explearn.familymap.R;

import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;
import com.weebly.explearn.familymap.model.DataCache;

/**
 * The main activity which holds a login fragment or map fragment
 * depending on whether or not the user is logged in
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Iconify.with(new FontAwesomeModule());
        setContentView(R.layout.activity_main);

        FragmentManager fm = this.getSupportFragmentManager();
        if (DataCache.getInstance().isLoggedIn()) {
            // loads the map fragment if the user is logged in
            MapFragment mapFragment = (MapFragment) fm.findFragmentById(R.id.mapRelativeLayout);
            if (mapFragment == null) {
                mapFragment = new MapFragment();
                Bundle args = new Bundle();
                args.putString(MapFragment.EVENT_ID, null);
                mapFragment.setArguments(args);
                fm.beginTransaction().add(R.id.mainFragmentContainer, mapFragment).commit();
            }
        }
        else {
            // loads the login fragment if user is logged out
            LoginFragment loginFragment = (LoginFragment) fm.findFragmentById(R.id.loginLinearLayout);
            if (loginFragment == null) {
                loginFragment = new LoginFragment();
                fm.beginTransaction().add(R.id.mainFragmentContainer, loginFragment).commit();
            }
        }
    }
}