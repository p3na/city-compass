package de.p3na.citycompass.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.p3na.citycompass.R;
import de.p3na.citycompass.managers.NetworkManager;
import de.p3na.citycompass.models.City;

/**
 * Author: Christian Hansen
 * Date: 05.08.17
 * E-Mail: c_hansen@gmx.de
 * <p>
 * Class Description:
 * Adapter handling search results for auto-completion.
 */

public class CityAutoCompleteAdapter extends BaseAdapter implements Filterable {

    private Context mContext;
    private List<String> resultList;

    public CityAutoCompleteAdapter(Context context) {
        mContext = context;
        resultList = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return resultList.size();
    }

    @Override
    public String getItem(int position) {
        return resultList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.dropdown_item_2line, parent, false);
        }

        // city string pattern is: "{city}, {region}, {country}"
        // we are going to split it up to show it in two lines
        String cityResult = getItem(position);

        int firstColonIndex = cityResult.indexOf(",");

        if (firstColonIndex > 0) {
            String city = cityResult.substring(0, firstColonIndex);
            String location = cityResult.substring(firstColonIndex + 1).trim();

            ((TextView) convertView.findViewById(R.id.tv_dropdown_item_title)).setText(city);
            ((TextView) convertView.findViewById(R.id.tv_dropdown_item_subtitle)).setText(location);
        } else {
            ((TextView) convertView.findViewById(R.id.tv_dropdown_item_title)).setText(mContext.getString(R.string.error_loading_cities));
            ((TextView) convertView.findViewById(R.id.tv_dropdown_item_subtitle)).setText(mContext.getString(R.string.error_check_network_config));
        }

        return convertView;
    }

    @Override
    public Filter getFilter() {

        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                FilterResults filterResults = new FilterResults();

                if (constraint != null) {
                    Log.i("QUERY", constraint.toString());

                    List<String> cities = findCities(mContext, constraint.toString());

                    if (cities != null) {
                        filterResults.values = cities;
                        filterResults.count = cities.size();
                    }
                }

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                if (results != null && results.count > 0) {
                    resultList = (List<String>) results.values;
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
    }

    private List<String> findCities(Context context, String cityName) {
        return NetworkManager.getInstance().queryCities(context, cityName);
    }
}
