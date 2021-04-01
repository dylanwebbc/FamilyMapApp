package com.weebly.explearn.familymap.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import requests.*;
import dbModels.Person;

import com.weebly.explearn.familymap.R;
import com.weebly.explearn.familymap.net.DataAsyncTask;
import com.weebly.explearn.familymap.net.LoginAsyncTask;
import com.weebly.explearn.familymap.net.RegisterAsyncTask;
import com.weebly.explearn.familymap.model.DataCache;

import java.util.ArrayList;

/**
 * A fragment which contains edit text fields and buttons for registering and logging in
 * Can transfer to: map fragment
 */
public class LoginFragment extends Fragment {

    private static final String REGISTER_RESULT_KEY = "RegisterResultKey";
    private static final String LOGIN_RESULT_KEY = "LoginResultKey";
    private static final String DATA_RETRIEVAL_KEY = "DataRetrievalKey";

    private String gender;

    private EditText hostEditText;
    private EditText portEditText;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText emailEditText;

    private RadioGroup genderRadioGroup;
    private RadioButton maleRadioButton;
    private RadioButton femaleRadioButton;

    private Button loginButton;
    private Button registerButton;

    public LoginFragment() {
        // Required empty public constructor
    }

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //create text watcher for the edit text fields to enable and disable login and register
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Required empty function call
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String hostName = hostEditText.getText().toString();
                String portNumber = portEditText.getText().toString();
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                String email = emailEditText.getText().toString();
                String firstName = firstNameEditText.getText().toString();
                String lastName = lastNameEditText.getText().toString();

                // check if all necessary fields are filled before enabling buttons
                loginButton.setEnabled(!hostName.isEmpty() && !portNumber.isEmpty() &&
                        !username.isEmpty() && !password.isEmpty());
                registerButton.setEnabled(!hostName.isEmpty() && !portNumber.isEmpty() &&
                        !username.isEmpty() && !password.isEmpty() && !email.isEmpty() &&
                        !firstName.isEmpty() && !lastName.isEmpty() && gender != null);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Required empty function call
            }
        };

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        hostEditText = view.findViewById(R.id.hostEditText);
        hostEditText.addTextChangedListener(textWatcher);
        portEditText = view.findViewById(R.id.portEditText);
        portEditText.addTextChangedListener(textWatcher);
        usernameEditText = view.findViewById(R.id.usernameEditText);
        usernameEditText.addTextChangedListener(textWatcher);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        passwordEditText.addTextChangedListener(textWatcher);
        firstNameEditText = view.findViewById(R.id.firstNameEditText);
        firstNameEditText.addTextChangedListener(textWatcher);
        lastNameEditText = view.findViewById(R.id.lastNameEditText);
        lastNameEditText.addTextChangedListener(textWatcher);
        emailEditText = view.findViewById(R.id.emailEditText);
        emailEditText.addTextChangedListener(textWatcher);

        maleRadioButton = view.findViewById(R.id.maleRadioButton);
        femaleRadioButton = view.findViewById(R.id.femaleRadioButton);
        genderRadioGroup = view.findViewById(R.id.genderRadioGroup);
        genderRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                setGender();
                //pass in arbitrary values to activate text watcher
                textWatcher.onTextChanged(null, 0, 0, 0);
            }
        });

        loginButton = view.findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });

        registerButton = view.findViewById(R.id.registerButton);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register();
            }
        });

        return view;
    }

    private void register() {
        String hostName = hostEditText.getText().toString();
        String portNumber = portEditText.getText().toString();
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String firstName = firstNameEditText.getText().toString();
        String lastName = lastNameEditText.getText().toString();

        RegisterRequest registerRequest = new RegisterRequest(username, password, email,
                firstName, lastName, gender);

        //Set up a handler that will alert the UI thread when login completes
        Handler uiThreadMessageHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                Bundle bundle = message.getData();
                ArrayList<String> result = bundle.getStringArrayList(REGISTER_RESULT_KEY);
                if (result.get(0) == null) {
                    Toast.makeText(getContext(), result.get(2),
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    retrieveData(hostName, portNumber, result.get(0), result.get(1));
                }
            }
        };
        RegisterAsyncTask.Execute(uiThreadMessageHandler, hostName, portNumber,
                registerRequest);
    }

    private void login() {
        String hostName = hostEditText.getText().toString();
        String portNumber = portEditText.getText().toString();
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        LoginRequest loginRequest = new LoginRequest(username, password);

        //Set up a handler that will alert the UI thread when login completes
        Handler uiThreadMessageHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                Bundle bundle = message.getData();
                ArrayList<String> result = bundle.getStringArrayList(LOGIN_RESULT_KEY);
                if (result.get(0) == null) {
                    Toast.makeText(getContext(), result.get(2),
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    retrieveData(hostName, portNumber, result.get(0), result.get(1));
                }
            }
        };
        LoginAsyncTask.Execute(uiThreadMessageHandler, hostName, portNumber, loginRequest);
    }

    private void retrieveData(String hostName, String portNumber, String authtoken,
                              String personID) {
        //Set up a handler that will alert the UI thread when data download completes
        Handler uiThreadMessageHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                Bundle bundle = message.getData();
                String result = bundle.getString(DATA_RETRIEVAL_KEY);
                Person user = DataCache.getInstance().getPeople().get(personID);
                DataCache.getInstance().setUser(user);
                if (result == null) {
                    if (user != null) {
                        String toastMessage = DataCache.getInstance().
                                getPersonFullName(user.getPersonID());
                        Toast.makeText(getContext(), toastMessage,
                                Toast.LENGTH_SHORT).show();
                        switchToMapFragment(user.getAssociatedUsername());
                    }
                    else {
                        Toast.makeText(getContext(), "User not found",
                                Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(getContext(), result,
                            Toast.LENGTH_SHORT).show();
                }
            }
        };
        DataAsyncTask.Execute(uiThreadMessageHandler, hostName, portNumber, authtoken);
    }

    private void switchToMapFragment(String username) {
        FragmentManager fm = getFragmentManager();
        Fragment mapFragment = (MapFragment) fm.findFragmentById(R.id.mapRelativeLayout);
        if (mapFragment == null) {
            mapFragment = new MapFragment();
            Bundle args = new Bundle();
            args.putString(MapFragment.EVENT_ID, null);
            mapFragment.setArguments(args);
            fm.beginTransaction().replace(R.id.mainFragmentContainer, mapFragment).commit();
        }
    }

    private void setGender() {
        if (genderRadioGroup.getCheckedRadioButtonId() == maleRadioButton.getId()) {
            gender = getString(R.string.gender_m);
        }
        else if (genderRadioGroup.getCheckedRadioButtonId() == femaleRadioButton.getId()) {
            gender = getString(R.string.gender_f);
        }
    }
}