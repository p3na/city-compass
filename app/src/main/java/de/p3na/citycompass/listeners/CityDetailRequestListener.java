package de.p3na.citycompass.listeners;

import android.location.Location;
import android.util.Log;

import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import org.json.JSONException;
import org.json.JSONObject;

import de.p3na.citycompass.interfaces.IResultSelectedListener;
import de.p3na.citycompass.models.City;

/**
 * Author: Christian Hansen
 * Date: 05.08.17
 * E-Mail: c_hansen@gmx.de
 * <p>
 * Class Description:
 */

public class CityDetailRequestListener implements JSONObjectRequestListener {

    private IResultSelectedListener mListener;

    public CityDetailRequestListener(IResultSelectedListener listener) {
        mListener = listener;
    }

    @Override
    public void onResponse(JSONObject response) {

        try {
            City city = new City();

            city.setName(response.getString("geobytescity"));
            city.setRegion(response.getString("geobytesregion"));
            city.setRegionCode(response.getString("geobytesregionlocationcode"));
            city.setCountry(response.getString("geobytescountry"));
            city.setCountryCode(response.getString("geobytesinternet"));

            Location location = new Location(city.getName());
            location.setLatitude(Double.valueOf(response.getString("geobyteslatitude")));
            location.setLongitude(Double.valueOf(response.getString("geobyteslongitude")));

            city.setLocation(location);

            mListener.onResultSelected(city);

        } catch (JSONException e) {
            Log.e("CITY DETAILS", e.getMessage());
        }
    }

    @Override
    public void onError(ANError anError) {
        Log.e("CITY DETAILS", anError.getMessage());
    }
}
