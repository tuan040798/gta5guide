package com.gta.guys.guide;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;


import com.daimajia.numberprogressbar.NumberProgressBar;
import com.daimajia.numberprogressbar.OnProgressBarListener;
import com.gta.guys.guide.utils.BackUpModel;
import com.gta.guys.guide.utils.HttpHandler;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.onjmrveaco.adx.service.InterstitialAdsManager;
import com.scary.teacher.guide.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import vn.aib.ratedialog.RatingDialog;

public class StartActivity extends AppCompatActivity implements OnProgressBarListener {


    private BackUpModel backUpModel;
    private String TAG = MainActivity.class.getSimpleName();
//    public static String NATIVE_AD_ID = "ca-app-pub-3940256099942544/2247696110";
//    public static String INTER_ID = "ca-app-pub-3940256099942544/1033173712";
//    public static String BANNER_ID = "ca-app-pub-3940256099942544/6300978111";

    public static String NATIVE_AD_ID = "ca-app-pub-2077548239432877/4209281160";
    public static String INTER_ID = "ca-app-pub-2077548239432877/5214584533";
    public static String BANNER_ID = "ca-app-pub-2077548239432877/6304111400";

    public static int PERCENT_SHOW_BANNER_AD = 100;
    public static int PERCENT_SHOW_INTER_AD = 100;
    public static int PERCENT_SHOW_NATIVE_AD = 100;
    public static int NUMBER_OF_NATIVE_AD = 1;

    ImageView btnStart;
    private InterstitialAdsManager adsManager;

    private Timer timer;

    private NumberProgressBar bnp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        try {
            Void aVoid = new GetBackUp().execute().get();
            if(backUpModel != null){
                if(!backUpModel.isLive){
                    new AlertDialog.Builder(StartActivity.this)
                            .setTitle("Notice from developer")
                            .setMessage("Please update the new application to continue using it. We are really sorry for this issue.")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    showApp(StartActivity.this, backUpModel.newAppPackage);
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setCancelable(false)
                            .show();
                }
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        adsManager = new InterstitialAdsManager();
        adsManager.init(false, this, INTER_ID, "#000000", getString(R.string.app_name));

        refreshAd(true, true);

        SharedPreferences prefs = getSharedPreferences("rate_dialog", MODE_PRIVATE);

        Boolean rated = prefs.getBoolean("rate", false);
        if(!rated){
            showRateDialog();
        }

        bnp = (NumberProgressBar)findViewById(R.id.numberbar1);
        bnp.setOnProgressBarListener(this);
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        bnp.incrementProgressBy(10);
                    }
                });
            }
        }, 2000, 100);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                View adContainer = findViewById(R.id.adMobView);

                AdView mAdView = new AdView(StartActivity.this);
                mAdView.setAdSize(AdSize.MEDIUM_RECTANGLE);
                mAdView.setAdUnitId(BANNER_ID);

                ((RelativeLayout)adContainer).addView(mAdView);
                AdRequest adRequest = new AdRequest.Builder().build();
                mAdView.loadAd(adRequest);

                mAdView.setAdListener(new AdListener() {
                    @Override
                    public void onAdLoaded() {
                        // Code to be executed when an ad finishes loading.
                        btnStart.setVisibility(View.VISIBLE);
                        bnp.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAdFailedToLoad(int errorCode) {
                        // Code to be executed when an ad request fails.
                        btnStart.setVisibility(View.VISIBLE);
                        bnp.setVisibility(View.GONE);

                    }

                });

                Random r = new Random();
                int ads = r.nextInt(100);

                if (ads >= PERCENT_SHOW_BANNER_AD){
                    mAdView.destroy();
                    mAdView.setVisibility(View.GONE);
                }

            }
        },500);

        btnStart = findViewById(R.id.btn_start);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(StartActivity.this, MainActivity.class));
            }
        });
    }


    private void showRateDialog() {
        RatingDialog ratingDialog = new RatingDialog(this);
        ratingDialog.setRatingDialogListener(new RatingDialog.RatingDialogInterFace() {
            @Override
            public void onDismiss() {
            }

            @Override
            public void onSubmit(float rating) {
                rateApp(StartActivity.this);
                SharedPreferences.Editor editor = getSharedPreferences("rate_dialog", MODE_PRIVATE).edit();
                editor.putBoolean("rate", true);
                editor.commit();
            }

            @Override
            public void onRatingChanged(float rating) {
            }
        });
        ratingDialog.showDialog();
    }

    public static void rateApp(Context context) {
        Intent intent = new Intent(new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://play.google.com/store/apps/details?id=" + context.getPackageName())));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
    }

    @Override
    public void onProgressChange(int current, int max) {
        if(current == max) {
//            bnp.setProgress(0);
            bnp.setVisibility(View.GONE);
            btnStart.setVisibility(View.VISIBLE);
        }
    }

    private class GetBackUp extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();
            String url = "https://raw.githubusercontent.com/scaryguidegame/gta5guide/master/backupdata.json";
            String jsonStr = sh.makeServiceCall(url);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    String appPackage = jsonObj.getString("appPackage");
                    Boolean isLive = jsonObj.getBoolean("isLive");
                    String newAppPackage = jsonObj.getString("newAppPackage");
                    Boolean isAdsShow = jsonObj.getBoolean("isAdsShow");
                    String inter = jsonObj.getString("inter");
                    String fb_inter = jsonObj.getString("fb_inter");
                    Boolean isShowGG = jsonObj.getBoolean("isShowGG");
                    String banner = jsonObj.getString("banner");
                    String nativeAd = jsonObj.getString("nativeAd");
                    String rewarded = jsonObj.getString("rewarded");
                    int percent_banner = jsonObj.getInt("percent_banner");
                    int percent_inter = jsonObj.getInt("percent_inter");
                    int percent_native = jsonObj.getInt("percent_native");
                    int numberNativeAd = jsonObj.getInt("numberNativeAd");

                    backUpModel = new BackUpModel();
                    backUpModel.appPackage = appPackage;
                    backUpModel.isLive = isLive;
                    backUpModel.newAppPackage = newAppPackage;
                    backUpModel.isAdsShow = isAdsShow;
                    backUpModel.inter = inter;
                    backUpModel.fb_inter = fb_inter;
                    backUpModel.isShowGG = isShowGG;
                    backUpModel.banner = banner;
                    backUpModel.nativeAd = nativeAd;
                    backUpModel.rewarded = rewarded;
                    backUpModel.percent_banner = percent_banner;
                    backUpModel.percent_inter = percent_inter;
                    backUpModel.percent_native = percent_native;
                    backUpModel.numberNativeAd = numberNativeAd;

                    INTER_ID = backUpModel.inter;
                    NATIVE_AD_ID = backUpModel.nativeAd;
                    BANNER_ID = backUpModel.banner;
                    PERCENT_SHOW_BANNER_AD = backUpModel.percent_banner;
                    PERCENT_SHOW_INTER_AD = backUpModel.percent_inter;
                    PERCENT_SHOW_NATIVE_AD = backUpModel.percent_native;
                    NUMBER_OF_NATIVE_AD = backUpModel.numberNativeAd;

                } catch (final JSONException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                        }
                    });

                }

            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    }
                });
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }

    public static void showApp(Context context, String pkg) {
        Intent intent = new Intent(new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://play.google.com/store/apps/details?id=" + pkg)));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adsManager != null)
            adsManager.onResume();
    }

    private void refreshAd(boolean requestUnifiedNativeAds,
                           boolean requestCustomTemplateAds) {
        if (!requestUnifiedNativeAds && !requestCustomTemplateAds) {
//            Toast.makeText(getContext(), "At least one ad format must be checked to request an ad.",
//                    Toast.LENGTH_SHORT).show();
            return;
        }

        AdLoader.Builder builder = new AdLoader.Builder(StartActivity.this, StartActivity.NATIVE_AD_ID);

        if (requestUnifiedNativeAds) {
            builder.forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
                @Override
                public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                    // You must call destroy on old ads when you are done with them,
                    // otherwise you will have a memory leak.
                    RecyclerViewFragment.nativeAd = unifiedNativeAd;
//                    FrameLayout frameLayout =
//                            rootV.findViewById(R.id.fl_adplaceholder);
//                    UnifiedNativeAdView adView = (UnifiedNativeAdView) getLayoutInflater()
//                            .inflate(R.layout.ad_unified_full, null);
//                    ImageView btnCancelNative = adView.findViewById(R.id.btnCancel);
//                    btnCancelNative.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            nativeAd.destroy();
//                            frameLayout.setVisibility(View.GONE);
//                        }
//                    });
//                    populateUnifiedNativeAdView(unifiedNativeAd, adView);
//                    frameLayout.removeAllViews();
//                    frameLayout.addView(adView);
                }

            });
        }

//        if (requestCustomTemplateAds) {
//            builder.forCustomTemplateAd(StartActivity.NATIVE_AD_ID,
//                    new NativeCustomTemplateAd.OnCustomTemplateAdLoadedListener() {
//                        @Override
//                        public void onCustomTemplateAdLoaded(NativeCustomTemplateAd ad) {
//                            FrameLayout frameLayout = rootV.findViewById(R.id.fl_adplaceholder);
//                            View adView = getLayoutInflater()
//                                    .inflate(R.layout.ad_simple_custom_template, null);
//                            populateSimpleTemplateAdView(ad, adView);
//                            frameLayout.removeAllViews();
//                            frameLayout.addView(adView);
//                        }
//                    },
//                    new NativeCustomTemplateAd.OnCustomClickListener() {
//                        @Override
//                        public void onCustomClick(NativeCustomTemplateAd ad, String s) {
////                            Toast.makeText(getContext(),
////                                    "A custom click has occurred in the simple template",
////                                    Toast.LENGTH_SHORT).show();
//                        }
//                    });
//        }

        VideoOptions videoOptions = new VideoOptions.Builder()
                .setStartMuted(true)
                .build();

        NativeAdOptions adOptions = new NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .build();

        builder.withNativeAdOptions(adOptions);

        AdLoader adLoader = builder.withAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int errorCode) {
//                Toast.makeText(getContext(), "Failed to load native ad: "
//                        + errorCode, Toast.LENGTH_SHORT).show();
            }
        }).build();

        adLoader.loadAd(new PublisherAdRequest.Builder().build());
    }


}
