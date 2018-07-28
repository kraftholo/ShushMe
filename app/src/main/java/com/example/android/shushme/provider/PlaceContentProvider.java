package com.example.android.shushme.provider;

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

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import static com.example.android.shushme.provider.PlaceContract.PlaceEntry;


public class PlaceContentProvider extends ContentProvider {


    public static final int PLACES = 100;
    public static final int PLACE_WITH_ID = 101;


    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private static final String TAG = PlaceContentProvider.class.getName();


    public static UriMatcher buildUriMatcher() {

        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(PlaceContract.AUTHORITY, PlaceContract.PATH_PLACES, PLACES);
        uriMatcher.addURI(PlaceContract.AUTHORITY, PlaceContract.PATH_PLACES + "/#", PLACE_WITH_ID);
        return uriMatcher;
    }


    private PlaceDbHelper mPlaceDbHelper;

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mPlaceDbHelper = new PlaceDbHelper(context);
        return true;
    }


    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = mPlaceDbHelper.getWritableDatabase();


        int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case PLACES:

                long id = db.insert(PlaceEntry.TABLE_NAME, null, values);
                if (id > 0) {
                    returnUri = ContentUris.withAppendedId(PlaceContract.PlaceEntry.CONTENT_URI, id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }


        getContext().getContentResolver().notifyChange(uri, null);


        return returnUri;
    }


    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {


        final SQLiteDatabase db = mPlaceDbHelper.getReadableDatabase();


        int match = sUriMatcher.match(uri);
        Cursor retCursor;

        switch (match) {

            case PLACES:
                retCursor = db.query(PlaceEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }


        retCursor.setNotificationUri(getContext().getContentResolver(), uri);


        return retCursor;
    }


    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = mPlaceDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);

        int placesDeleted;
        switch (match) {

            case PLACE_WITH_ID:

                String id = uri.getPathSegments().get(1);

                placesDeleted = db.delete(PlaceEntry.TABLE_NAME, "_id=?", new String[]{id});
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (placesDeleted != 0) {

            getContext().getContentResolver().notifyChange(uri, null);
        }

        return placesDeleted;
    }


    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        final SQLiteDatabase db = mPlaceDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);

        int placesUpdated;

        switch (match) {
            case PLACE_WITH_ID:

                String id = uri.getPathSegments().get(1);

                placesUpdated = db.update(PlaceEntry.TABLE_NAME, values, "_id=?", new String[]{id});
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (placesUpdated != 0) {

            getContext().getContentResolver().notifyChange(uri, null);
        }

        return placesUpdated;
    }


    @Override
    public String getType(@NonNull Uri uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
