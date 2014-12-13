package com.shakeme.sazedul.knockknock;

import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;


public class NotificationActivity extends ActionBarActivity {

    RingtoneManager ringer;
    Intent intent;
    // Vibrate the mobile phone
    Vibrator vibrator;
    String geoIds[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        intent = getIntent();
        ringer = new RingtoneManager(getApplicationContext());
        vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate the mobile phone and play ringtone
        ringer.getRingtone(ringer.getCursor().getPosition()).play();
        // Vibration pattern in milliseconds
        long pattern[] = {1000, 200, 1000, 200, 500, 100, 500, 100, 1000};
        vibrator.cancel();
        vibrator.vibrate(pattern, 0);

        geoIds = intent.getStringArrayExtra(GeofenceUtils.EXTRA_NOTIFICATION_IDS);
        String ids = TextUtils.join(GeofenceUtils.GEOFENCE_ID_DELIMITER, geoIds);
        String transitionType = intent.getStringExtra(GeofenceUtils.EXTRA_NOTIFICATION_TRANSITION);

        TextView txtDescription = (TextView) findViewById(R.id.lbl_description);
        if (transitionType.matches(getString(R.string.geofence_transition_entered)))
            txtDescription.setText("You have entered into geofence with ids "+ids);
        else txtDescription.setText("You have exited from geofence with ids "+ids);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_notification, menu);
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

    public void shutUp(View view) {
        ringer.stopPreviousRingtone();
        vibrator.cancel();

        Intent intentApp = new Intent(this, MapsActivity.class);
        intentApp.addCategory(GeofenceUtils.CATEGORY_START_FROM_NOTIFICATION);
        intentApp.putExtra(GeofenceUtils.EXTRA_NOTIFICATION_IDS, geoIds);
        startActivity(intentApp);
        finish();
    }
}
