package com.shakeme.sazedul.knockknock;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * Created by Sazedul on 08-Dec-14.
 *
 * Storage for geofence values, implemented in SharedPreferences
 */
public class SimpleGeofenceStore {
    // Keys for flattened geofences stored in SharedPreferences
    public static final String KEY_LATITUDE = "com.shakeme.sazedul.knockknock.KEY_LATITUDE";
    public static final String KEY_LONGITUDE = "com.shakeme.sazedul.knockknock.KEY_LONGITUDE";
    public static final String KEY_RADIUS = "com.shakeme.sazedul.knockknock.KEY_RADIUS";
    public static final String KEY_EXPIRATION_DURATION = "com.shakeme.sazedul.knockknock.KEY_EXPIRATION_DURATION";
    public static final String KEY_TRANSITION_TYPE = "com.shakeme.sazedul.knockknock.KEY_TRANSITION_TYPE";
    public static final String KEY_PREFIX = "com.shakeme.sazedul.knockknock.KEY";

    /*
     * Invalid values used to test geofence storage when retrieving geofences
     */
    public static final long INVALID_LONG_VALUE = -999l;
    public static final float INVALID_FLOAT_VALUE = -999.0f;
    public static final int INVALID_INT_VALUE = -999;

    // The SharedPreferences object in which geofences are stored
    private final SharedPreferences mPrefs;
    // The name of the SharedPreferences
    private static final String SHARED_PREFERENCES = "KnockKnockSharedPreferences";
    // Create the SharedPreferences with private access only
    public SimpleGeofenceStore(Context context) {
        mPrefs = context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_APPEND);
    }

    /**
     * Returns a stored geofence by its id, or returns null if it's not found.
     *
     * @param id The id of a stored geofence
     * @return A geofence defined by its center and radius.
     */
    public SimpleGeofence getGeofence(String id) {
        /*
         * Get the latitude for the geofence identified by id, or
         * INVALID_FLOAT_VALUE if it doesn't exist
         */
        double lat = mPrefs.getFloat(getGeofenceFieldKey(id, KEY_LATITUDE), INVALID_FLOAT_VALUE);
        /*
         * Get the longitude for the geofence identified by id, or
         * INVALID_FLOAT_VALUE if it doesn't exist
         */
        double lng = mPrefs.getFloat(getGeofenceFieldKey(id, KEY_LONGITUDE), INVALID_FLOAT_VALUE);
        /*
         * Get the radius for the geofence identified by id, or
         * INVALID_FLOAT_VALUE if it doesn't exist
         */
        float radius = mPrefs.getFloat(getGeofenceFieldKey(id, KEY_RADIUS), INVALID_FLOAT_VALUE);
        /*
         * Get the expiration duration for the geofence identified by id, or
         * INVALID_LONG_VALUE if it doesn't exist
         */
        long expirationDuration = mPrefs.getLong(getGeofenceFieldKey(id, KEY_EXPIRATION_DURATION), INVALID_LONG_VALUE);
        /*
         * Get the transition type for the geofence identified by id, or
         * INVALID_INT_VALUE if it doesn't exist
         */
        int transitionType = mPrefs.getInt(getGeofenceFieldKey(id, KEY_TRANSITION_TYPE), INVALID_INT_VALUE);

        // If none of the values is incorrect, return the object
        if (lat != INVALID_FLOAT_VALUE && lng != INVALID_FLOAT_VALUE &&
                expirationDuration != INVALID_LONG_VALUE &&
                radius != INVALID_FLOAT_VALUE && transitionType != INVALID_INT_VALUE) {
            // Return a true Geofence object
            System.out.println("YOUR FUCKING ID IS "+id);
            return new SimpleGeofence(id, lat, lng, radius, expirationDuration, transitionType);
        } else {
            System.out.println("FUCKING NULL IS GONNA RETURNED");
            return null;
        }
    }

    /**
     * Save a geofence.
     *
     * @param id ID of the geofence
     * @param geofence The SimpleGeofence containing the values
     *                 you want to save in SharedPreferences
     */
    public void setGeofence(String id, SimpleGeofence geofence){
        /*
         * Get a SharedPreferences editor instance. Among other things,
         * SharedPreferences ensures that updates are atomic and non-concurrent
         */
        Editor editor = mPrefs.edit();
        // Write the Geofence vaues to SharedPreferences
        editor.putFloat(getGeofenceFieldKey(id, KEY_LATITUDE), geofence.getLatitude().floatValue());
        editor.putFloat(getGeofenceFieldKey(id, KEY_LONGITUDE), geofence.getLongitude().floatValue());
        editor.putFloat(getGeofenceFieldKey(id, KEY_RADIUS), geofence.getRadius());
        editor.putLong(getGeofenceFieldKey(id, KEY_EXPIRATION_DURATION), geofence.getExpirationDuration());
        editor.putInt(getGeofenceFieldKey(id, KEY_TRANSITION_TYPE), geofence.getTransitionType());
        // Commit the changes
        editor.apply();
    }

    public void clearGeofence(String id) {
        /*
         * Remove a flattened geofence object from storage by removing all of its keys
         */
        Editor editor = mPrefs.edit();
        // First remove it from active geofences
        MapsActivity.setAsInactiveGeofence(Integer.parseInt(id));
        // Remove the keys
        editor.remove(getGeofenceFieldKey(id, KEY_LATITUDE));
        editor.remove(getGeofenceFieldKey(id, KEY_LONGITUDE));
        editor.remove(getGeofenceFieldKey(id, KEY_RADIUS));
        editor.remove(getGeofenceFieldKey(id, KEY_EXPIRATION_DURATION));
        editor.remove(getGeofenceFieldKey(id, KEY_TRANSITION_TYPE));
        editor.apply();
    }

    /**
     * Given a Geofence object's ID and the name of the field (for example, KEY_LATITUDE),
     * return the key name of the object's values in SharedPreferences
     *
     * @param id The id of a geofence object
     * @param fieldName The field represented by the key
     * @return The full key name of a value in SharedPreferences
     */
    private String getGeofenceFieldKey(String id, String fieldName) {
        return KEY_PREFIX + "_" + id + "_" + fieldName;
    }
}
