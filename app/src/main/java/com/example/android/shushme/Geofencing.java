package com.example.android.shushme;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;

import java.util.ArrayList;
import java.util.List;

public class Geofencing {


    private static long GEOFENCE_EXPIRATION_DURATION = 1000 * 60 * 60 * 24;        //1 day
    private static float GEOEFENCE_RADIUS = 100;                            //Radius in metres
    private List<Geofence> mGeofencesList;
    private Context mContext;
    private GoogleApiClient mGoogleApiClient;
    private PendingIntent mPendingIntent;


    public Geofencing(Context context, GoogleApiClient googleApiClient) {
        this.mContext = context;
        this.mGoogleApiClient = googleApiClient;
    }

    public void registerAllGeofences() {

        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.GeofencingApi.addGeofences(mGoogleApiClient,
                getGeoFencingRequestList(),
                getGeofencePendingIntent());
    }

    public void unRegisterAllGeofences(){
        LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient,getGeofencePendingIntent());
    }

    //creates geofence obj for each place and adds to the mGeofencesList
    public void updateGeofencesList(PlaceBuffer places){

        mGeofencesList= new ArrayList<>();

        if(places==null||places.getCount()==0)return;

        for(Place place: places){
            String placeID = place.getId();
            Double latitude= place.getLatLng().latitude;
            Double longitude= place.getLatLng().longitude;

            Geofence geofenceObj = new Geofence.Builder()
                                            .setRequestId(placeID)
                                            .setExpirationDuration(GEOFENCE_EXPIRATION_DURATION)
                                            .setCircularRegion(latitude,longitude,GEOEFENCE_RADIUS)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER|Geofence.GEOFENCE_TRANSITION_EXIT)
                                            .build();

            mGeofencesList.add(geofenceObj);
        }

    }



    //These helper methods will be used by registerAllGeofences() and unRegisterAllGeofences() s

    //HELPER METHOD--returns geofencingrequest from the mGeofencesList
    private GeofencingRequest getGeoFencingRequestList(){

        return new GeofencingRequest.Builder()
                .addGeofences(mGeofencesList)
                .setInitialTrigger(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build();

    }

    //HELPER METHOD--returns a pendingIntent for the GeofenceBroadcastReceiver Class
    private PendingIntent getGeofencePendingIntent(){

        if(mPendingIntent!=null)return mPendingIntent;

        Intent intent = new Intent(mContext,GeofenceBroadcastReceiver.class);

        mPendingIntent = PendingIntent.getBroadcast(mContext,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);

        return mPendingIntent;

    }


}
