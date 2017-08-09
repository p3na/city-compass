package de.p3na.citycompass.managers;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import de.p3na.citycompass.BuildConfig;
import de.p3na.citycompass.models.City;

/**
 * Author: Christian Hansen
 * Date: 03.08.17
 * E-Mail: c_hansen@gmx.de
 * <p>
 * Class Description:
 * Content manager for data (that is already available).
 */

public class ContentManager {

    private static final String TAG_HISTORY = "tag_history";

    private static ContentManager mInstance;

    private ArrayList<City> cities;

    private ContentManager() {
        cities = new ArrayList<>();
    }

    public static ContentManager getInstance() {
        if (mInstance == null) {
            mInstance = new ContentManager();
        }
        return mInstance;
    }

    public ArrayList<City> getCities() {
        return cities;
    }

    /**
     * Load search history.
     * It will be visible in the navigation drawer.
     */
    public void loadHistory(Context context) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);

        Gson gson = new Gson();
        String json = sharedPreferences.getString(TAG_HISTORY, null);

        Type type = new TypeToken<ArrayList<City>>() {}.getType();
        ArrayList<City> historyCities = gson.fromJson(json, type);

        if (historyCities != null)
            cities = historyCities;
    }

    /**
     * Save already searched cities.
     * @param context
     */
    public void saveHistory(Context context) {

        if (cities.size() == 0) return;

        SharedPreferences sharedPreferences = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Gson gson = new Gson();
        String json = gson.toJson(cities);

        editor.putString(TAG_HISTORY, json);
        editor.apply();
    }

    public void saveCity(Context context, City city) {

        cities.add(city);
        saveHistory(context);
    }

    public void clearCities(Context context) {

        cities.clear();

        SharedPreferences sharedPreferences = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(TAG_HISTORY, "");
        editor.apply();
    }
}
