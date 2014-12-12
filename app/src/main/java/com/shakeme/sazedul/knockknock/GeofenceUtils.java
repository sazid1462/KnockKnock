package com.shakeme.sazedul.knockknock;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;

/**
 * Created by Sazedul on 12-Dec-14.
 */
public class GeofenceUtils {
    // Intent actions
    public static final String ACTION_CONNECTION_ERROR =
            "com.shakeme.sazedul.knockknock.ACTION_CONNECTION_ERROR";

    public static final String ACTION_CONNECTION_SUCCESS =
            "com.shakeme.sazedul.knockknock.ACTION_CONNECTION_SUCCESS";

    public static final String ACTION_GEOFENCES_ADDED =
            "com.shakeme.sazedul.knockknock.ACTION_GEOFENCES_ADDED";

    public static final String ACTION_GEOFENCES_REMOVED =
            "com.shakeme.sazedul.knockknock.ACTION_GEOFENCES_DELETED";

    public static final String ACTION_GEOFENCE_ERROR =
            "com.shakeme.sazedul.knockknock.ACTION_GEOFENCES_ERROR";

    public static final String ACTION_GEOFENCE_TRANSITION =
            "com.shakeme.sazedul.knockknock.ACTION_GEOFENCE_TRANSITION";

    public static final String ACTION_GEOFENCE_TRANSITION_ERROR =
            "com.shakeme.sazedul.knockknock.ACTION_GEOFENCE_TRANSITION_ERROR";

    // The Intent category used by all Location Services sample apps
    public static final String CATEGORY_LOCATION_SERVICES =
            "com.shakeme.sazedul.knockknock.CATEGORY_LOCATION_SERVICES";

    // Keys for extended data in Intents
    public static final String EXTRA_CONNECTION_CODE =
            "com.shakeme.sazedul.knockknock.EXTRA_CONNECTION_CODE";

    public static final String EXTRA_CONNECTION_ERROR_CODE =
            "com.shakeme.sazedul.knockknock.EXTRA_CONNECTION_ERROR_CODE";

    public static final String EXTRA_CONNECTION_ERROR_MESSAGE =
            "com.shakeme.sazedul.knockknock.EXTRA_CONNECTION_ERROR_MESSAGE";

    public static final String EXTRA_GEOFENCE_STATUS =
            "com.shakeme.sazedul.knockknock.EXTRA_GEOFENCE_STATUS";
    public static final String EXTRA_GEOFENCE_ID = "com.shakeme.sazedul.knockknock.EXTRA_GEOFENCE_ID";

    public static final String EXTRA_GEOFENCE_TRANSITION_TYPE = "com.shakeme.sazedul.knockknock.EXTRA_GEOFENCE_TRANSITION_TYPE";
    // Extra Message prefix
    public static final String PREFIX = "com.shakeme.sazedul.knockknock";
    public static final int REQUEST_CODE_FOR_REMOVE_GEOFENCE = 5;

    // Defines the allowable request types
    /*public enum REQUEST_TYPE {
        ADD,
        REMOVE,
        REMOVE_INTENT,
        IDLE
    }*/

    // Used to track what type of geofence removal request was made.
    public enum REMOVE_TYPE {INTENT, LIST}

    // Used to track what type of request is in process
    public enum REQUEST_TYPE {ADD, REMOVE}

    /*
     * A log tag for the application
     */
    public static final String APPTAG = "Knock Knock";

    /** Global instance of the HTTP transport. */
    public static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    // Google API Key
    public static final String API_KEY = "AIzaSyCRLa4LQZWNQBcjCYcIVYA45i9i8zfClqc";

    // Google Places serach url's
    public static final String PLACES_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/search/json?";
    public static final String PLACES_TEXT_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/search/json?";
    public static final String PLACES_DETAILS_URL = "https://maps.googleapis.com/maps/api/place/details/json?";

    /**
     * Request code for auto Google Play Services error resolution.
     */
    public static final int REQUEST_CODE_RESOLUTION = 2;

    // RequestCode for starting ListGeofenceActivity for the result
    public static final int REQUEST_CODE_FOR_LIST_GEOFENCE = 3;
    // RequestCode for starting AddGeofenceActivity for the result
    public static final int REQUEST_CODE_FOR_ADD_GEOFENCE = 1;

    public static final String KEY_IN_RESOLUTION = "is_in_resolution";
    public static  final String KEY_GEOFENCE_ID = "com.shakeme.sazedul.knockknock.KEY_GEOFENCE_ID";
    public static  final String ACTIVE_GEOFENCE_EMPTY = "EMPTY";

    public static final String DEFAULT_NAME = "New Reminder";

    // The MAX ID of a geofence
    public static final int MAX_ID = 100;

    // The name of the SharedPreferences
    public static final String SHARED_PREFERENCES = "KnockKnockSharedPreferences";

    // Keys for flattened geofences stored in SharedPreferences
    public static final String KEY_NAME = "com.shakeme.sazedul.knockknock.KEY_NAME";
    public static final String KEY_LATITUDE = "com.shakeme.sazedul.knockknock.KEY_LATITUDE";
    public static final String KEY_LONGITUDE = "com.shakeme.sazedul.knockknock.KEY_LONGITUDE";
    public static final String KEY_RADIUS = "com.shakeme.sazedul.knockknock.KEY_RADIUS";
    public static final String KEY_EXPIRATION_DURATION = "com.shakeme.sazedul.knockknock.KEY_EXPIRATION_DURATION";
    public static final String KEY_TRANSITION_TYPE = "com.shakeme.sazedul.knockknock.KEY_TRANSITION_TYPE";
    public static final String KEY_PREFIX = "com.shakeme.sazedul.knockknock.KEY";


    /*
     * Use to set an expiration time for a geofence. After this amount of time Location Services
     * will stop tracking the geofence
     */
    public static final long SECOND_PER_HOUR = 60;
    public static final long MILLISECONDS_PER_SECOND = 1000;

    // Invalid values, used to test geofence storage when retrieving geofences
    public static final long INVALID_LONG_VALUE = -999l;

    public static final float INVALID_FLOAT_VALUE = -999.0f;

    public static final int INVALID_INT_VALUE = -999;

    /*
     * Constants used in verifying the correctness of input values
     */
    public static final double MAX_LATITUDE = 90.d;

    public static final double MIN_LATITUDE = -90.d;

    public static final double MAX_LONGITUDE = 180.d;

    public static final double MIN_LONGITUDE = -180.d;

    public static final float MIN_RADIUS = 1f;

    /*
     * Define a request code to send to Google Play Services. This code is
     * returned in Activity.OnActivityResult
     */
    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    // A string of length 0, used to clear out input fields
    public static final String EMPTY_STRING = new String();

    public static final CharSequence GEOFENCE_ID_DELIMITER = ",";
}
