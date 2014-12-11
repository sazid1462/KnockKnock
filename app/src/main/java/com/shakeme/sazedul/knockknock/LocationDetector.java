package com.shakeme.sazedul.knockknock;

/***
 * Created by Sazedul on 01-Dec-14.
 **/

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationStatusCodes;

import java.util.ArrayList;

public class LocationDetector implements
        LocationListener, GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        LocationClient.OnAddGeofencesResultListener, LocationClient.OnRemoveGeofencesResultListener {

    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in seconds
    public static final int UPDATE_INTERVAL_IN_SECONDS_ONSTOP = 10;
    // Update frequency in seconds
    public static final int UPDATE_INTERVAL_IN_SECONDS_ONSTART = 5;
    // Update frequency in milliseconds when the app is running in background
    private static final long UPDATE_INTERVAL_ONSTOP = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS_ONSTOP;
    // Update frequency in milliseconds when the app is visible
    private static final long UPDATE_INTERVAL_ONSTART = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS_ONSTART;
    // The fastest update frequency, in seconds when visible
    private static final int FASTEST_INTERVAL_IN_SECONDS_ONSTART = 1;
    // A fast frequency ceiling in milliseconds when visible
    private static final long FASTEST_INTERVAL_ONSTART =
            MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS_ONSTART;
    // The fastest update frequency, in seconds when not visible
    private static final int FASTEST_INTERVAL_IN_SECONDS_ONSTOP = 5;
    // A fast frequency ceiling in milliseconds when not visible
    private static final long FASTEST_INTERVAL_ONSTOP =
            MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS_ONSTOP;

    private static final String TAG = "LocationDetector";

    /**
     * Request code for auto Google Play Services error resolution.
     */
    private static final int REQUEST_CODE_RESOLUTION = 2;

    // Alert Dialog Manager
    MessageDialogueViewer alert = new MessageDialogueViewer();

    // Define an object that holds accuracy and frequency parameters
    private LocationRequest mLocationRequest;
    private boolean mUpdatesRequested;
    /**
     * Location client for handling location requests
     */
    private LocationClient mLocationClient;

    SharedPreferences mPrefs;
    SharedPreferences.Editor mEditor;

    // Defines the allowable request types
    public enum REQUEST_TYPE {
        ADD,
        REMOVE,
        REMOVE_INTENT,
        IDLE
    }
    private REQUEST_TYPE mRequestType;
    // Flag that indicates if a request is underway.
    private boolean mInProgress;
    /**
     * Determines if the client is in a resolution state, and
     * waiting for resolution intent to return.
     */
    public boolean mIsInResolution;

    private PendingIntent mGeofencesRequestIntent;

    private final Activity activity;

    // Persistent storage for geofences
    private SimpleGeofenceStore mGeofenceStorage;
    private ArrayList<Geofence> mCurrentGeofences;
    private ArrayList<String> mGeofencesToRemove;
    /*
     * Define a request code to send to Google Play Services. This code is
     * returned in Activity.OnActivityResult
     */
    private final static int CONNECTION_FAILURE_RESULATION_REQUEST = 9000;

    private Location mCurrentLocation;


    public Location getCurrentLocation(){
        return mCurrentLocation;
    }

    public static class ErrorDialogueFragment extends DialogFragment {
        // Global field to contain the error dialog
        private Dialog mDialog;

        // Default constructor. Sets the dialog field to null
        public ErrorDialogueFragment() {
            super();
            mDialog = null;
        }

        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        // Return a Dialog to the DialogFragment.
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

    /**
     * General constructor for the LocationDetector
     */
    public LocationDetector(Activity activity) {
        // Set the activity of the LocationDetector
        this.activity = activity;

        mLocationClient = new LocationClient(activity, this, this);

        mRequestType = REQUEST_TYPE.IDLE;

        // Instantiate a new geofence storage area
        mGeofenceStorage = new SimpleGeofenceStore(activity);
        // Instantiate the current List of geofences
        mCurrentGeofences = new ArrayList<>();
        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create();
        // Start with the request flag set to false
        mInProgress = false;
    }

    public void startLocationDetector() {
        // Open the shared preferences
        mPrefs = activity.getSharedPreferences("SharedPreferences",
                Context.MODE_PRIVATE);
        // Get a SharedPreferences editor
        mEditor = mPrefs.edit();
        mEditor.apply();
        if (mLocationClient==null) {
            mLocationClient = new LocationClient(activity, this, this);
        }
        mLocationClient.connect();
    }

    public void pauseLocationDetector() {
        // Save the current setting for updates
        mEditor.putBoolean("KEY_UPDATES_ON", mUpdatesRequested);
        mEditor.commit();
    }

    protected void resumeLocationDetector() {
        // Start with updates turned off
        mUpdatesRequested = false;
        /*
         * Get any previous setting for location updates
         * Gets "false" if an error occurs
         */
        if (mPrefs.contains("KEY_UPDATES_ON")) {
            mUpdatesRequested =
                    mPrefs.getBoolean("KEY_UPDATES_ON", false);

            // Otherwise, turn off location updates
        } else {
            mEditor.putBoolean("KEY_UPDATES_ON", false);
            mEditor.commit();
        }
    }

    protected void stopLocationDetector() {
        // If the client is connected
        if (mLocationClient.isConnected()) {
            mLocationClient.disconnect();
        }
    }

    /**
     * Handles Google Play Services resolution callbacks.
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_RESOLUTION:
                retryConnecting();
                break;
        }
    }

    private void retryConnecting() {
        mIsInResolution = false;
        if (!mLocationClient.isConnecting()) {
            mLocationClient.connect();
        }
    }

    /**
     * Called when {@code mLocationClient} is connected.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "LocationClient connected");
        if (mUpdatesRequested) {
            mLocationClient.requestLocationUpdates(mLocationRequest, this);
        }
        Log.i(TAG, "GooglePlayServicesClient connected");
        switch (mRequestType) {
            case ADD:
                // Get the PendingIntent for the request
                PendingIntent mTransitionPendingIntent = getTransitionPendingIntent();
                // Send a request to add the current geofences
                mLocationClient.addGeofences(mCurrentGeofences, mTransitionPendingIntent, this);
                break;
            case REMOVE_INTENT:
                mLocationClient.removeGeofences(mGeofencesRequestIntent, this);
                break;
            case REMOVE:
                mLocationClient.removeGeofences(mGeofencesToRemove, this);
                break;
        }
    }

    /**
     * Called when {@code mLocationClient} is disconnected.
     */
    @Override
    public void onDisconnected() {
        Log.i(TAG, "LocationClient disconnected");
        // Turn off the request flag
        mInProgress = false;
        mLocationClient = null;
    }

    /**
     * Called when {@code mGooglePlayServicesClient} is trying to connect but failed.
     * Handle {@code result.getResolution()} if there is a resolution
     * available.
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "GooglePlayServicesClient connection failed: " + result.toString());
        // Turn off the request flag
        mInProgress = false;
        /*
         * If the error has a resolution, start a Google Play Services activity
         * to resolve it.
         */
        if (result.hasResolution()) {
            try {
                result.startResolutionForResult(activity, CONNECTION_FAILURE_RESULATION_REQUEST);
            } catch (SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            // Get the error code
            int errorCode = result.getErrorCode();
            // Get the error dialog from Google Play Services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(errorCode, activity, CONNECTION_FAILURE_RESULATION_REQUEST);
            // Show a localized error dialog.
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                ErrorDialogueFragment errorFragment = new ErrorDialogueFragment();
                // Set the dialog in the DialogFragment
                errorFragment.setDialog(errorDialog);
                // Show the error dialog in the DialogFragment
                errorFragment.show(activity.getFragmentManager(), "Knock Knock");
            }
            return;
        }
        // If there is an existing resolution error being displayed or a resolution
        // activity has started before, do nothing and wait for resolution
        // progress to be completed.
        if (mIsInResolution) {
            return;
        }
        mIsInResolution = true;
        try {
            result.startResolutionForResult(activity, REQUEST_CODE_RESOLUTION);
        } catch (SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
            retryConnecting();
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        String msg = "Updated Location: " + LocationUtilities.getLatLngString(location);
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
        mCurrentLocation = location;
    }

    @Override
    public void onAddGeofencesResult(int statusCode, String[] strings) {
        // If adding the geofences was successful
        if (LocationStatusCodes.SUCCESS == statusCode) {
            /*
             * Handle successful addition of geofences.
             * Show a confirmation dialog to the user
             */
            alert.showAlertDialog(activity, "Add Reminder", "Your reminder for the location "+strings[0]+" is successful.", true,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
        } else {
            /*
             * If adding the geofences failed report errors to the user
             */
            alert.showAlertDialog(activity, "Add Reminder", "Sorry, your reminder for the location "+strings[0]+" is unsuccessful.\n" +
                            "Restart the app and try again later.", false,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
        }
        // Turn off the in progress flag.
        mInProgress = false;
        mLocationClient.disconnect();
    }


    @Override
    public void onRemoveGeofencesByRequestIdsResult(int i, String[] strings) {

    }

    /**
     * When the request to remove geofences by PendingIntent returns, handle the result
     *
     * @param statusCode the code returned by Location Services
     * @param requestIntent The Intent used to request the removal
     */
    @Override
    public void onRemoveGeofencesByPendingIntentResult(int statusCode, PendingIntent requestIntent) {
        // If removing the geofences was successful
        if (statusCode == LocationStatusCodes.SUCCESS) {
            /*
             * Handle successful removal of geofences.
             * Show a confirmation dialog to the user
             */
            alert.showAlertDialog(activity, "Stop Tracking Reminders", "Your stop request for all reminders is successful.", true,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
        } else {
            /*
             * If adding the geofences failed report errors to the user
             */
            alert.showAlertDialog(activity, "Stop Tracking Reminders", "Sorry, your stop request for all reminders is unsuccessful.\n" +
                            "Restart the app and try again later.", false,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
        }
        // Turn off the in progress flag.
        mInProgress = false;
        mLocationClient.disconnect();
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
         * Create an internal object to store the data. Get its ID by calling the getNextGeofenceID()
         * method and set it. This is a "flattened" object that contains a set of strings
         */
        String id = MapsActivity.getNextGeofenceID();
        if (!id.matches("-1")) {
            SimpleGeofence mSimpleGeofence = new SimpleGeofence(id, name, latitude, longitude, radius, expiration < 0 ? -1 : expiration, type);
            // Store this flat version
            mGeofenceStorage.setGeofence(mSimpleGeofence.getId(), mSimpleGeofence);
            mCurrentGeofences.add(mSimpleGeofence.toGeofence());;
            addGeofences();
        } else {
            alert.showAlertDialog(activity, "Knock Knock", "Sorry, no more geofence is available!", false, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
        }
    }

    public void deleteGeofences(String ids[]) {
        // Instantiate the list of geofences ids to be removed
        ArrayList<String> mDeleteGeofences = new ArrayList<>();
        for (int i=0; i<ids.length; i++) {
            mGeofenceStorage.clearGeofence(ids[i]);
            mDeleteGeofences.add(ids[i]);
        }
        removeGeofences(mDeleteGeofences);
    }

    /**
     * Start a request to add geofences
     */
    public void addGeofences(){
        // Record the type of add request
        mRequestType = REQUEST_TYPE.ADD;
        /*
         * Test for GooglePlayServices after setting the request type.
         * If GooglePlayServices isn't present, the proper request can be restarted
         */
        if (!servicesConnected()) {
            return;
        }
        /*
         * Create a new location client object. Since the current class implements ConnectionCallbacks and
         * OnConnectionFailedListener, pass the current class object as the listener for both parameters
         */
        mLocationClient = new LocationClient(activity, this, this);
        // If a request is not already underway
        if (!mInProgress) {
            // Indicate that a request is underway
            mInProgress = true;
            // Request a connection from the client to Location Services
            mLocationClient.connect();
        } else {
            // Notify the user that a request is being processed
            alert.showAlertDialog(activity, "Add Reminder", "A request is being processed at this moment. Please try again later.",
                    false, new DialogInterface.OnClickListener() {
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
        // Record the type of removal request
        mRequestType = REQUEST_TYPE.REMOVE_INTENT;
        /*
         * Test for GooglePlayServices after setting the request type.
         * If GooglePlayServices isn't present, the proper request can be restarted
         */
        if (!servicesConnected()) {
            return;
        }
        // Store the list of geofences to remove
        mGeofencesToRemove= geofenceIds;
        /*
         * Create a new location client.
         */
        mLocationClient = new LocationClient(activity, this, this);
        // If a request is not already underway
        if (!mInProgress) {
            // Indicate that a request is underway
            mInProgress = true;
            mLocationClient.connect();
        } else {
            // Notify the user that a request is being processed
            alert.showAlertDialog(activity, "Stop Tracking Reminders", "A request is being processed at this moment. Please try again later.",
                    false, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
        }
    }

    /**
     * Start a request to remove geofences by calling LocationClient.connect()
     */
    public void removeGeofences(PendingIntent requestIntent){
        // Record the type of removal request
        mRequestType = REQUEST_TYPE.REMOVE_INTENT;
        /*
         * Test for GooglePlayServices after setting the request type.
         * If GooglePlayServices isn't present, the proper request can be restarted
         */
        if (!servicesConnected()) {
            return;
        }
        // Store the PendingIntent
        mGeofencesRequestIntent = requestIntent;
        /*
         * Create a new location client.
         */
        mLocationClient = new LocationClient(activity, this, this);
        // If a request is not already underway
        if (!mInProgress) {
            // Indicate that a request is underway
            mInProgress = true;
            mLocationClient.connect();
        } else {
            // Notify the user that a request is being processed
            alert.showAlertDialog(activity, "Stop Tracking Reminders", "A request is being processed at this moment. Please try again later.",
                    false, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
        }
    }

    private boolean servicesConnected() {
        // Check that GooglePlayservices is available
        int errorCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        // If GooglePlayServices is available
        if (errorCode == ConnectionResult.SUCCESS) {
            // In debug mode, log the status
            Log.d("Geofence Detection", "Google Play Services is available.");
            return true;
            // GooglePlayServices is not available for some reason
        }else{
            GooglePlayServicesUtil.getErrorDialog(errorCode, activity, 0).show();
            return false;
        }
    }

    /*
     * Create a PendingIntent that triggers an IntentService in the app when a geofence
     * transition occurs
     */
    private PendingIntent getTransitionPendingIntent(){
        // Create an explicit Intent
        Intent intent = new Intent(activity, ReceiveGeofenceTransitionsIntentService.class);
        /*
         * Return the PendingIntent
         */
        return PendingIntent.getService(activity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
