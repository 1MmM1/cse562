package com.example.imu;

import android.widget.Button;

import java.util.ArrayList;

public class Constants {
    static Button startButton;
    static Button stopButton;
    static boolean start = false;
    static ArrayList<Long> gyroTimestamps;
    static ArrayList<Double> gyroTilts;
    static ArrayList<Float> gyrox;
    static ArrayList<Float> gyroy;
    static ArrayList<Float> gyroz;
    static ArrayList<Long> accTimestamps;
    static ArrayList<Double> accTilts;
    static ArrayList<Float> accx;
    static ArrayList<Float> accy;
    static ArrayList<Float> accz;

    // Noise and bias
    static double[] gyroBias = new double[]{-4.30931844660195e-06, 1.20660916504854e-05, 4.30931844660190e-07};
}
