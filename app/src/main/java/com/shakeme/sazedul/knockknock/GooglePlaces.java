package com.shakeme.sazedul.knockknock;

/**
 * Created by Sazedul on 06-Dec-14.
 */

import android.util.Log;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;

import org.apache.http.client.HttpResponseException;

public class GooglePlaces {

    private double _latitude;
    private double _longitude;
    private double _radius;

    /**
     * Searching places
     * @param latitude - latitude of place
     * @params longitude - longitude of place
     * @param radius - radius of searchable area
     * @param types - type of place to search
     * @return list of places
     * */
    public PlaceList search(double latitude, double longitude, double radius, String types)
            throws Exception {

        this._latitude = latitude;
        this._longitude = longitude;
        this._radius = radius;

        try {

            HttpRequestFactory httpRequestFactory = createRequestFactory(GeofenceUtils.HTTP_TRANSPORT);
            HttpRequest request = httpRequestFactory
                    .buildGetRequest(new GenericUrl(GeofenceUtils.PLACES_SEARCH_URL));
            request.getUrl().put("key", GeofenceUtils.API_KEY);
            request.getUrl().put("location", _latitude + "," + _longitude);
            request.getUrl().put("radius", _radius); // in meters
            request.getUrl().put("sensor", "false");
            if(types != null)
                request.getUrl().put("types", types);

            PlaceList list = request.execute().parseAs(PlaceList.class);
            // Check log cat for places response status
            Log.d("Places Status", "" + list.status);
            return list;

        } catch (HttpResponseException e) {
            Log.e("Error:", e.getMessage());
            return null;
        }

    }

    /**
     * Searching single place full details
     * @param reference - reference id of place
     *                 - which you will get in search api request
     * */
    public PlaceDetails getPlaceDetails(String reference) throws Exception {
        try {

            HttpRequestFactory httpRequestFactory = createRequestFactory(GeofenceUtils.HTTP_TRANSPORT);
            HttpRequest request = httpRequestFactory
                    .buildGetRequest(new GenericUrl(GeofenceUtils.PLACES_DETAILS_URL));
            request.getUrl().put("key", GeofenceUtils.API_KEY);
            request.getUrl().put("reference", reference);
            request.getUrl().put("sensor", "false");

            PlaceDetails place = request.execute().parseAs(PlaceDetails.class);

            return place;

        } catch (HttpResponseException e) {
            Log.e("Error in Perform Details", e.getMessage());
            throw e;
        }
    }

    /**
     * Creating http request Factory
     * */
    public static HttpRequestFactory createRequestFactory(
            final HttpTransport transport) {
        return transport.createRequestFactory(new HttpRequestInitializer() {
            public void initialize(HttpRequest request) {
                JsonObjectParser parser = new JsonObjectParser(new JacksonFactory());
                request.setParser(parser);
            }
        });
    }
}
