package com.example.android.shushme;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class About extends AppCompatActivity {

    Intent mIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }

    public void onTermsOfUseClicked(View view) {
         mIntent = new Intent();
         mIntent.setAction(Intent.ACTION_VIEW);
         Uri uri = Uri.parse("https://maps.google.com/help/terms_maps.html");
         mIntent.setData(uri);
         startActivity(mIntent);
    }

    public void onPrivacyPolicyClicked(View view) {
        mIntent = new Intent();
        mIntent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.parse("https://policies.google.com/privacy");
        mIntent.setData(uri);
        startActivity(mIntent);
    }
}
