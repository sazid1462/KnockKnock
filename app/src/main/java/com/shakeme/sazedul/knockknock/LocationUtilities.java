package com.shakeme.sazedul.knockknock;

/***
 * Created by Sazedul on 01-Dec-14.
 **/

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

/**
 * Defines app-wide constants and utilities
 */
public final class LocationUtilities {

    // Create an empty string for initializing strings
    public static final String EMPTY_STRING = "";

    /**
     * Get the latitude and longitude from the Location object returned by
     * Location Services.
     *
     * @param currentLocation A Location object containing the current location
     * @return The latitude and longitude of the current location, or null if no
     * location is available.
     */
    public static String getLatLngString(Location currentLocation) {
        // If the location is valid
        if (currentLocation != null) {

            // Return the latitude and longitude as strings
            return Double.toString(currentLocation.getLatitude()) + " , " + Double.toString(currentLocation.getLongitude());
        } else {

            // Otherwise, return the empty string
            return EMPTY_STRING;
        }
    }

    public static LatLng getLatLng(Location currentLocation) {
        // If the location is valid
        if (currentLocation != null) {

            // Return the latitude and longitude as strings
            return new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        } else {

            // Otherwise, return the empty string
            return new LatLng(0, 0);
        }
    }
}

