package com.shakeme.sazedul.knockknock;

/***
 * Created by Sazedul on 01-Dec-14.
 **/

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class MapsActivity extends FragmentActivity implements
        GoogleMap.OnMapClickListener {

    private static final String TAG = "MapActivity";
    // RequestCode for starting ListGeofenceActivity for the result
    private static final int REQUEST_CODE_FOR_LIST_GEOFENCE = 3;
    // RequestCode for starting AddGeofenceActivity for the result
    private static final int REQUEST_CODE_FOR_ADD_GEOFENCE = 1;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private TextView mCurrentLocation;

    // flag for Internet connection status
    Boolean isInternetPresent = false;

    // Connection detector class
    NetworkConnectivityDetector nCDetector;

    // Alert Dialog Manager
    MessageDialogueViewer alert = new MessageDialogueViewer();

    // Google Places
    GooglePlaces googlePlaces;

    private static final String KEY_IN_RESOLUTION = "is_in_resolution";
    private static  final String KEY_GEOFENCE_ID = "com.shakeme.sazedul.knockknock.KEY_GEOFENCE_ID";
    private static  final String ACTIVE_GEOFENCE_EMPTY = "EMPTY";

    /*
    * Invalid values used to test geofence storage when retrieving geofences
    */
    public static final long INVALID_LONG_VALUE = -999l;
    public static final float INVALID_FLOAT_VALUE = -999.0f;
    public static final int INVALID_INT_VALUE = -999;
    private static final String DEFAULT_NAME = "New Reminder";

    // The name of the SharedPreferences
    private static final String SHARED_PREFERENCES = "KnockKnockSharedPreferences";
    // The SharedPreferences object in which geofences are stored
    private SharedPreferences mPrefs;

    // Places List
    PlaceList nearPlaces;

    // Select a place
    Place place;

    // Details of a place data
    PlaceDetails placeDetails;

    LocationDetector mLocationDetector;

    Location currentLocation;

    // Extra Message prefix
    public static final String PREFIX = "com.shakeme.sazedul.knockknock";

    // The MAX ID of a geofence
    private static final int MAX_ID = 100;
    // Mark the used id
    private static boolean usedId[] = new boolean[MAX_ID];

    // Store the Triggering Geofences Ids
    private static String triggeringGeofencesIds[];
    // Store the Triggering Geofences
    private static List<Geofence> triggeringGeofences;

    public static void setTriggeringGeofences(String ids[], List<Geofence> geofenceList) {
        triggeringGeofencesIds = ids;
        triggeringGeofences = geofenceList;
    }

    /**
     * Return if a geofence is active
     *
     * @param id Id of the geofence
     * @return If it is true/false
     */
    public static boolean isActiveGeofence(int id) {
        return usedId[id];
    }

    public static void setAsInactiveGeofence(int id) {
        usedId[id] = false;
    }

    public static void setAsActiveGeofence(int id) {
        usedId[id] = true;
    }
    public static String getNextGeofenceID () {
        for (int geofenceID=0; geofenceID<MAX_ID; geofenceID++) {
            if (!usedId[geofenceID]) {
                return Integer.toString((geofenceID) + 1);
            }
        }
        //System.out.println("GETNEXTGEOFENCEID is RETURNING -1");
        return "-1";
    }

    /**
     * Called when the app is launched.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        servicesConnected();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        setUpMapIfNeeded();
        mCurrentLocation = (TextView) findViewById(R.id.txt_current_location);
        nCDetector = new NetworkConnectivityDetector(getApplicationContext());
        mLocationDetector = new LocationDetector(this);
        mPrefs = getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);

        // Get the activeGeofencesId string from the SharedPreference
        String activeGeofencesId = mPrefs.getString(KEY_GEOFENCE_ID, ACTIVE_GEOFENCE_EMPTY);
        if (!activeGeofencesId.matches(ACTIVE_GEOFENCE_EMPTY)) {
            // Update the usedID according to activeGeofencesId
            String activeGeofencesIdArray[] = activeGeofencesId.split("_");
            for (String anActiveGeofencesIdArray : activeGeofencesIdArray) {
                if (!anActiveGeofencesIdArray.isEmpty()) {
                    usedId[Integer.parseInt(anActiveGeofencesIdArray)] = true;
                }
            }
        }
    }

    /**
     * Called when the Activity is made visible.
     * A connection to Play Services need to be initiated as
     * soon as the activity is visible. Registers {@code ConnectionCallbacks}
     * and {@code OnConnectionFailedListener} on the
     * activities itself.
     */
    @Override
    protected void onStart() {
        super.onStart();
        // Call the mLocationDetector to start
        mLocationDetector.startLocationDetector();
    }

    @Override
    protected void onPause() {
        // Call the mLocationDetector to pause
        mLocationDetector.pauseLocationDetector();
        super.onPause();
    }

    /**
     * Called when activity gets invisible. Connection to Play Services needs to
     * be disconnected as soon as an activity is invisible.
     */
    @Override
    protected void onStop() {
        // Call the mLocationDetector to stop
        mLocationDetector.stopLocationDetector();
        // Manipulate an activeGeofencesId string to store in the SharedPreference
        String activeGeofencesId = "_";
        for (int i=0; i<MAX_ID; i++) {
            if (usedId[i]) activeGeofencesId += Integer.toString(i)+"_";
        }
        /*
         * Get a SharedPreferences editor instance. Among other things,
         * SharedPreferences ensures that updates are atomic and non-concurrent
         */
        SharedPreferences.Editor editor = mPrefs.edit();
        // Write the usedGeofencesId value to SharedPreferences
        editor.putString(KEY_GEOFENCE_ID, activeGeofencesId);
        // Commit the changes
        editor.apply();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Call the mLocationDetector to resume
        mLocationDetector.resumeLocationDetector();
        // Check if Internet present
        isInternetPresent = nCDetector.isConnectionToInternetAvailable();
        if (!isInternetPresent) {
            // Internet Connection is not present
            alert.showAlertDialog(this, "Internet Connection Error",
                    "Please connect to the Internet for full app services.", false,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //exit(1);
                        }
                    });
            // stop executing code by return
            return;
        }
        setUpMapIfNeeded();
    }

    private boolean servicesConnected() {
        // Check that GooglePlayservices is available
        int errorCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        // If GooglePlayServices is available
        if (errorCode == ConnectionResult.SUCCESS) {
            // In debug mode, log the status
            Log.d("Geofence Detection", "Google Play Services is available.");
            return true;
            // GooglePlayServices is not available for some reason
        }else{
            GooglePlayServicesUtil.getErrorDialog(errorCode, this, 0).show();
            return false;
        }
    }

    /**
     * Saves the resolution state.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_IN_RESOLUTION, mLocationDetector.mIsInResolution);
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        //curLatLng = LocationUtilities.getLatLng(getCurrentLocation());

        mMap.setMyLocationEnabled(true);
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(curLatLng, 13));
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                currentLocation = location;
                if (nCDetector.isConnectionToInternetAvailable()) {
                    new LoadPlaces().execute();
                }
            }
        });

        mMap.setOnMapClickListener(this);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        Intent intent = new Intent(this, AddGeofenceActivity.class);
        double extra[] = {latLng.latitude, latLng.longitude};
        intent.putExtra(PREFIX + ".latlng", extra);
        startActivityForResult(intent, REQUEST_CODE_FOR_ADD_GEOFENCE);
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
                // Radius in meters - increase this value if you don't find any places
                double radius = 80; // 80 meters
                // Passing null as types will return all types of supported places found.
                nearPlaces = googlePlaces.search(currentLocation.getLatitude(),
                        currentLocation.getLongitude(), radius, null);
                // continue searching for places until finding any place and increase the searching radius up to 160 metres
                for (int i=1; i<8 && nearPlaces==null; i++) {
                    radius += 10;
                    // get nearest places
                    nearPlaces = googlePlaces.search(currentLocation.getLatitude(),
                            currentLocation.getLongitude(), radius, null);
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
                alert.showAlertDialog(MapsActivity.this, "Knock Knock",
                        "Can not retrieve any address",
                        false,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //exit(1);
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
                                mCurrentLocation.setText(address);
                            }
                        } else {
                            mCurrentLocation.setText("Could not find details.");
                        }
                    }
                }
            });

        }

    }

    public void addNewGeofence(View view){
        Intent intent = new Intent(this, AddGeofenceActivity.class);
        startActivityForResult(intent, REQUEST_CODE_FOR_ADD_GEOFENCE);
    }

    public void showListGeofence(View view) {
        Intent intentList = new Intent(this, ListGeofencesActivity.class);
        startActivityForResult(intentList, REQUEST_CODE_FOR_LIST_GEOFENCE);
    }
    /**
     * Handles Google Play Services resolution callbacks.
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mLocationDetector.onActivityResult(requestCode, resultCode, data);
        if (data == null) return;
        if (data.hasExtra("GeofenceData")) {
            String name = data.getStringExtra("GeofenceName");
            double lat = data.getDoubleExtra("GeofenceLat", INVALID_FLOAT_VALUE);
            double lng = data.getDoubleExtra("GeofenceLng", INVALID_FLOAT_VALUE);
            float rad = data.getFloatExtra("GeofenceRad", INVALID_FLOAT_VALUE);
            long exp = data.getLongExtra("GeofenceExp", INVALID_LONG_VALUE);
            int type = data.getIntExtra("GeofenceType", INVALID_INT_VALUE);

            //System.err.println("CREATING GEOFENCE : LAT "+lat+" LNG "+lng+" rad "+rad);

            mLocationDetector.createGeofence(name, lat, lng, rad, exp, type);
        }
        if (data.hasExtra("GeofenceShow")) {
            String id = data.getStringExtra("GeofenceId");
            // TODO
        }
        if (data.hasExtra("GeofenceDelete")) {
            String ids[] = data.getStringArrayExtra("GeofenceIds");
            mLocationDetector.deleteGeofences(ids);
        }
        if (data.hasExtra("GeofenceClear")) {
            mLocationDetector.clearAllGeofences();
        }
    }
}
