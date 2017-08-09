package de.p3na.citycompass.managers;


import android.content.Context;
import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANRequest;
import com.androidnetworking.common.ANResponse;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import de.p3na.citycompass.R;
import de.p3na.citycompass.activities.MainActivity;
import de.p3na.citycompass.interfaces.IResultSelectedListener;
import de.p3na.citycompass.listeners.CityDetailRequestListener;

/**
 * Author: Christian Hansen
 * Date: 31.07.17
 * E-Mail: c_hansen@gmx.de
 * <p>
 * Class Description:
 */

public class NetworkManager {

    private static NetworkManager mInstance;

    private NetworkManager() { }

    public static NetworkManager getInstance() {
        if (mInstance == null)
            mInstance = new NetworkManager();

        return mInstance;
    }

    /**
     * Synchronous search query.
     * As this function is intended to be invoked by a filter performance,
     * and for the reason that this performance is already running on a separate thread,
     * we do not need to create another one. And we can directly access/return the result.
     * @param context
     * @param search
     * @return
     */
    public List<String> queryCities(Context context, String search) {

        List<String> results = new ArrayList<>();

        ANRequest request = AndroidNetworking
                .get(String.format(context.getString(R.string.url_city_autocomplete_search), search))
                .build();

        ANResponse anResponse = request.executeForJSONArray();
        if (anResponse.isSuccess()) {

            try {
                JSONArray array = (JSONArray) anResponse.getResult();
                for (int i = 0; i < array.length(); i++) {
                    results.add(array.get(i).toString());
                }
            } catch (JSONException | ClassCastException e) {
                Log.e("CITY SEARCH", e.getMessage());
            }

        } else {
            results.add(context.getString(R.string.error_loading_cities));
        }

        return results;
    }

    public void queryCityDetails(Context context, IResultSelectedListener listener, String city) {

        AndroidNetworking
                .get(String.format(context.getString(R.string.url_city_details), city))
                .build()
                .getAsJSONObject(new CityDetailRequestListener(listener));
    }
}
