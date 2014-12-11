package com.shakeme.sazedul.knockknock;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class ListGeofencesActivity extends ActionBarActivity {
    private static final int MAX_ID = 100;
    ArrayAdapter<CharSequence> listGeofenceAdapter;
    ListView mListGeofence;
    // Persistent storage for geofences
    private SimpleGeofenceStore mGeofenceStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_geofences);

        mListGeofence = (ListView) findViewById(R.id.geofence_list);
        for (int i=0; i<MAX_ID; i++) {
            if (MapsActivity.isActiveGeofence(i)) listGeofenceAdapter.add(mGeofenceStorage.getGeofence(Integer.toString(i)).getId());
        }
        mListGeofence.setAdapter(listGeofenceAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list_geofences, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
