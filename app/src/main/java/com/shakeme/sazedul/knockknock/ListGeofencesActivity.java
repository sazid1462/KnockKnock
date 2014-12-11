package com.shakeme.sazedul.knockknock;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.Vector;


public class ListGeofencesActivity extends ActionBarActivity {
    private static final int MAX_ID = 100;

    ListView mListGeofence;
    // Persistent storage for geofences
    private SimpleGeofenceStore mGeofenceStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_geofences);
        Vector<String> list = new Vector<>();
        mGeofenceStorage = new SimpleGeofenceStore(this);

        mListGeofence = (ListView) findViewById(R.id.geofence_list);
        for (int i=0; i<MAX_ID; i++) {
            if (MapsActivity.isActiveGeofence(i)) {
                System.out.println("FUCK");
                SimpleGeofence geofence = mGeofenceStorage.getGeofence(Integer.toString(i));
                if (geofence != null) {
                    System.out.println(geofence.getId());
                    list.add(geofence.getId());
                }
            }
        }
        final ArrayAdapter<String> listGeofenceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_checked, list);
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
