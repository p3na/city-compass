package de.p3na.citycompass.activities;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;

/**
 * Author: Christian Hansen
 * Date: 02.08.17
 * E-Mail: c_hansen@gmx.de
 * <p>
 * Class Description:
 */

public class CompassActivity extends Activity implements SensorEventListener {

    Float azimut;  // View to draw a compass

    public class CustomDrawableView extends View {
        Paint paint = new Paint();
        public CustomDrawableView(Context context) {
            super(context);
            paint.setColor(0xff00ff00);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);
            paint.setAntiAlias(true);
        };

        protected void onDraw(Canvas canvas) {
            int width = getWidth();
            int height = getHeight();
            int centerx = width/2;
            int centery = height/2;
            canvas.drawLine(centerx, 0, centerx, height, paint);
            canvas.drawLine(0, centery, width, centery, paint);
            // Rotate the canvas with the azimut
            if (azimut != null) {
                float angle = -azimut * 360 / (2 * (float) Math.PI);
                paint.setTextSize(20);
                canvas.drawText(String.format("Azimuth: %s, Angle: %s°", azimut, angle), 10, 20, paint);
                canvas.rotate(angle, centerx, centery);
            }
            paint.setColor(0xff0000ff);
            canvas.drawLine(centerx, -1000, centerx, +1000, paint);
            canvas.drawLine(-1000, centery, 1000, centery, paint);
            canvas.drawText("N", centerx+5, centery-10, paint);
            canvas.drawText("S", centerx-10, centery+15, paint);
            paint.setColor(0xff00ff00);
        }
    }

    CustomDrawableView mCustomDrawableView;
    private SensorManager mSensorManager;
    Sensor accelerometer;
    Sensor magnetometer;
    static final float ALPHA = 0.15f;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCustomDrawableView = new CustomDrawableView(this);
        setContentView(mCustomDrawableView);
        // Register the sensor listeners
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {  }

    float[] mGravity;
    float[] mGeomagnetic;
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = lowPass(event.values, mGravity);
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = lowPass(event.values, mGeomagnetic);
        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                azimut = orientation[0]; // orientation contains: azimut, pitch and roll
            }
        }
        mCustomDrawableView.invalidate();
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
}
