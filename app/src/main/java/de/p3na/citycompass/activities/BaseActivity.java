package de.p3na.citycompass.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import de.p3na.citycompass.R;
import de.p3na.citycompass.adapters.DrawerListAdapter;
import de.p3na.citycompass.interfaces.IDrawerItemSelectedListener;
import de.p3na.citycompass.managers.ContentManager;
import de.p3na.citycompass.models.City;

/**
 * Author: Christian Hansen
 * Date: 06.08.17
 * E-Mail: c_hansen@gmx.de
 * <p>
 * Class Description:
 * Handle location updates, permission requests, navigation drawer actions.
 */

abstract class BaseActivity extends AppCompatActivity implements IDrawerItemSelectedListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;

    private boolean isPerformingLocationUpdates = false;
    private boolean isTargetSet = false;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;

    private Location targetLocation = new Location("targetLocation");
    private Location deviceLocation = new Location("deviceLocation");

    private DrawerLayout mDrawerLayout;
    private DrawerListAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private ActionBarDrawerToggle mActionBarToggle;
    private TextView mEmptyHistory;
    private Button mClearHistory;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mActionBarToggle.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }

    void initHistory() {
        ContentManager.getInstance().loadHistory(this);
    }

    boolean initDrawerLayout(RecyclerView recyclerView, DrawerLayout drawerLayout) {

        mRecyclerView = recyclerView;
        mDrawerLayout = drawerLayout;

        if (mRecyclerView == null) return false;
        if (mDrawerLayout == null) return false;

        mRecyclerView.setHasFixedSize(true);

        mAdapter = new DrawerListAdapter(ContentManager.getInstance().getCities(), this);
        mRecyclerView.setAdapter(mAdapter);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mActionBarToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        mDrawerLayout.addDrawerListener(mActionBarToggle);
        mActionBarToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        return true;
    }

    boolean initDrawerHistoryItems(TextView emptyHistory, Button clearHistory) {

        if (emptyHistory == null) return false;
        if (clearHistory == null) return false;

        mEmptyHistory = emptyHistory;
        mClearHistory = clearHistory;

        return true;
    }

    void handleDrawerLayoutItems() {
        mClearHistory.setVisibility((ContentManager.getInstance().getCities().size() == 0) ? View.GONE : View.VISIBLE);
        mEmptyHistory.setVisibility((ContentManager.getInstance().getCities().size() == 0) ? View.VISIBLE : View.GONE);
    }

    void initLocationClient() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("PERMISSION", "Location permission not granted");
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            deviceLocation = location;
                            updateOwnLocationInHeader();
                        }
                    }
                });
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    deviceLocation = location;
                    updateOwnLocationInHeader();
                }
            }
        };

        createLocationRequest();
    }

    Location getDeviceLocation() {
        return deviceLocation;
    }

    Location getTargetLocation() {
        return targetLocation;
    }

    void setTargetLocation(Location newTarget) {
        targetLocation = newTarget;
        isTargetSet = true;

        updateTargetLocationInHeader();

        mAdapter.setCities(ContentManager.getInstance().getCities());
        mAdapter.notifyDataSetChanged();
    }

    boolean isTargetLocationSet() {
        return isTargetSet;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    initLocationClient();
                    if (!isPerformingLocationUpdates)
                        startLocationUpdates();

                } else {

                    // TODO: Replace target needle with permission warning; disable input field
                }
        }

    }

    private void createLocationRequest() {
        if (mLocationRequest != null) return;
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Log.e("PERMISSION", "Location permission not granted");
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        if (mFusedLocationClient != null && mLocationCallback != null) {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback,
                    null /* Looper */);
            isPerformingLocationUpdates = true;
        }
}

    void stopLocationUpdates() {

        if (mFusedLocationClient != null && mLocationCallback != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            isPerformingLocationUpdates = false;
        }
    }

    @Override
    public void onDrawerItemSelected(City city) {
        Toast.makeText(this, "Selected: " + city.getName(), Toast.LENGTH_SHORT).show();

        setTargetLocation(city.getLocation());

        mDrawerLayout.closeDrawers();

        handleDrawerLayoutItems();
    }

    /**
     * Update own location in navigation drawer header layout.
     */
    private void updateOwnLocationInHeader() {
        findViewById(R.id.ll_own_location).setVisibility(View.VISIBLE); // is hidden initially
        TextView tvCoordinatesOwnLocation = (TextView) findViewById(R.id.tv_coordinates_location);
        if (tvCoordinatesOwnLocation != null)
            tvCoordinatesOwnLocation.setText(String.format("Lat: %s°\nLng: %s°", deviceLocation.getLatitude(), deviceLocation.getLongitude()));
    }

    /**
     * Update target location in navigation drawer header layout.
     */
    private void updateTargetLocationInHeader() {
        findViewById(R.id.ll_target_location).setVisibility(View.VISIBLE); // is hidden initially
        TextView tvCoordinatesOwnLocation = (TextView) findViewById(R.id.tv_coordinates_target);
        if (tvCoordinatesOwnLocation != null)
            tvCoordinatesOwnLocation.setText(String.format("%s\nLat: %s°\nLng: %s°",
                    targetLocation.getProvider(), targetLocation.getLatitude(), targetLocation.getLongitude()));
    }

    void clearDrawerHistory() {
        ContentManager.getInstance().clearCities(this);
        mAdapter.setCities(ContentManager.getInstance().getCities());
        mAdapter.notifyDataSetChanged();
        mDrawerLayout.closeDrawers();
        handleDrawerLayoutItems();
    }
}
