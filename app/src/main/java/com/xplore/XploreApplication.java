package com.xplore;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

/**
 * Created by Nikaoto on 7/17/2017.
 * This class is exclusively used for multiDex for the support lower-end device debugging
 */

public class XploreApplication extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
