package com.shakeme.sazedul.knockknock;

/***
 * Created by Sazedul on 01-Dec-14.
 **/

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.shakeme.sazedul.knockknock.GeofenceUtils.REMOVE_TYPE;
import com.shakeme.sazedul.knockknock.GeofenceUtils.REQUEST_TYPE;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("FieldCanBeLocal")
public class MapsActivity extends FragmentActivity implements
        GoogleMap.OnMapClickListener, GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    private static final String TAG = "MapActivity";

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private TextView mCurrentLocation;

    // flag for Internet connection status
    private Boolean isInternetPresent = false;

    // Connection detector class
    private NetworkConnectivityDetector nCDetector;

    // Store the current request
    private REQUEST_TYPE mRequestType;

    // Store the current type of removal
    private REMOVE_TYPE mRemoveType;

    // Persistent storage for geofences
    private SimpleGeofenceStore mPrefs;

    // Store a list of geofences to add
    private List<Geofence> mCurrentGeofences;

    // Add geofences handler
    private GeofenceRequester mGeofenceRequester;
    // Remove geofences handler
    private GeofenceRemover mGeofenceRemover;

    // Alert Dialog Manager
    private MessageDialogueViewer alert = new MessageDialogueViewer();

    // Google Places
    private GooglePlaces googlePlaces;

    // Places List
    private PlaceList nearPlaces;

    // Select a place
    private Place place;

    // Details of a place data
    private PlaceDetails placeDetails;

    // decimal formats for latitude, longitude, and radius
    @SuppressWarnings("FieldCanBeLocal")
    private DecimalFormat mLatLngFormat;
    private DecimalFormat mRadiusFormat;

    private Location currentLocation;

    /*
     * An instance of an inner class that receives broadcasts from listeners and from the
     * IntentService that receives geofence transition events
     */
    private GeofenceReceiver mBroadcastReceiver;

    private boolean isFindingPlace;

    // An intent filter for the broadcast receiver
    private IntentFilter mIntentFilter;

    // Store the list of geofences to remove
    private List<String> mGeofenceIdsToRemove;

    // Mark the used id
    private static boolean usedId[] = new boolean[GeofenceUtils.MAX_ID];

    // Store the Triggering Geofences Ids
    private static String triggeringGeofencesIds[];
    // Store the Triggering Geofences
    private static List<Geofence> triggeringGeofences;

    private LocationClient locationClient;
    private LocationRequest locationRequest;

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

    public static void setAllGeofenceAsInactive () {
        usedId = new boolean[GeofenceUtils.MAX_ID];
    }

    public static String getNextGeofenceID () {
        for (int geofenceID=0; geofenceID<GeofenceUtils.MAX_ID; geofenceID++) {
            if (!usedId[geofenceID]) {
                return Integer.toString((geofenceID));
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

        int resp = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resp == ConnectionResult.SUCCESS) {
            locationClient = new LocationClient(this, this, this);
            locationClient.connect();
        }

        // Set the pattern for the latitude and longitude format
        String latLngPattern = getString(R.string.lat_lng_pattern);

        // Set the format for latitude and longitude
        mLatLngFormat = new DecimalFormat(latLngPattern);

        isFindingPlace = false;

        // Localize the format
        mLatLngFormat.applyLocalizedPattern(mLatLngFormat.toLocalizedPattern());

        // Set the pattern for the radius format
        String radiusPattern = getString(R.string.radius_pattern);

        // Set the format for the radius
        mRadiusFormat = new DecimalFormat(radiusPattern);

        // Localize the pattern
        mRadiusFormat.applyLocalizedPattern(mRadiusFormat.toLocalizedPattern());

        // Create a new broadcast receiver to receive updates from the listeners and service
        mBroadcastReceiver = new GeofenceReceiver();

        // Create an intent filter for the broadcast receiver
        mIntentFilter = new IntentFilter();

        // Action for broadcast Intents that report successful addition of geofences
        mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCES_ADDED);

        // Action for broadcast Intents that report successful removal of geofences
        mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCES_REMOVED);

        // Action for broadcast Intents containing various types of geofencing errors
        mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCE_ERROR);

        // All Location Services sample apps use this category
        mIntentFilter.addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES);

        // Instantiate a new geofence storage area
        mPrefs = new SimpleGeofenceStore(this);

        // Instantiate the current List of geofences
        mCurrentGeofences = new ArrayList<Geofence>();
        mGeofenceIdsToRemove = new ArrayList<String>();

        // Instantiate a Geofence requester
        mGeofenceRequester = new GeofenceRequester(this);

        // Instantiate a Geofence remover
        mGeofenceRemover = new GeofenceRemover(this);

        setUpMapIfNeeded();
        mCurrentLocation = (TextView) findViewById(R.id.txt_current_location);
        nCDetector = new NetworkConnectivityDetector(getApplicationContext());
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
        for (int i=0; i<GeofenceUtils.MAX_ID; i++) {
            SimpleGeofence geofence = mPrefs.getGeofence(Integer.toString(i));
            if (geofence != null) {
                setAsActiveGeofence(Integer.parseInt(geofence.getId()));
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    /**
     * Called when activity gets invisible. Connection to Play Services needs to
     * be disconnected as soon as an activity is invisible.
     */
    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     * Creates a Geofence as the given parameter
     *
     * @param latitude The latitude of the center of the geofence circle
     * @param longitude The longitude of the center of the geofence circle
     * @param radius The radius of the geofence circle
     * @param expiration Expiration duration of the geofence in milli seconds
     * @param type Transition type of the geofence
     */
    public void createGeofence(String name, double latitude, double longitude, float radius, long expiration, int type) {
        /*
         * Record the request as an ADD. If a connection error occurs,
         * the app can automatically restart the add request if Google Play services
         * can fix the error
         */
        mRequestType = GeofenceUtils.REQUEST_TYPE.ADD;

        /*
         * Check for Google Play services. Do this after
         * setting the request type. If connecting to Google Play services
         * fails, onActivityResult is eventually called, and it needs to
         * know what type of request was in progress.
         */
        if (!servicesConnected()) {

            return;
        }
        /*
         * Create an internal object to store the data. Get its ID by calling the getNextGeofenceID()
         * method and set it. This is a "flattened" object that contains a set of strings
         */
        String id = getNextGeofenceID();
        if (!id.matches("-1")) {
            SimpleGeofence mSimpleGeofence = new SimpleGeofence(id, name, latitude, longitude, radius,
                    expiration < 0 ? Geofence.NEVER_EXPIRE : expiration, type);
            // Store this flat version
            mPrefs.setGeofence(mSimpleGeofence.getId(), mSimpleGeofence);
            mCurrentGeofences.add(mSimpleGeofence.toGeofence());
            // Store this flat version in SharedPreferences
            //mPrefs.setGeofence("1", mSimpleGeofence);
            /*
             * Add Geofence objects to a List. toGeofence()
             * creates a Location Services Geofence object from a
             * flat object
             */
            //mCurrentGeofences.add(mSimpleGeofence.toGeofence());

            // Start the request. Fail if there's already a request in progress
            try {
                // Try to add geofences
                mGeofenceRequester.addGeofences(mCurrentGeofences);
            } catch (UnsupportedOperationException e) {
                // Notify user that previous request hasn't finished.
                Toast.makeText(this, R.string.add_geofences_already_requested_error,
                        Toast.LENGTH_LONG).show();
            }

        } else {
            alert.showAlertDialog(this, "Knock Knock", "Sorry, no more geofence is available!", false, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
        }
    }

    /**
     * Start a request to remove geofences by calling LocationClient.connect()
     */
    public void removeGeofences(ArrayList<String> geofenceIds) {
       /*
         * Remove the geofence by creating a List of geofences to
         * remove and sending it to Location Services. The List
         * contains the id of geofence 2, which is "2".
         * The removal happens asynchronously; Location Services calls
         * onRemoveGeofencesByPendingIntentResult() (implemented in
         * the current Activity) when the removal is done.
         */

        /*
         * Record the removal as remove by list. If a connection error occurs,
         * the app can automatically restart the removal if Google Play services
         * can fix the error
         */
        mRemoveType = GeofenceUtils.REMOVE_TYPE.LIST;
        // Instantiate the list of geofences ids to be removed
        mGeofenceIdsToRemove = geofenceIds;
        /*
         * Check for Google Play services. Do this after
         * setting the request type. If connecting to Google Play services
         * fails, onActivityResult is eventually called, and it needs to
         * know what type of request was in progress.
         */
        if (!servicesConnected()) {

            return;
        }

        // Try to remove the geofence
        try {
            mGeofenceRemover.removeGeofencesById(mGeofenceIdsToRemove);

            // Catch errors with the provided geofence IDs
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (UnsupportedOperationException e) {
            // Notify user that previous request hasn't finished.
            Toast.makeText(this, R.string.remove_geofences_already_requested_error,
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Called when the user clicks the "Remove geofences" button
     */
    public void unregisterByPendingIntent() {
        /*
         * Remove all geofences set by this app. To do this, get the
         * PendingIntent that was added when the geofences were added
         * and use it as an argument to removeGeofences(). The removal
         * happens asynchronously; Location Services calls
         * onRemoveGeofencesByPendingIntentResult() (implemented in
         * the current Activity) when the removal is done
         */

        /*
         * Record the removal as remove by Intent. If a connection error occurs,
         * the app can automatically restart the removal if Google Play services
         * can fix the error
         */
        // Record the type of removal
        mRemoveType = GeofenceUtils.REMOVE_TYPE.INTENT;

        /*
         * Check for Google Play services. Do this after
         * setting the request type. If connecting to Google Play services
         * fails, onActivityResult is eventually called, and it needs to
         * know what type of request was in progress.
         */
        if (!servicesConnected()) {

            return;
        }

        // Try to make a removal request
        try {
        /*
         * Remove the geofences represented by the currently-active PendingIntent. If the
         * PendingIntent was removed for some reason, re-create it; since it's always
         * created with FLAG_UPDATE_CURRENT, an identical PendingIntent is always created.
         */
            mGeofenceRemover.removeGeofencesByIntent(mGeofenceRequester.getRequestPendingIntent());

        } catch (UnsupportedOperationException e) {
            // Notify user that previous request hasn't finished.
            Toast.makeText(this, R.string.remove_geofences_already_requested_error,
                    Toast.LENGTH_LONG).show();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register the broadcast receiver to receive status updates
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, mIntentFilter);
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
        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {

            // In debug mode, log the status
            Log.d(GeofenceUtils.APPTAG, getString(R.string.play_services_available));

            // Continue
            return true;

            // Google Play services was not available for some reason
        } else {

            // Display an error dialog
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            if (dialog != null) {
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(dialog);
                errorFragment.show(getSupportFragmentManager(), GeofenceUtils.APPTAG);
            }
            return false;
        }
    }

    /**
     * Saves the resolution state.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
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
                if (nCDetector.isConnectionToInternetAvailable() && !isFindingPlace) {
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
        intent.putExtra(GeofenceUtils.PREFIX + ".latlng", extra);
        startActivityForResult(intent, GeofenceUtils.REQUEST_CODE_FOR_ADD_GEOFENCE);
    }

    class LoadPlaces extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isFindingPlace = true;
        }

        /**
         * getting Places JSON
         * */
        protected String doInBackground(String... args) {
            // creating Places class object
            googlePlaces = new GooglePlaces();

            try {
                // Radius in meters - increase this value if you don't find any places
                double radius = 100; // 80 meters
                // Passing null as types will return all types of supported places found.
                nearPlaces = googlePlaces.search(currentLocation.getLatitude(),
                        currentLocation.getLongitude(), radius, null);

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
                    isFindingPlace = false;
                }
            });
        }

    }

    public void addNewGeofence(View view){
        Intent intent = new Intent(this, AddGeofenceActivity.class);
        startActivityForResult(intent, GeofenceUtils.REQUEST_CODE_FOR_ADD_GEOFENCE);
    }

    public void showListGeofence(View view) {
        Intent intentList = new Intent(this, ListGeofencesActivity.class);
        startActivityForResult(intentList, GeofenceUtils.REQUEST_CODE_FOR_LIST_GEOFENCE);
    }

    /*
     * Handle results returned to this Activity by other Activities started with
     * startActivityForResult(). In particular, the method onConnectionFailed() in
     * GeofenceRemover and GeofenceRequester may call startResolutionForResult() to
     * start an Activity that handles Google Play services problems. The result of this
     * call returns here, to onActivityResult.
     * calls
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        // Choose what to do based on the request code
        switch (requestCode) {

            // If the request code matches the code sent in onConnectionFailed
            case GeofenceUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST :

                switch (resultCode) {
                    // If Google Play services resolved the problem
                    case Activity.RESULT_OK:

                        // If the request was to add geofences
                        if (GeofenceUtils.REQUEST_TYPE.ADD == mRequestType) {

                            // Toggle the request flag and send a new request
                            mGeofenceRequester.setInProgressFlag(false);

                            // Restart the process of adding the current geofences
                            mGeofenceRequester.addGeofences(mCurrentGeofences);

                            // If the request was to remove geofences
                        } else if (GeofenceUtils.REQUEST_TYPE.REMOVE == mRequestType ){

                            // Toggle the removal flag and send a new removal request
                            mGeofenceRemover.setInProgressFlag(false);

                            // If the removal was by Intent
                            if (GeofenceUtils.REMOVE_TYPE.INTENT == mRemoveType) {

                                // Restart the removal of all geofences for the PendingIntent
                                mGeofenceRemover.removeGeofencesByIntent(
                                        mGeofenceRequester.getRequestPendingIntent());

                                // If the removal was by a List of geofence IDs
                            } else {

                                // Restart the removal of the geofence list
                                mGeofenceRemover.removeGeofencesById(mGeofenceIdsToRemove);
                            }
                        }
                    // If any other result was returned by Google Play services
                    default:

                        // Report that Google Play services was unable to resolve the problem.
                        Log.d(GeofenceUtils.APPTAG, getString(R.string.no_resolution));
                }

                // If any other request code was received
            default:
                // Report that this Activity received an unknown requestCode
                Log.d(GeofenceUtils.APPTAG,
                        getString(R.string.unknown_activity_request_code, requestCode));
                switch (resultCode){
                    case Activity.RESULT_OK:
                        if (intent.hasExtra("GeofenceData")) {
                            String name = intent.getStringExtra("GeofenceName");
                            double lat = intent.getDoubleExtra("GeofenceLat", GeofenceUtils.INVALID_FLOAT_VALUE);
                            double lng = intent.getDoubleExtra("GeofenceLng", GeofenceUtils.INVALID_FLOAT_VALUE);
                            float rad = intent.getFloatExtra("GeofenceRad", GeofenceUtils.INVALID_FLOAT_VALUE);
                            long exp = intent.getLongExtra("GeofenceExp", GeofenceUtils.INVALID_LONG_VALUE);
                            int type = intent.getIntExtra("GeofenceType", GeofenceUtils.INVALID_INT_VALUE);

                            //System.err.println("CREATING GEOFENCE : LAT "+lat+" LNG "+lng+" rad "+rad);

                            createGeofence(name, lat, lng, rad, exp, type);
                        }
                        if (intent.hasExtra("GeofenceShow")) {
                            String id = intent.getStringExtra("GeofenceId");
                            // TODO
                        }
                        if (intent.hasExtra("GeofenceDelete")) {
                            String ids[] = intent.getStringArrayExtra("GeofenceIds");
                            ArrayList<String> geofencesToBeDelete = new ArrayList<>();
                            for (int i=0; i<ids.length; i++) {
                                geofencesToBeDelete.add(ids[i]);
                                setAsInactiveGeofence(Integer.parseInt(ids[i]));
                                mPrefs.clearGeofence(ids[i]);
                            }
                            removeGeofences(geofencesToBeDelete);
                        }
                        if (intent.hasExtra("GeofenceClear")) {
                            mPrefs.clearAllGeofences();
                            setAllGeofenceAsInactive();
                            unregisterByPendingIntent();
                        }
                        break;
                }
                break;
        }
    }

    /**
     * Define a Broadcast receiver that receives updates from connection listeners and
     * the geofence transition service.
     */
    public class GeofenceReceiver extends BroadcastReceiver {
        /*
         * Define the required method for broadcast receivers
         * This method is invoked when a broadcast Intent triggers the receiver
         */
        @Override
        public void onReceive(Context context, Intent intent) {

            // Check the action code and determine what to do
            String action = intent.getAction();

            // Intent contains information about errors in adding or removing geofences
            if (TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCE_ERROR)) {

                handleGeofenceError(context, intent);

                // Intent contains information about successful addition or removal of geofences
            } else if (
                    TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCES_ADDED)
                            ||
                            TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCES_REMOVED)) {

                handleGeofenceStatus(context, intent);

                // Intent contains information about a geofence transition
            } else if (TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCE_TRANSITION)) {

                handleGeofenceTransition(context, intent);

                // The Intent contained an invalid action
            } else {
                Log.e(GeofenceUtils.APPTAG, getString(R.string.invalid_action_detail, action));
                Toast.makeText(context, R.string.invalid_action, Toast.LENGTH_LONG).show();
            }
        }

        /**
         * If you want to display a UI message about adding or removing geofences, put it here.
         *
         * @param context A Context for this component
         * @param intent The received broadcast Intent
         */
        private void handleGeofenceStatus(Context context, Intent intent) {

        }

        /**
         * Report geofence transitions to the UI
         *
         * @param context A Context for this component
         * @param intent The Intent containing the transition
         */
        private void handleGeofenceTransition(Context context, Intent intent) {
            /*
             * If you want to change the UI when a transition occurs, put the code
             * here. The current design of the app uses a notification to inform the
             * user that a transition has occurred.
             */

        }

        /**
         * Report addition or removal errors to the UI, using a Toast
         *
         * @param intent A broadcast Intent sent by ReceiveTransitionsIntentService
         */
        private void handleGeofenceError(Context context, Intent intent) {
            String msg = intent.getStringExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS);
            Log.e(GeofenceUtils.APPTAG, msg);
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Define a DialogFragment to display the error dialog generated in
     * showErrorDialog.
     */
    public static class ErrorDialogFragment extends DialogFragment {

        // Global field to contain the error dialog
        private Dialog mDialog;

        /**
         * Default constructor. Sets the dialog field to null
         */
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        /**
         * Set the dialog to display
         *
         * @param dialog An error dialog
         */
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        /*
         * This method must return a Dialog to the DialogFragment.
         */
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(5 * 50 * 1000);
        locationRequest.setFastestInterval(5 * 50 * 1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    @Override
    public void onDisconnected() {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != locationClient) {
            locationClient.disconnect();
        }
    }
}
