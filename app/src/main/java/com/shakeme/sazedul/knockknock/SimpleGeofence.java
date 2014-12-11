package com.shakeme.sazedul.knockknock;

import com.google.android.gms.location.Geofence;

/**
 * Created by Sazedul on 08-Dec-14.
 */
public class SimpleGeofence {
    // Instance variables
    private final String mId;
    private final String mName;
    private final Double mLatitude;
    private final Double mLongitude;
    private final float mRadius;
    private long mExpirationDuration;
    private int mTransitionType;

    /**
     *
     * @param mId The Geofence's request ID
     * @param mLatitude Latitude of the Geofence's center
     * @param mLongitude Longitude of the Geofence's center
     * @param mRadius Radius of the Geofence circle
     * @param mExpirationDuration Geofence expiration duration
     * @param mTransitionType Type of Geofence transition
     */
    public SimpleGeofence(String mId, String mName, Double mLatitude, Double mLongitude, float mRadius, long mExpirationDuration, int mTransitionType) {
        // Set the instance fields from the constructor
        this.mId = mId;
        this.mName = mName;
        this.mLatitude = mLatitude;
        this.mLongitude = mLongitude;
        this.mRadius = mRadius;
        this.mExpirationDuration = mExpirationDuration;
        this.mTransitionType = mTransitionType;
    }

    // Instance field getters
    public String getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public Double getLatitude() {
        return mLatitude;
    }

    public Double getLongitude() {
        return mLongitude;
    }

    public float getRadius() {
        return mRadius;
    }

    public long getExpirationDuration() {
        return mExpirationDuration;
    }

    public int getTransitionType() {
        return mTransitionType;
    }

    /**
     * Creates a Location Services Geofence object from a SimpleGeofence.
     *
     * @return A Geofence object
     */
    public Geofence toGeofence() {
        // Build a new Geofence object
        return new Geofence.Builder()
                .setRequestId(getId())
                .setCircularRegion(getLatitude(), getLongitude(), getRadius())
                .setExpirationDuration(getExpirationDuration())
                .setTransitionTypes(getTransitionType())
                .build();
    }
}
