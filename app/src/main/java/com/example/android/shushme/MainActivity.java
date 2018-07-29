package com.example.android.shushme;

/*
* Copyright (C) 2017 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*  	http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.example.android.shushme.provider.PlaceContract;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


    public static final String TAG = MainActivity.class.getSimpleName();

    private static final int PERMISSIONS_REQUEST_FINE_LOCATION = 100 ;
    private static final int PLACE_PICKER_REQUEST =200;

    private PlaceListAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private GoogleApiClient mClient;
    private Geofencing mGeofencing;
    private boolean mSwitchIsEnabled;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mRecyclerView = (RecyclerView) findViewById(R.id.places_list_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new PlaceListAdapter(this,null);
        mRecyclerView.setAdapter(mAdapter);

        mClient = new GoogleApiClient.Builder(this)
                                            .addConnectionCallbacks(this)
                                            .addOnConnectionFailedListener(this)
                                            .addApi(LocationServices.API)
                                            .addApi(Places.GEO_DATA_API)
                                            .enableAutoManage(this,this)
                                            .build();

        mGeofencing = new Geofencing(this,mClient);


        //see whether the Geofencing switch is turned on or off
        Switch onOffSwitch = (Switch) findViewById(R.id.enable_switch);

        //value of switch on APP LAUNCH
        mSwitchIsEnabled = getPreferences(MODE_PRIVATE).getBoolean(getString(R.string.setting_enabled),false);
        onOffSwitch.setChecked(mSwitchIsEnabled);

        //on flipping the switch, write it to the sharedPref
        onOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
                editor.putBoolean(getString(R.string.setting_enabled),isChecked);
                mSwitchIsEnabled = isChecked;
                editor.commit();

                if (mSwitchIsEnabled) mGeofencing.registerAllGeofences();
                else mGeofencing.unRegisterAllGeofences();

            }
        });


    }

    //my add button, check if location permission granted or not ... if yes, then launch PlacePicker
    public void onAddPlaceButtonClicked(View view) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, getString(R.string.need_location_permission_message), Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(this, getString(R.string.location_permissions_granted_message), Toast.LENGTH_LONG).show();

        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        Intent intent = null;
        try {
            intent = builder.build(this);
        } catch (GooglePlayServicesRepairableException e) {
            Log.e(TAG, "onAddPlaceButtonClicked: "+e );
        } catch (GooglePlayServicesNotAvailableException e) {
            Log.e(TAG, "onAddPlaceButtonClicked: "+e );
        }
        startActivityForResult(intent,PLACE_PICKER_REQUEST);

    }

    //called after PlacePickerRequest is complete
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==PLACE_PICKER_REQUEST && resultCode == RESULT_OK){

            Place place = PlacePicker.getPlace(this,data);

            if(place==null){
                Log.i(TAG, "onActivityResult: No Place Selected");
                return;
            }

            String placeName = place.getName().toString();
            String placeAddress = place.getAddress().toString();
            String placeID = place.getId();


            ContentValues cv = new ContentValues();
            cv.put(PlaceContract.PlaceEntry.COLUMN_PLACE_ID,placeID);
            getContentResolver().insert(PlaceContract.PlaceEntry.CONTENT_URI,cv);

            refreshPlacesData(); //also called after my GoogleApiClient connects

        }

    }



    //my checkbox clicked, ask for location permissions
    public void onLocationPermissionClicked(View view) {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSIONS_REQUEST_FINE_LOCATION);
    }

    @Override
    protected void onResume() {
        super.onResume();

        CheckBox locationPermissions = (CheckBox) findViewById(R.id.enable_location_checkbox);

        if(ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            locationPermissions.setChecked(false);
        }else{
            locationPermissions.setChecked(true);
            locationPermissions.setEnabled(false);
        }


    }

    private void refreshPlacesData(){
        Cursor cursor = getContentResolver().query(PlaceContract.PlaceEntry.CONTENT_URI
                                                    ,null,null,null,null);

        if(cursor==null||!cursor.moveToFirst())return;

        ArrayList<String> placeIDs = new ArrayList<>();
        while (cursor.moveToNext()){
            String placeID = cursor.getString(cursor.getColumnIndex(PlaceContract.PlaceEntry.COLUMN_PLACE_ID));
            
            placeIDs.add(placeID);
        }

        cursor.close();

        PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mClient,
                                                                            placeIDs.toArray(new String[placeIDs.size()]));


        placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
            @Override
            public void onResult(@NonNull PlaceBuffer places) {
                mAdapter.swapPlaces(places);

                //if geofencing is switched on .. a change in places should be reflected in the geofencingList
                mGeofencing.updateGeofencesList(places);
                if (mSwitchIsEnabled) mGeofencing.registerAllGeofences();
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        refreshPlacesData();
        Log.d(TAG, "onConnected: connection successful!");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended: connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed: connection failed :( ");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.about,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemID = item.getItemId();
        switch (itemID){
            case R.id.about:
                Intent startAboutAct= new Intent(this,About.class);
                startActivity(startAboutAct);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
