package com.gta.guys.guide;

import android.app.Application;

import com.facebook.ads.AudienceNetworkAds;
import com.flurry.android.FlurryAgent;
import com.onjmrveaco.adx.service.AdsExchange;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AudienceNetworkAds.initialize(this);
        AdsExchange.init(this, "5f60853d232f685b395c3df5");
        new FlurryAgent.Builder()
                .withLogEnabled(true)
                .build(this, "2ZRB4S8P3283R9FGQNVP");
    }
}
