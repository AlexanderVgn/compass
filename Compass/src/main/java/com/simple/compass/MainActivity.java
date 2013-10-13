package com.simple.compass;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.app.Activity;

public class MainActivity extends Activity {

    private static final int SENSOR_ARRAY_SIZE = 3;

    private static final int ORIENTATION_AZIMUTH = 0;

    private static final int ORIENTATION_ROLL = 1;

    private static final int ORIENTATION_PITCH = 2;

    private float[] aValues = new  float[SENSOR_ARRAY_SIZE];

    private  float[] mValues = new float[SENSOR_ARRAY_SIZE];

    private CompassView compassView;

    private SensorManager sensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        compassView = (CompassView)this.findViewById(R.id.compassView);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        updateOrientation(new float[] {0, 0, 0});
}

    private void updateOrientation(float[] values){
        if (compassView != null ){
            compassView.setBearing(values[ORIENTATION_AZIMUTH]);
            compassView.setPitch(values[ORIENTATION_PITCH]);
            compassView.setRoll(values[ORIENTATION_ROLL]);
            compassView.invalidate();
        }
    }

    private float[] calculateOrientation(){
        float[] values = new float[SENSOR_ARRAY_SIZE];
        float[] R = new float[9];
        float[] outR = new float[9];

        SensorManager.getRotationMatrix(R, null, aValues, mValues);
        //SensorManager.remapCoordinateSystem(R, SensorManager.AXIS_X, SensorManager.AXIS_Y, outR);
        //SensorManager.getOrientation(outR, values);
        SensorManager.getOrientation(R, values);

        // Преобразование радиан в градусы
        values[ORIENTATION_AZIMUTH] = (float) Math.toDegrees(values[ORIENTATION_AZIMUTH]);
        values[ORIENTATION_PITCH] = (float) Math.toDegrees(values[ORIENTATION_PITCH]);
        values[ORIENTATION_ROLL] = (float) Math.toDegrees(values[ORIENTATION_ROLL]);

        return  values;
    }

    private final SensorEventListener sensorEventListener = new
            SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                        aValues = event.values;

                    if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
                        mValues = event.values;

                    updateOrientation(calculateOrientation());
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {
                }
            };

    @Override
    protected void  onResume(){
        super.onResume();

        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor magField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        sensorManager.registerListener(sensorEventListener,
                accelerometer,
                SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(sensorEventListener,
                magField,
                SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onStop(){
        sensorManager.unregisterListener(sensorEventListener);
        super.onStop();
    }
}
