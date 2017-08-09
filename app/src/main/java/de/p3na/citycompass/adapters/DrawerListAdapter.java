package de.p3na.citycompass.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.p3na.citycompass.R;
import de.p3na.citycompass.interfaces.IDrawerItemSelectedListener;
import de.p3na.citycompass.models.City;

/**
 * Author: Christian Hansen
 * Date: 03.08.17
 * E-Mail: c_hansen@gmx.de
 * <p>
 * Class Description:
 */

public class DrawerListAdapter extends RecyclerView.Adapter<DrawerListAdapter.ViewHolder> {

    private IDrawerItemSelectedListener mListener;
    private ArrayList<City> mCities;

    public DrawerListAdapter(ArrayList<City> cities, IDrawerItemSelectedListener listener) {
        this.mCities = cities;
        this.mListener = listener;
    }

    public void setCities(ArrayList<City> cities) {
        mCities = cities;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_drawer_item, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        City c = mCities.get(position);

        holder.title.setText(c.getName());
        holder.subTitle.setText(c.getCountry());
    }

    @Override
    public int getItemCount() {
        return mCities.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.tv_drawer_item_title) TextView title;
        @BindView(R.id.tv_drawer_item_subtitle) TextView subTitle;

        ViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);

            itemView.findViewById(R.id.ll_drawer_item).setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            City city = mCities.get(getAdapterPosition());
            mListener.onDrawerItemSelected(city);
        }
    }
}
