package com.shakeme.sazedul.knockknock;

/***
 * Created by Sazedul on 01-Dec-14.
 **/

import android.app.ProgressDialog;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

public class MapsActivity extends LocationDetector implements LocationListener {

    private static final String TAG = "MapActivity";
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private TextView mCurrentLocation;
    private ProgressBar mActivityIndicator;

    // flag for Internet connection status
    Boolean isInternetPresent = false;

    // Connection detector class
    NetworkConnectivityDetector nCDetector;

    // Alert Dialog Manager
    MessageDialogueViewer alert = new MessageDialogueViewer();

    // Google Places
    GooglePlaces googlePlaces;

    // Places List
    PlaceList nearPlaces;

    // Progress dialog
    ProgressDialog pDialog;

    // ListItems data
    Place place;

    // KEY Strings
    public static String KEY_REFERENCE = "reference"; // id of the place
    public static String KEY_NAME = "name"; // name of the place
    public static String KEY_VICINITY = "vicinity"; // Place area name

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
        mCurrentLocation = (TextView) findViewById(R.id.txt_current_location);
        mActivityIndicator = (ProgressBar) findViewById(R.id.cur_location_progress);
        Button mAddNew = (Button)findViewById(R.id.btn_add_geofences);
        nCDetector = new NetworkConnectivityDetector(getApplicationContext());

        mAddNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
            }
        });
    }

    /**
     * Called when activity gets invisible. Connection to Play Services needs to
     * be disconnected as soon as an activity is invisible.
     */
    @Override
    protected void onStop() {
        // If the client is connected
        if (mLocationClient.isConnected()) {
            /*
             * Remove location updates for a listener.
             * The current Activity is the listener, so
             * the argument is "this".
             */
            mLocationClient.removeLocationUpdates(this);
        }
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check if Internet present
        isInternetPresent = nCDetector.isConnectionToInternetAvailable();
        if (!isInternetPresent) {
            // Internet Connection is not present
            alert.showAlertDialog(this, "Internet Connection Error",
                    "Please connect to the Internet", false);
            // stop executing code by return
            return;
        }
        setUpMapIfNeeded();
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
     * Called when {@code mGoogleApiClient} is connected.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "LocationClient connected");
        if (mUpdatesRequested) {
            mLocationClient.requestLocationUpdates(mLocationRequest, this);
        }
    }

    @Override
    public void onDisconnected() {
        Log.i(TAG, "LocationClient disconnected");

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
                new LoadPlaces().execute();
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                // TODO
                //getAddress();
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        String msg = "Updated Location: " + LocationUtilities.getLatLngString(location);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        //getAddress();
    }

    class LoadPlaces extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mActivityIndicator.setVisibility(View.VISIBLE);
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
                // type 'geocode' is used here
                String types = "geocode"; // Listing places only geocoding results

                // Radius in meters - increase this value if you don't find any places
                double radius = 10; // 1000 meters

                // get nearest places
                nearPlaces = googlePlaces.search(getCurrentLocation().getLatitude(),
                        getCurrentLocation().getLongitude(), radius, types);


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
            mActivityIndicator.setVisibility(View.GONE);
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed Places into LISTVIEW
                     * */
                    // Get json response status
                    String status = nearPlaces.status;

                    // Check for all possible status
                    if(status.equals("OK")){
                        // Successfully got places details
                        if (nearPlaces.results != null) {
                            // loop through each place
                            place = nearPlaces.results.get(0);
                            mCurrentLocation.setText(place.name);
                        }
                    }
                    else {
                        // Zero results found
                        alert.showAlertDialog(MapsActivity.this, "Knock Knock",
                                "Can not retrieve place name.",
                                false);
                    }
                }
            });

        }

    }
}
