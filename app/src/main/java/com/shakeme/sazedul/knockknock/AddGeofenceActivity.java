package com.shakeme.sazedul.knockknock;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.location.Geofence;

import static java.lang.System.exit;


public class AddGeofenceActivity extends ActionBarActivity
        implements AdapterView.OnItemSelectedListener,
        EditText.OnFocusChangeListener{

    // Alert Dialog Manager
    MessageDialogueViewer alert = new MessageDialogueViewer();

    // Google Places
    GooglePlaces googlePlaces;

    // Places List
    PlaceList nearPlaces;

    // Select a place
    Place place;

    // Details of a place data
    PlaceDetails placeDetails;

    // Extra Message prefix
    public static final String PREFIX = "com.shakeme.sazedul.knockknock";

    // To set the invalid latitude or longitude value if the text fields contain invalid data
    private static final float INVALID_FLOAT_VALUE = -999f;

    /*
     * Use to set an expiration time for a geofence. After this amount of time Location Services
     * will stop tracking the geofence
     */
    private static final long SECOND_PER_HOUR = 60;
    private static final long MILLISECONDS_PER_SECOND = 1000;

    // For the reference of UI elements
    private EditText txtLatitude;
    private EditText txtLongitude;
    private EditText txtRadius;
    private EditText txtExpirationDuration;
    private Spinner spinnerTypes;
    private TextView txtAddress;
    private Intent intent;

    // Type of the geofence
    private int mType;

    private boolean isValidLatitudeValue(String value) {
        double lat;
        if (value.matches("")) lat = INVALID_FLOAT_VALUE;
        else lat = Double.parseDouble(value);
        return lat >= -85f && lat <= 85f;
    }

    private boolean isValidLongitudeValue(String value) {
        double lng;
        if (value.matches("")) lng = INVALID_FLOAT_VALUE;
        else lng = Double.parseDouble(value);
        return lng >= -180f && lng <= 180f;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_geofence);

        // Get the latitude and longitude from the intent
        intent = getIntent();
        txtLatitude = (EditText) findViewById(R.id.txt_latitude);
        txtLongitude = (EditText) findViewById(R.id.txt_longitude);
        txtRadius = (EditText) findViewById(R.id.txt_radius);
        txtExpirationDuration = (EditText) findViewById(R.id.txt_expiration);
        spinnerTypes = (Spinner) findViewById(R.id.spinner_type);
        txtAddress = (TextView) findViewById(R.id.txt_place_address);

        // The default value of type (Both);
        mType = Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT;

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.geofence_type_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerTypes.setAdapter(adapter);
        spinnerTypes.setOnItemSelectedListener(this);

        txtLatitude.setOnFocusChangeListener(this);
        txtLongitude.setOnFocusChangeListener(this);
        txtRadius.setOnFocusChangeListener(this);
        txtExpirationDuration.setOnFocusChangeListener(this);

        if (intent.hasExtra(PREFIX+".latlng")) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    double extra[] = intent.getDoubleArrayExtra(PREFIX+".latlng");

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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

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
        if (isValidLatitudeValue(txtLatitude.getText().toString()) &&
                isValidLongitudeValue(txtLongitude.getText().toString()))
            new LoadPlaces().execute();
        else runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtAddress.setText("Invalid latitude or longitude!");
            }
        });
    }

    class LoadPlaces extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
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
                // Separeate your place types by PIPE symbol "|"
                // If you want all types places make it as null
                // Check list of types supported by google
                String types = null; // Listing all detected types of places

                // Radius in meters - increase this value if you don't find any places
                double radius = 80; // 80 meters
                nearPlaces = googlePlaces.search(Double.parseDouble(txtLatitude.getText().toString()),
                        Double.parseDouble(txtLongitude.getText().toString()), radius, types);
                // continue searching for places until finding any place and increase the searching radius up to 160 metres
                for (int i=1; i<8 && nearPlaces==null; i++) {
                    radius += 10;
                    // get nearest places
                    nearPlaces = googlePlaces.search(Double.parseDouble(txtLatitude.getText().toString()),
                            Double.parseDouble(txtLongitude.getText().toString()), radius, types);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * and show the data in UI
         * Always use runOnUiThread(new Runnable()) to update UI from background
         * thread, otherwise you will get error
         * **/
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
            else {
                // Zero results found
                alert.showAlertDialog(AddGeofenceActivity.this, "Knock Knock",
                        "Can not retrieve any address",
                        false,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                exit(1);
                            }
                        });
            }

        }

    }

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

            // creating Places class object
            //googlePlaces = new GooglePlaces();

            // Check if user is connected to Internet
            try {
                placeDetails = googlePlaces.getPlaceDetails(reference);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * and show the data in UI
         * Always use runOnUiThread(new Runnable()) to update UI from background
         * thread, otherwise you will get error
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

    public void showHelpActivity(View view){
        Intent intentHelp = new Intent(this, DetailsHelpActivity.class);
        startActivity(intentHelp);
    }

    public void finishTheActivity(View view) {
        finish();
    }

    public void addReminder(View view) {
        Intent intentReminder = new Intent();
        intentReminder.putExtra("GeofenceData", "Data successfully acquired.");
        intentReminder.putExtra("GeofenceLat", Double.parseDouble(txtLatitude.getText().toString()));
        intentReminder.putExtra("GeofenceLng", Double.parseDouble(txtLongitude.getText().toString()));
        intentReminder.putExtra("GeofenceRad", Float.parseFloat(txtRadius.getText().toString()));
        intentReminder.putExtra("GeofenceExp", Long.parseLong(txtExpirationDuration.getText().toString()));
        intentReminder.putExtra("GeofenceType", mType);
        setResult(RESULT_OK, intentReminder);
        finish();
    }
}
