package com.shakeme.sazedul.knockknock;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;


public class ListGeofencesActivity extends ActionBarActivity
        implements ListView.OnItemLongClickListener,
        ListView.OnItemClickListener {
    private static final int MAX_ID = 100;
    Map<String, String> map;

    ListView mListGeofence;
    // Persistent storage for geofences
    private SimpleGeofenceStore mGeofenceStorage;
    private int checked_count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_geofences);
        Vector<String> list = new Vector<>();
        mGeofenceStorage = new SimpleGeofenceStore(this);
        map = new HashMap<>();

        mListGeofence = (ListView) findViewById(R.id.geofence_list);
        for (int i=0; i<MAX_ID; i++) {
            if (MapsActivity.isActiveGeofence(i)) {
                SimpleGeofence geofence = mGeofenceStorage.getGeofence(Integer.toString(i));
                if (geofence != null) {
                    list.add(geofence.getName());
                    map.put(geofence.getName(), geofence.getId());
                }
            }
        }
        final ArrayAdapter<String> listGeofenceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, list);
        mListGeofence.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        mListGeofence.setAdapter(listGeofenceAdapter);

        mListGeofence.setOnItemClickListener(this);
        mListGeofence.setOnItemLongClickListener(this);
        mListGeofence.setOnItemLongClickListener(this);
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
        else if (id == R.id.action_delete) {
            Intent intentReminder = new Intent();
            intentReminder.putExtra("GeofenceDelete", "Data successfully acquired.");
            SparseBooleanArray checked = mListGeofence.getCheckedItemPositions();

            String checked_position[] = new String[checked_count];

            for (int i=0; i<checked_count; i++) {
                // Item position in adapter
                int position = checked.keyAt(i);
                checked_position[i] = map.get(mListGeofence.getAdapter().getItem(position).toString());
            }
            intentReminder.putExtra("GeofenceIds", checked_position);
            setResult(RESULT_OK, intentReminder);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (checked_count > 0) {
            if (mListGeofence.isItemChecked(position)) {
                mListGeofence.setItemChecked(position, false);
                checked_count--;
            } else {
                mListGeofence.setItemChecked(position, true);
                checked_count++;
            }
        } else {
            Intent intentReminder = new Intent();
            intentReminder.putExtra("GeofenceShow", "Data successfully acquired.");
            intentReminder.putExtra("GeofenceId", map.get(parent.getItemAtPosition(position).toString()));
            setResult(RESULT_OK, intentReminder);
            finish();
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (mListGeofence.isItemChecked(position)) {
            mListGeofence.setItemChecked(position, false);
            checked_count--;
        } else {
            mListGeofence.setItemChecked(position, true);
            checked_count++;
        }
        return false;
    }
}
