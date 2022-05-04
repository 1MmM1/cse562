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
    private static final double ALPHA = 0.98;
    private static final int RAMP_UP_MS = 1000;
    private static final double NS2S = 1.0 / 1000000000.0;

    private SensorManager sensorManager;
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

    private Sensor gyroSensor;
    private Sensor acclerationSensor;

    private long startTime;

    // assume tilt is never negative
    private double gyroTilt = -1;
    private double accTilt = -1;
    private double instaGyroTilt = -1;
    private double cumTilt = 0;
    private boolean validAcc = false;
    private boolean validGyro = false;
    private boolean validInstaGyro = false;

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

        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_FASTEST);

        acclerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, acclerationSensor, SensorManager.SENSOR_DELAY_FASTEST);

        // on click listeners
        Constants.startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
            if (sensorEvent.sensor.equals(gyroSensor)) {
                if (Constants.gyrox.size() == 100) {
                    Constants.gyroBias[0] = cumGyroX / Constants.gyrox.size();
                    Constants.gyroBias[1] = cumGyroY / Constants.gyroy.size();
                }
                long prevTime = Constants.gyroTimestamps.get(Constants.gyroTimestamps.size() - 1);
                long currTime = sensorEvent.timestamp;
                float sensorX = sensorEvent.values[0];
                float sensorY = sensorEvent.values[1];

                Constants.gyroTimestamps.add(currTime); // now in nanoseconds
                Constants.gyrox.add(sensorX);
                Constants.gyroy.add(sensorY);
                Constants.gyroz.add(sensorEvent.values[2]);

                long gapDurationNs = prevTime - currTime; // now in nanoseconds
                double durationS = gapDurationNs * NS2S;
                cumGyroX += sensorX;
                cumGyroY += sensorY;
                double angleX = Math.toDegrees((cumGyroX) * durationS);
                double angleY = Math.toDegrees((cumGyroY) * durationS);

                gyroTilt = Math.sqrt(Math.pow(angleX, 2) + Math.pow(angleY, 2));
                validGyro = true;
                Constants.gyroTilts.add(gyroTilt);

                // instantaneous calculation for the complemenatry filter
                angleX = Math.toDegrees((sensorX) * durationS);
                angleY = Math.toDegrees((sensorY) * durationS);
                instaGyroTilt = Math.sqrt(Math.pow(angleX, 2) + Math.pow(angleY, 2));
                validInstaGyro = true;

//                graphData(new float[]{0, (float) gyroTilt, 0});
            }
            if (sensorEvent.sensor.equals(acclerationSensor)){
                float sensorX = sensorEvent.values[0];
                float sensorY = sensorEvent.values[1];
                float sensorZ = sensorEvent.values[2];
                Constants.accx.add(sensorX);
                Constants.accy.add(sensorY);
                Constants.accz.add(sensorZ);

                double denom = Math.sqrt(Math.pow(sensorX / 9.81f, 2) + Math.pow(sensorY / 9.81f, 2) + Math.pow(sensorZ / 9.81f, 2));
                accTilt = Math.acos((sensorZ / 9.81f) / denom);
                accTilt = Math.toDegrees(accTilt);
                validAcc = true;
                Constants.accTilts.add(accTilt);
//                graphData(new float[]{(float) accTilt, 0, 0});
            }
            if (validAcc && validGyro && validInstaGyro) {
                cumTilt = ALPHA * (cumTilt + instaGyroTilt) + (1 - ALPHA) * accTilt;
//                graphData(new float[]{(float) accTilt, (float) gyroTilt, (float) cumTilt});
                graphData(new float[]{0, 0, (float) cumTilt});
                validAcc = false;
                validGyro = false;
                validInstaGyro = false;
            }
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
        sensorManager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, acclerationSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}