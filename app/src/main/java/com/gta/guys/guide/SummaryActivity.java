package com.gta.guys.guide;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.scary.teacher.guide.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

public class SummaryActivity extends AppCompatActivity {

    private String summary;
    private String data;
    private String photo;

    private TextView txtSummary;
    private ImageView imgSummary, btnNext;

    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getIntent().getStringExtra("name"));

        summary = getIntent().getStringExtra("summary");
        data = getIntent().getStringExtra("data");
        photo = getIntent().getStringExtra("photo");

        View adContainer = findViewById(R.id.adMobView);
        txtSummary = findViewById(R.id.txtSummary);
        imgSummary = findViewById(R.id.summaryImg);
        btnNext = findViewById(R.id.btnNext);

        txtSummary.setText(summary);

        AssetManager am = getAssets();
        try {
            InputStream is = am.open(photo);
            Drawable d = Drawable.createFromStream(is, null);

            Glide
                    .with(this)
                    .load(d)
                    .centerCrop()
                    .into(imgSummary);
        } catch (IOException e) {

        }

        AdView mAdView = new AdView(this);
        mAdView.setAdSize(AdSize.BANNER);
        mAdView.setAdUnitId(StartActivity.BANNER_ID);
        ((RelativeLayout)adContainer).addView(mAdView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                btnNext.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                btnNext.setVisibility(View.VISIBLE);
                // Code to be executed when an ad request fails.

            }

        });

        Random r = new Random();
        int ads = r.nextInt(100);

        if (ads >= StartActivity.PERCENT_SHOW_BANNER_AD){
            mAdView.destroy();
            mAdView.setVisibility(View.GONE);
        }

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(StartActivity.INTER_ID);
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                loadAds();
            }

            @Override
            public void onAdFailedToLoad(int i) {

            }

        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SummaryActivity.this, DetailActivity.class);
                intent.putExtra("data", data);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                Random r = new Random();
                int ads = r.nextInt(100);

                if (ads < StartActivity.PERCENT_SHOW_INTER_AD){
                    showInterstitial();
                }
            }
        });


    }

    private void loadAds() {
        if (!mInterstitialAd.isLoading() && !mInterstitialAd.isLoaded()) {
            AdRequest adRequest = new AdRequest.Builder().build();
            mInterstitialAd.loadAd(adRequest);
        }

    }

    private void showInterstitial() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            if(isOnline()){
                loadAds();
            } else {
                Toast.makeText(this, "Please check network connection!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        }
        catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }

        return false;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}