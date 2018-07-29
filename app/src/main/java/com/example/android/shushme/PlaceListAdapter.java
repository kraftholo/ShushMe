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

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;

public class PlaceListAdapter extends RecyclerView.Adapter<PlaceListAdapter.PlaceViewHolder> {

    private Context mContext;
    private PlaceBuffer mPlaces;     //acts as the dataSource


    public PlaceListAdapter(Context context,PlaceBuffer placeBuffer) {
        this.mContext = context;
        this.mPlaces = placeBuffer;
    }


    @Override
    public PlaceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Get the RecyclerView item layout
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.item_place_card, parent, false);
        return new PlaceViewHolder(view);
    }


    @Override
    public void onBindViewHolder(PlaceViewHolder holder, int position) {
        Place currPlace = mPlaces.get(position);
        holder.nameTextView.setText(currPlace.getName());
        holder.addressTextView.setText(currPlace.getAddress());
    }



    @Override
    public int getItemCount() {
        if(mPlaces==null)return 0;

        return mPlaces.getCount();
    }

    public void swapPlaces(PlaceBuffer newPlaces) {
        this.mPlaces = newPlaces;

        if(mPlaces!=null)this.notifyDataSetChanged();

    }

    /**
     * PlaceViewHolder class for the recycler view item
     */
    class PlaceViewHolder extends RecyclerView.ViewHolder {

        TextView nameTextView;
        TextView addressTextView;

        public PlaceViewHolder(View itemView) {
            super(itemView);
            nameTextView = (TextView) itemView.findViewById(R.id.name_text_view);
            addressTextView = (TextView) itemView.findViewById(R.id.address_text_view);
        }

    }
}
