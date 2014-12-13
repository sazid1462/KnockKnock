package com.shakeme.sazedul.knockknock;

/**
 * This Class is the activity class for getting user input of adding a geofence
 *
 * Created by Sazedul on 5-Dec-14.
 */

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;


public class AddGeofenceActivity extends Activity
        implements AdapterView.OnItemSelectedListener,
        EditText.OnFocusChangeListener{

    // Google Places
    GooglePlaces googlePlaces;

    // Places List
    PlaceList nearPlaces;

    // Select a place
    Place place;

    // NetworkConnectivityDetector class' object
    NetworkConnectivityDetector nCDetector;

    // Details of a place data
    PlaceDetails placeDetails;

    // For the reference of UI elements
    private EditText txtName;
    private EditText txtLatitude;
    private EditText txtLongitude;
    private EditText txtRadius;
    private EditText txtExpirationDuration;
    private Spinner spinnerTypes;
    private TextView txtAddress;
    private Intent intent;

    // Type of the geofence according to the selection of types from the spinnerTypes
    private int mType;

    /**
     * Checks if the Latitude value is a valid Latitude or not
     *
     * @param value The value to be checked
     * @return true/false
     */
    private boolean isValidLatitudeValue(String value) {
        double lat;
        if (value.matches("")) lat = GeofenceUtils.INVALID_FLOAT_VALUE;
        else lat = Double.parseDouble(value);
        return lat >= GeofenceUtils.MIN_LATITUDE && lat <= GeofenceUtils.MAX_LATITUDE;
    }

    /**
     * Checks if the Longitude value is a valid Longitude or not
     *
     * @param value The value to be checked
     * @return true/false
     */
    private boolean isValidLongitudeValue(String value) {
        double lng;
        if (value.matches("")) lng = GeofenceUtils.INVALID_FLOAT_VALUE;
        else lng = Double.parseDouble(value);
        return lng >= GeofenceUtils.MIN_LONGITUDE && lng <= GeofenceUtils.MAX_LONGITUDE;
    }

    /**
     * Checks if the Radius value is a valid Radius or not
     *
     * @param value The value to be checked
     * @return true/false
     */
    private boolean isValidRadius(String value) {
        Float rad;
        if (value.matches("")) rad = GeofenceUtils.INVALID_FLOAT_VALUE;
        else rad = Float.parseFloat(value);
        return rad >= GeofenceUtils.MIN_RADIUS;
    }

    // This function is called when the activity is first created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_geofence);

        // Instantiate the NetworkConnectivityDetector class' object
        nCDetector = new NetworkConnectivityDetector(this);
        // Get the latitude and longitude from the intent
        intent = getIntent();
        txtName = (EditText) findViewById(R.id.txt_name);
        txtLatitude = (EditText) findViewById(R.id.txt_latitude);
        txtLongitude = (EditText) findViewById(R.id.txt_longitude);
        txtRadius = (EditText) findViewById(R.id.txt_radius);
        txtExpirationDuration = (EditText) findViewById(R.id.txt_expiration);
        spinnerTypes = (Spinner) findViewById(R.id.spinner_type);
        txtAddress = (TextView) findViewById(R.id.txt_place_address);

        // The default value of type (Enter);
        mType = Geofence.GEOFENCE_TRANSITION_ENTER;

        // Adapter for the spinnerTypes
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.geofence_type_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerTypes.setAdapter(adapter);
        spinnerTypes.setOnItemSelectedListener(this);

        // Set the listeners for the EditText views
        txtName.setOnFocusChangeListener(this);
        txtLatitude.setOnFocusChangeListener(this);
        txtLongitude.setOnFocusChangeListener(this);
        txtRadius.setOnFocusChangeListener(this);
        txtExpirationDuration.setOnFocusChangeListener(this);

        /*
         * Check the intent's extra to know whether it is created by clicking the Add New Reminder
         * button or by clicking on the map. If it is created from clicking on the map then update the
         * Latitude and Longitude value in the text fields
         */

        if (intent.hasExtra(GeofenceUtils.PREFIX+".latlng")) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    double extra[] = intent.getDoubleArrayExtra(GeofenceUtils.PREFIX+".latlng");

                    txtLatitude.setText(Double.toString(extra[0]));
                    txtLongitude.setText(Double.toString(extra[1]));
                }
            });
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_geofence, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (position){
            case 0: // Enter is selected
                mType = Geofence.GEOFENCE_TRANSITION_ENTER;
                break;
            case 1: // Exit is selected
                mType = Geofence.GEOFENCE_TRANSITION_EXIT;
                break;
            default: // Both is selected
                mType = Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // If nothing is selected set the Both value to the mType
        mType = Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        // If Latitude and Longitude values are correct then try to get the approximate location
        // from Google Places API
        if (isValidLatitudeValue(txtLatitude.getText().toString()) &&
                isValidLongitudeValue(txtLongitude.getText().toString())) {
            if (nCDetector.isConnectionToInternetAvailable()) {
                // Run the thread
                new LoadPlaces().execute();
            }
        }
        else runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtAddress.setText("Invalid latitude or longitude!");
            }
        });
    }

    /**
     * This Async Task will try to find some places near your place using Google Places API
     */
    class LoadPlaces extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        /**
         * getting Places JSON
         * */
        protected String doInBackground(String... args) {
            // creating Places class object
            googlePlaces = new GooglePlaces();

            try {
                // Separate your place types by PIPE symbol "|"
                // If you want all types places make it as null
                // Check list of types supported by google
                String types = null; // Listing all detected types of places

                // Radius in meters - increase this value if you don't find any places
                double radius = 100; // 100 meters
                nearPlaces = googlePlaces.search(Double.parseDouble(txtLatitude.getText().toString()),
                        Double.parseDouble(txtLongitude.getText().toString()), radius, types);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * After completing background task of getting places near your place try to get the details
         * from one of the places you get and show the data in UI
         * Always use runOnUiThread(new Runnable()) to update UI from background
         * thread, otherwise you may get error
         **/
        protected void onPostExecute(String file_url) {
            // Get json response status
            String status = nearPlaces.status;

            // Check for status
            if(status.equals("OK")){
                // Successfully got places details
                if (nearPlaces.results != null) {
                    // select a place
                    place = nearPlaces.results.get(0);
                    new LoadPlaceDetails().execute(place.reference);
                    //mCurrentLocation.setText(place.id);
                }
            }
        }
    }

    /**
     * This Async Task will try to find the details of one of the places nearby
     */
    class LoadPlaceDetails extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        /**
         * getting Profile JSON
         * */
        protected String doInBackground(String... args) {
            String reference = args[0];

            try {
                placeDetails = googlePlaces.getPlaceDetails(reference);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * After completing background task show the data in UI
         * Always use runOnUiThread(new Runnable()) to update UI from background
         * thread, otherwise you might get error
         * **/
        protected void onPostExecute(String file_url) {
            // remove the progress bar after getting all products
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    // Get json response status
                    if (placeDetails != null) {
                        String status = placeDetails.status;

                        if (status.equals("OK")) {
                            // Successfully got places details
                            if (placeDetails.result != null) {

                                String address = placeDetails.result.formatted_address;
                                address = address == null ? "Unknown!" : address;
                                txtAddress.setText(address);
                            }
                        } else {
                            txtAddress.setText("Could not find details.");
                        }
                    }
                }
            });

        }

    }

    /**
     * To show the Help Activity
     * @param view is the object by which it is invoked
     */
    public void showHelpActivity(View view){
        Intent intentHelp = new Intent(this, DetailsHelpActivity.class);
        startActivity(intentHelp);
    }

    /**
     * To finish the activity
     * @param view is the object by which it is invoked
     */
    public void finishTheActivity(View view) {
        finish();
    }

    /**
     * Start adding geofences by giving the UI data to the MapsActivity as Result
     * @param view is the object by which it is invoked
     */
    public void addReminder(View view) {
        // The intent which is gonna passed to the activity who called this activity for results
        // In this case it is MapsActivity
        Intent intentReminder = new Intent();

        if (isValidLatitudeValue(txtLatitude.getText().toString())
                && isValidLongitudeValue(txtLongitude.getText().toString())
                && isValidRadius(txtRadius.getText().toString())) {
            intentReminder.putExtra("GeofenceData", "Data successfully acquired.");
            intentReminder.putExtra("GeofenceName", txtName.getText().toString());
            intentReminder.putExtra("GeofenceLat", Double.parseDouble(txtLatitude.getText().toString()));
            intentReminder.putExtra("GeofenceLng", Double.parseDouble(txtLongitude.getText().toString()));
            intentReminder.putExtra("GeofenceRad", Float.parseFloat(txtRadius.getText().toString()));
            intentReminder.putExtra("GeofenceExp", Long.parseLong(txtExpirationDuration.getText().toString())
                    * GeofenceUtils.SECOND_PER_HOUR * GeofenceUtils.MILLISECONDS_PER_SECOND);
            intentReminder.putExtra("GeofenceType", mType);
            // Set the intent as result
            setResult(RESULT_OK, intentReminder);
            // Finish the activity
            finish();
        } else {
            Toast.makeText(this, "Check the inputs! There are errors!", Toast.LENGTH_LONG).show();
        }
    }
}
