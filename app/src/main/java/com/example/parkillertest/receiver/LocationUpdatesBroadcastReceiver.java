package com.example.parkillertest.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.example.parkillertest.R;
import com.example.parkillertest.activity.MainActivity;
import com.example.parkillertest.service.LocationUpdatesService;
import com.example.parkillertest.util.Constants;

/**
 * Created by JuanCarlos on 21/02/16.
 */
public class LocationUpdatesBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Location location = intent.getParcelableExtra(Constants.LOCATION_DATA_EXTRA);

        if(location == null) {
            return;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        String message;

        double lat = Double.valueOf(prefs.getString(Constants.KEY_DESTINATION_LAT, "0.0"));
        double lon = Double.valueOf(prefs.getString(Constants.KEY_DESTINATION_LON, "0.0"));

        if(lat != 0.0 && lon != 0.0) {
            Location destination = new Location("");
            destination.setLatitude(lat);
            destination.setLongitude(lon);

            int distanceTo = (int) location.distanceTo(destination);

            if(distanceTo > Constants.DISTANCE_200M && !prefs.getBoolean(Constants.KEY_200M_MESSAGE_SHOWED, false)) {

                message = context.getString(R.string.distance_higher_than_200);
                editor.putBoolean(Constants.KEY_200M_MESSAGE_SHOWED, true);
                sendNotification(context, message);

            } else if(distanceTo > Constants.DISTANCE_100M && distanceTo <= Constants.DISTANCE_200M && !prefs.getBoolean(Constants.KEY_100M_MESSAGE_SHOWED, false)) {

                message = context.getString(R.string.distance_higher_than_100);
                editor.putBoolean(Constants.KEY_100M_MESSAGE_SHOWED, true);
                sendNotification(context, message);

            } else if(distanceTo > Constants.DISTANCE_50M && distanceTo <= Constants.DISTANCE_100M && !prefs.getBoolean(Constants.KEY_50M_MESSAGE_SHOWED, false)) {

                message = context.getString(R.string.distance_higher_than_50);
                editor.putBoolean(Constants.KEY_50M_MESSAGE_SHOWED, true);
                sendNotification(context, message);

            } else if(distanceTo > Constants.DISTANCE_10M && distanceTo <= Constants.DISTANCE_50M && !prefs.getBoolean(Constants.KEY_10M_MESSAGE_SHOWED, false)) {

                message = context.getString(R.string.distance_higher_than_10);
                editor.putBoolean(Constants.KEY_10M_MESSAGE_SHOWED, true);
                sendNotification(context, message);

            } else if(distanceTo <= Constants.DISTANCE_10M && !prefs.getBoolean(Constants.KEY_ARRIVED_MESSAGE_SHOWED, false)) {

                message = context.getString(R.string.distance_lower_than_10);
                editor.putBoolean(Constants.KEY_ARRIVED_MESSAGE_SHOWED, true);
                sendNotification(context, message);
            }

            editor.apply();
        }
    }

    private void sendNotification(Context context, String message) {
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.cast_ic_notification_1)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());

    }
}
