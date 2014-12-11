package com.shakeme.sazedul.knockknock;

import android.app.IntentService;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

import java.util.List;

/**
 * Created by Sazedul on 10-Dec-14.
 */
public class ReceiveGeofenceTransitionsIntentService extends IntentService {
    /**\
     * Sets an identifier for the service
     */
    public ReceiveGeofenceTransitionsIntentService() {
        super("ReceiveGeofenceTransitionsIntentService");
    }

    /**
     * Handles incoming intents
     * @param intent The Intent sent by Location Services. This Intent is provided
     * to Location Services (inside a PendingIntent) when you call addGeofences()
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        // First check for errors
        if (LocationClient.hasError(intent)) {
            // Get the error code with a static method
            int errorCode = LocationClient.getErrorCode(intent);
            // Log the error
            Log.e("ReceiveGeofenceTransitionsIntentService", "Location Services error: " + Integer.toString(errorCode));
            /*
             * You can also send the error code to an Activity or Fragment with a broadcast Intent
             */
        /*
         * If there's no error, get the transition type and the IDs of the geofence or geofences that triggered the transition
         */
        } else {
            // Get the type of transition (entry or exit)
            int transitionType = LocationClient.getGeofenceTransition(intent);
            // Test that a valid transition was reported
            if ((transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) ||
                    (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT)) {
                List<Geofence> triggerList = LocationClient.getTriggeringGeofences(intent);

                String[] triggerIds = new String[triggerList.size()];
                for (int i=0; i<triggerIds.length; i++) {
                    // Store the Id of each geofence
                    triggerIds[i] = triggerList.get(i).getRequestId();
                }
                MapsActivity.setTriggeringGeofences(triggerIds, triggerList);
                if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) {
                    MessageDialogueViewer alert = new MessageDialogueViewer();
                    alert.showAlertDialog(this, "Geofence Transition", "You have entered the area", true, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                }
                else {
                    MessageDialogueViewer alert = new MessageDialogueViewer();
                    alert.showAlertDialog(this, "Geofence Transition", "You have exited the area", true, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                }
            // An invalid transition was reported
            } else {
                Log.e("ReceivedTransitionsIntentService", "Geofence transition error: "+Integer.toString(transitionType));
            }
        }
    }
}
