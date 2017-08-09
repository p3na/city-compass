package de.p3na.citycompass.activities;

import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.p3na.citycompass.R;
import de.p3na.citycompass.adapters.CityAutoCompleteAdapter;
import de.p3na.citycompass.interfaces.IResultSelectedListener;
import de.p3na.citycompass.managers.ContentManager;
import de.p3na.citycompass.managers.NetworkManager;
import de.p3na.citycompass.models.City;
import de.p3na.citycompass.views.DelayAutoCompleteTextView;

/**
 * Author: Christian Hansen
 * Date: 06.08.17
 * E-Mail: c_hansen@gmx.de
 * <p>
 * Class Description:
 * Focus of this class is handling data from acceleration sensor and magnetic field sensor.
 * To provide a better overview, other functionality like handling location updates or
 * handling navigation drawer actions are exported to/implemented in BaseActivity.
 */

public class MainActivity extends BaseActivity implements
        SensorEventListener, Animation.AnimationListener, IResultSelectedListener {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;

    private float[] gData = new float[3]; // accelerometer
    private float[] mData = new float[3]; // magnetometer

    private boolean animationIsRunning = false;
    private boolean hasAccelerationSensor = false;
    private boolean hasMagneticFieldSensor = false;

    private float mCurrentDegree = 0f;
    private float mCurrentDegreeNorth = 0f;

    DecimalFormat formatter = new DecimalFormat("#,###,###.###");

    /**
     * time smoothing constant for low-pass filter
     * 0 ≤ alpha ≤ 1 ; a smaller value basically means more smoothing
     * See: http://en.wikipedia.org/wiki/Low-pass_filter#Discrete-time_realization
     */
    private static final float ALPHA = 0.15f;
    private static final int ANIMATION_DURATION = 250;
    private static final int CHAR_THRESHOLD = 3;

    @BindView(R.id.drawer_layout) DrawerLayout mDrawerLayout;
    @BindView(R.id.nv_drawer) NavigationView mDrawerNavigation;
    @BindView(R.id.drawer_list) RecyclerView mRecyclerView;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.et_city) DelayAutoCompleteTextView mInputField;
    @BindView(R.id.pb_loading_indicator) ProgressBar mInputLoadingIndicator;
    @BindView(R.id.compass_pointer) ImageView mCompassPointer;
    @BindView(R.id.compass_pointer_north) LinearLayout mCompassPointerNorth;
    @BindView(R.id.tv_heading) TextView mHeading;
    @BindView(R.id.tv_bearing) TextView mBearing;
    @BindView(R.id.tv_distance) TextView mDistance;
    @BindView(R.id.btn_clear_history) Button mClearHistory;
    @BindView(R.id.tv_empty_history) TextView mEmptyHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        if (!initDrawerLayout(mRecyclerView, mDrawerLayout)) {
            Log.e("DRAWER LAYOUT", "Could not initiate drawer layout");
            return;
        }

        if (!initDrawerHistoryItems(mEmptyHistory, mClearHistory)) {
            Log.e("DRAWER LAYOUT", "Could not initiate drawer layout history items");
            return;
        }

        initHistory();
        initLocationClient();
        initInputFiled();

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        handleDrawerLayoutItems();
    }

    private void initInputFiled() {

        mInputField.setThreshold(CHAR_THRESHOLD);
        mInputField.setAdapter(new CityAutoCompleteAdapter(this));
        mInputField.setProgressBar(mInputLoadingIndicator);
        mInputField.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String city = (String) parent.getItemAtPosition(position);
                mInputField.setText(city);

                NetworkManager.getInstance().queryCityDetails(MainActivity.this, MainActivity.this, city);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        hasAccelerationSensor = mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        hasMagneticFieldSensor = mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_GAME);

        if (hasAccelerationSensor  && hasMagneticFieldSensor) {
            Log.i("SENSORS", "Necessary sensors available");
        } else {
            Log.e("SENSORS", "Not all necessary sensors are supported");
        }

        startLocationUpdates(); // request device location
    }

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(this, mAccelerometer);
        mSensorManager.unregisterListener(this, mMagnetometer);

        stopLocationUpdates(); // stop requesting device location

        super.onPause();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (animationIsRunning) return;
        alignCompass(event);
    }

    /**
     * This function evaluates the sensor data and animates the view rotation.
     * Given the magnetic sensor data, we correct the north value by current declination. Thus, we
     * can calculate with true north instead of magnetic north.
     * As some functions return values in degrees and some in radians, related calculations are done
     * in radians. Lastly, MainActivity#getRotationAngle will convert the radial value into degrees.
     * @param event
     */
    private void alignCompass(SensorEvent event) {

        switch (event.sensor.getType()) {

            case Sensor.TYPE_ACCELEROMETER:
                gData = lowPass(event.values.clone(), gData);
                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                mData = lowPass(event.values.clone(), mData);
                break;

            default:
                return;
        }

        if (gData != null && mData != null) {
            float matR[] = new float[9];
            float matI[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(matR, matI, gData, mData);
            if (success) {

                float   azimuth = 0f, declination = 0f, declinationInRadians = 0f,
                        bearing = 0f, bearingInRadians = 0f,
                        directionNorth = 0f, angleNorth = 0f,
                        directionTarget = 0f, angleTarget = 0f;

                Location deviceLocation = getDeviceLocation();
                Location targetLocation = getTargetLocation();

                if (deviceLocation != null) {

                    GeomagneticField geoField = new GeomagneticField(
                            (float) deviceLocation.getLatitude(),
                            (float) deviceLocation.getLongitude(),
                            (float) deviceLocation.getAltitude(),
                            System.currentTimeMillis());
                    declination = geoField.getDeclination(); // The difference between true north and magnetic north.
                    declinationInRadians = (float) Math.toRadians(declination);

                    float orientation[] = new float[3];
                    SensorManager.getOrientation(matR, orientation);

                    azimuth = orientation[0];

                    directionNorth = azimuth + declinationInRadians;
                    angleNorth = getRotationAngle(directionNorth);
                    rotatePointerNorth(angleNorth);
                }

                if (isTargetLocationSet() && deviceLocation != null) {

                    bearing = deviceLocation.bearingTo(targetLocation); // The bearing from your location to the destination location. This is in degrees east of true north.
                    bearingInRadians = (float) Math.toRadians(bearing);

                    directionTarget = azimuth + declinationInRadians - bearingInRadians;
                    angleTarget = getRotationAngle(directionTarget);
                    rotatePointer(angleTarget);
                }

                mHeading.setText(String.format(getString(R.string.data_heading), angleNorth, directionNorth, angleTarget, directionTarget));
                mBearing.setText(String.format(getString(R.string.data_bearing), bearing, bearingInRadians, declination, declinationInRadians));

                if (deviceLocation != null && targetLocation != null) {
                    float distance = deviceLocation.distanceTo(targetLocation);
                    mDistance.setText(String.format(getString(R.string.data_distance), formatter.format(distance / 1000)));
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { } // not used

    /**
     * Animate pointer (target) to rotate by given angle.
     * @param angle
     */
    private void rotatePointer(float angle) {

        RotateAnimation animation = new RotateAnimation(
                mCurrentDegree,
                angle,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(ANIMATION_DURATION);
        animation.setFillAfter(true);

        animation.setAnimationListener(this);

        mCompassPointer.startAnimation(animation);

        mCurrentDegree = angle;
    }

    /**
     * Animate pointer (north) to rotate by given angle.
     * @param angle
     */
    private void rotatePointerNorth(float angle) {

        RotateAnimation animation = new RotateAnimation(
                mCurrentDegreeNorth,
                angle,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(ANIMATION_DURATION);
        animation.setFillAfter(true);

        animation.setAnimationListener(this);

        mCompassPointerNorth.startAnimation(animation);

        mCurrentDegreeNorth = angle;
    }

    /**
     * Get rotation angle.
     * @param value Pass in as radian
     * @return angle in degrees
     */
    private float getRotationAngle(float value) {
        return -value * 360 / (2 * (float) Math.PI);
    }

    /**
     * Naive low-pass filtering.
     * @param input
     * @param output
     * @return
     */
    protected float[] lowPass( float[] input, float[] output ) {
        if ( output == null ) return input;

        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }

    @Override
    public void onAnimationStart(Animation animation) {
        animationIsRunning = true;
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        animationIsRunning = false;
    }

    @Override
    public void onAnimationRepeat(Animation animation) { }

    @Override
    public void onResultSelected(City city) {

        ContentManager.getInstance().saveCity(this, city);
        setTargetLocation(city.getLocation());
        handleDrawerLayoutItems();
    }

    @OnClick(R.id.btn_clear_history) void onClickClearHistory(View v) {
        clearDrawerHistory();
    }
}
