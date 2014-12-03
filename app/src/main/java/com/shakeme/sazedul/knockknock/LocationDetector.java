package com.shakeme.sazedul.knockknock;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;

import java.util.Vector;


public class LocationDetector extends FragmentActivity implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

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

    private static final String KEY_IN_RESOLUTION = "is_in_resolution";

    /**
     * Request code for auto Google Play Services error resolution.
     */
    protected static final int REQUEST_CODE_RESOLUTION = 1;

    // Define an object that holds accuracy and frequency parameters
    LocationRequest mLocationRequest;

    /**
     * Location client for handling location requests
     */
    private LocationClient mLocationClient;

    /**
     * List of Geofences
     */
    private Vector<Geofence> listGeofences;

    // Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends DialogFragment {
        // Global field to contain the error dialog
        private Dialog mDialog;
        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }
        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }
        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

    /**
     * Determines if the client is in a resolution state, and
     * waiting for resolution intent to return.
     */
    private boolean mIsInResolution;

    public Location getCurrentLocation(){
        return mLocationClient.getLastLocation();
    }

    /**
     * Called when the activity is starting. Restores the activity state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mIsInResolution = savedInstanceState.getBoolean(KEY_IN_RESOLUTION, false);
        }
        mLocationClient = new LocationClient(this, this, this);

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create();
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
        if (mLocationClient == null) {
            mLocationClient = new LocationClient(this, this, this);
        }
        mLocationClient.connect();

        // Use high accuracy
        mLocationRequest.setPriority(
                LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the update interval to 5 seconds
        mLocationRequest.setInterval(UPDATE_INTERVAL_ONSTART);
        // Set the fastest update interval to 1 second
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL_ONSTART);
    }

    /**
     * Called when activity gets invisible. Connection to Play Services needs to
     * be disconnected as soon as an activity is invisible.
     */
    @Override
    protected void onStop() {
        if (mLocationClient != null) {
            mLocationClient.disconnect();
        }
        // Use high accuracy
        mLocationRequest.setPriority(
                LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        // Set the update interval to 5 seconds
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_SECONDS_ONSTOP);
        // Set the fastest update interval to 1 second
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL_IN_SECONDS_ONSTOP);

        super.onStop();
    }

    /**
     * Saves the resolution state.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_IN_RESOLUTION, mIsInResolution);
    }

    /**
     * Handles Google Play Services resolution callbacks.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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
     * Called when {@code mGoogleApiClient} is connected.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "LocationClient connected");
    }

    @Override
    public void onDisconnected() {
        Log.i(TAG, "LocationClient disconnected");

    }

    /**
     * Called when {@code mGooglePlayServicesClient} is trying to connect but failed.
     * Handle {@code result.getResolution()} if there is a resolution
     * available.
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "GooglePlayServicesClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            // Show a localized error dialog.
            GooglePlayServicesUtil.getErrorDialog(
                    result.getErrorCode(), this, 0, new OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            retryConnecting();
                        }
                    }).show();
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
            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
            retryConnecting();
        }
    }

    public void addGeofence(LatLng latLng){
        listGeofences.add(new Geofence() {
            @Override
            public String getRequestId() {
                return null; // TODO
            }
        });
    }
}
