package com.shakeme.sazedul.knockknock;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;


public class AddGeofenceActivity extends ActionBarActivity {

    // Extra Message prefix
    public static final String PREFIX = "com.shakeme.sazedul.knockknock";

    // Invalid value used to test latitude and longitude storage when retrieving extra message
    public static final float INVALID_FLOAT_VALUE = -999.0f;

    private EditText txtLatitude;
    private EditText txtLongitude;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_geofence);

        // Get the latitude and longitude from the intent
        intent = getIntent();
        txtLatitude = (EditText) findViewById(R.id.txt_latitude);
        txtLongitude = (EditText) findViewById(R.id.txt_longitude);

        if (intent.hasExtra(PREFIX+".latlng")) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    double extra[] = intent.getDoubleArrayExtra(PREFIX+".latlng");

                    txtLatitude.setText(Double.toString(extra[0]));
                    txtLongitude.setText(Double.toString(extra[1]));
                }
            });
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_geofence, menu);
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

    public void showHelpActivity(View view){
        Intent intent = new Intent(this, DetailsHelpActivity.class);
        startActivity(intent);
    }
}
