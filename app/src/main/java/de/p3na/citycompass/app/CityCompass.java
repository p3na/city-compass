package de.p3na.citycompass.app;

import android.app.Application;

import com.androidnetworking.AndroidNetworking;

/**
 * Author: Christian Hansen
 * Date: 31.07.17
 * E-Mail: c_hansen@gmx.de
 * <p>
 * Class Description:
 */

public class CityCompass extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        AndroidNetworking.initialize(getApplicationContext());
    }
}
