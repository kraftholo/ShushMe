package com.example.android.shushme;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = GeofenceBroadcastReceiver.class.getSimpleName();


    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        if(geofenceTransition== Geofence.GEOFENCE_TRANSITION_ENTER){
            setRingerMode(context,AudioManager.RINGER_MODE_SILENT);
            sendNotification(context,geofenceTransition);

        }else if(geofenceTransition== Geofence.GEOFENCE_TRANSITION_EXIT){
            setRingerMode(context,AudioManager.RINGER_MODE_NORMAL);
            sendNotification(context,geofenceTransition);

        }else{
            Log.e(TAG, String.format("Unknown Transition : %d",geofenceTransition));

            return;
        }
    }


    //HELPER METHOD -- called when i want to change the ringer mode
    private void setRingerMode(Context context,int mode){

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        //if <24 ie less than nougat , no need to check for NotificationPolicyGranted
        if (nm != null && (Build.VERSION.SDK_INT < 24 ||
                (Build.VERSION.SDK_INT >= 24 && !nm.isNotificationPolicyAccessGranted()))) {
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            audioManager.setRingerMode(mode);
        }
    }
    private void sendNotification(Context context,int transitionType){

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        if(transitionType==Geofence.GEOFENCE_TRANSITION_ENTER){
            builder.setSmallIcon(R.drawable.ic_volume_off_white_24dp)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.ic_volume_off_white_24dp))
                    .setContentTitle(context.getString(R.string.silent_mode_activated));

        }else if(transitionType==Geofence.GEOFENCE_TRANSITION_EXIT){
            builder.setSmallIcon(R.drawable.ic_volume_up_white_24dp)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.ic_volume_up_white_24dp))
                    .setContentTitle(context.getString(R.string.back_to_normal));
        }

        builder.build();
    }
}
