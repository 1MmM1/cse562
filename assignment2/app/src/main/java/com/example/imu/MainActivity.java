package com.example.imu;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private static final double ALPHA = 0.8;

    private SensorManager sensorManager;
    private Sensor gravitySensor;
    private TextView textView;
    private Sensor linearAcclerationSensor;
    private LineChart lineChart;
    private int grantResults[];
    List<Entry> lineDataX;
    List<Entry> lineDataY;
    List<Entry> lineDataZ;
    int counter = 0;
    int lim = 500;
    Activity av;

    private Sensor magnetSensor;
    private Sensor gyroSensor;
    private Sensor acclerationSensor;

    private long startTime;

    // assume tilt is never negative
    private double gyroTilt = -1;
    private double accTilt = -1;
    private double cumGyroX = 0; // in radians
    private double cumGyroY = 0; // in radians

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        av = this;

        // get permissions
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        onRequestPermissionsResult(1, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, grantResults);

        // ui logic
        Constants.startButton = (Button) findViewById(R.id.button);
        Constants.stopButton = (Button) findViewById(R.id.button2);
        lineChart = (LineChart) findViewById(R.id.linechart);
        textView = (TextView) findViewById(R.id.textView);

        Constants.startButton.setEnabled(true);
        Constants.stopButton.setEnabled(false);

        // defining sensors
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        sensorManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_FASTEST);

        linearAcclerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(this, linearAcclerationSensor, SensorManager.SENSOR_DELAY_FASTEST);

        magnetSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.registerListener(this, magnetSensor, SensorManager.SENSOR_DELAY_NORMAL);

        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL);

        acclerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, acclerationSensor, SensorManager.SENSOR_DELAY_NORMAL);

        // on click listeners
        Constants.startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Constants.laccx = new ArrayList<>();
                Constants.laccy = new ArrayList<>();
                Constants.laccz = new ArrayList<>();
                Constants.gravx = new ArrayList<>();
                Constants.gravy = new ArrayList<>();
                Constants.gravz = new ArrayList<>();
                Constants.magx = new ArrayList<>();
                Constants.magy = new ArrayList<>();
                Constants.magz = new ArrayList<>();
                Constants.gyrox = new ArrayList<>();
                Constants.gyroy = new ArrayList<>();
                Constants.gyroz = new ArrayList<>();
                Constants.accx = new ArrayList<>();
                Constants.accy = new ArrayList<>();
                Constants.accz = new ArrayList<>();
                Constants.startButton.setEnabled(false);
                Constants.stopButton.setEnabled(true);

                lineDataX = new ArrayList<>();
                lineDataY = new ArrayList<>();
                lineDataZ = new ArrayList<>();
                counter = 0;
                Constants.start = true;
                startTime = System.currentTimeMillis();
                Constants.gyroTimestamps = new ArrayList<>();
                Constants.gyroTimestamps.add(startTime);
                Constants.accTimestamps = new ArrayList<>();
                Constants.accTimestamps.add(startTime);

                Constants.gyroTilts = new ArrayList<>();
                Constants.accTilts = new ArrayList<>();
            }
        });
        Constants.stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Constants.startButton.setEnabled(true);
                Constants.stopButton.setEnabled(false);
                Constants.start = false;
                String fname = System.currentTimeMillis() + "";
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText(fname);
                    }
                });
                FileOperations.writetofile(av, fname);
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (Constants.start) {
            if (sensorEvent.sensor.equals(linearAcclerationSensor)) {
                Constants.laccx.add(sensorEvent.values[0]);
                Constants.laccy.add(sensorEvent.values[1]);
                Constants.laccz.add(sensorEvent.values[2]);

                //graphing logic
//                graphData(sensorEvent.values);
            }
            if (sensorEvent.sensor.equals(gravitySensor)) {
                Constants.gravx.add(sensorEvent.values[0]);
                Constants.gravy.add(sensorEvent.values[1]);
                Constants.gravz.add(sensorEvent.values[2]);
            }
            if (sensorEvent.sensor.equals(magnetSensor)) {
                Constants.magx.add(sensorEvent.values[0]);
                Constants.magy.add(sensorEvent.values[1]);
                Constants.magz.add(sensorEvent.values[2]);
//                graphData(sensorEvent.values);
            }
            if (sensorEvent.sensor.equals(gyroSensor)) {
                long prevTime = Constants.gyroTimestamps.get(Constants.gyroTimestamps.size() - 1);
                long currTime = System.currentTimeMillis();
                Constants.gyroTimestamps.add(currTime);
                Constants.gyrox.add(sensorEvent.values[0]);
                Constants.gyroy.add(sensorEvent.values[1]);
                Constants.gyroz.add(sensorEvent.values[2]);

//                long durationMs = System.currentTimeMillis() - this.startTime;
//                long durationMs = 200000; // delay for SENSOR_DELAY_NORMAL as specificied in documentation
                long durationMs = prevTime - currTime;
                double durationS = durationMs / 1000.0;
                cumGyroX += sensorEvent.values[0];
                cumGyroY += sensorEvent.values[1];
                double angleX = Math.toDegrees((cumGyroX - Constants.gyroBias[0]) * durationS);
                double angleY = Math.toDegrees((cumGyroY) * durationS);

                gyroTilt = Math.sqrt(Math.pow(angleX, 2) + Math.pow(angleY, 2));
                Constants.gyroTilts.add(gyroTilt);
                Log.i("gyro_tilt", "" + gyroTilt);
//                graphData(sensorEvent.values);
                graphData(new float[]{(float) angleX, (float) angleY, (float) Math.toDegrees((sensorEvent.values[2] - Constants.gyroBias[2]) * durationS)});
//                graphData(new float[]{0, (float) gyroTilt, 0});
            }
            if (sensorEvent.sensor.equals(acclerationSensor)){
                Constants.accx.add(sensorEvent.values[0]);
                Constants.accy.add(sensorEvent.values[1]);
                Constants.accz.add(sensorEvent.values[2]);
//                accTilt = Math.acos(sensorEvent.values[2] / Math.sqrt(Math.pow(sensorEvent.values[0], 2) + Math.pow(sensorEvent.values[1], 2) + Math.pow(sensorEvent.values[2], 2)));
                accTilt = Math.acos((sensorEvent.values[2] - Constants.accBias[2]) / Math.sqrt(Math.pow(sensorEvent.values[0] - Constants.accBias[0], 2)
                        + Math.pow(sensorEvent.values[1] - Constants.accBias[1], 2) + Math.pow(sensorEvent.values[2] - Constants.accBias[2], 2)));
                accTilt = Math.toDegrees(accTilt);
                Constants.accTilts.add(accTilt);
                Log.i("acc_tilt", "" + accTilt);
//                graphData(sensorEvent.values);
//                graphData(new float[]{(float) accTilt, 0, 0});
            }
//            Log.e("log",String.format("%s %.2f %.2f %.2f",sensorEvent.sensor.getName(),sensorEvent.values[0],sensorEvent.values[1],sensorEvent.values[2]));
//            if (gyroTilt > 0 && accTilt > 0) {
//                graphData(new float[]{(float) accTilt, (float) gyroTilt, 0});
//                accTilt = -1;
//                gyroTilt = -1;
//            }
        }
    }

    public void graphData(float[] values) {
        lineDataX.add(new Entry(counter, values[0]));
        lineDataY.add(new Entry(counter, values[1]));
        lineDataZ.add(new Entry(counter, values[2]));
        if (lineDataX.size() > lim) {
            lineDataX.remove(0);
            lineDataY.remove(0);
            lineDataZ.remove(0);
        }
        counter += 1;

        LineDataSet data1 = new LineDataSet(lineDataX, "x");
        LineDataSet data2 = new LineDataSet(lineDataY, "y");
        LineDataSet data3 = new LineDataSet(lineDataZ, "z");
        data1.setDrawCircles(false);
        data2.setDrawCircles(false);
        data3.setDrawCircles(false);
        data1.setColor(((MainActivity) this).getResources().getColor(R.color.red));
        data2.setColor(((MainActivity) this).getResources().getColor(R.color.green));
        data3.setColor(((MainActivity) this).getResources().getColor(R.color.blue));
        List<ILineDataSet> data = new ArrayList<>();
        data.add(data1);
        data.add(data2);
        data.add(data3);

        LineData lineData = new LineData(data);
        lineChart.setData(lineData);
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();
    }

    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, linearAcclerationSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, magnetSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, acclerationSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}