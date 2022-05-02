package com.example.imu;

import android.widget.Button;

import java.util.ArrayList;

public class Constants {
    static Button startButton;
    static Button stopButton;
    static boolean start = false;
    static ArrayList<Float> laccx;
    static ArrayList<Float> laccy;
    static ArrayList<Float> laccz;
    static ArrayList<Float> gravx;
    static ArrayList<Float> gravy;
    static ArrayList<Float> gravz;
    static ArrayList<Float> magx;
    static ArrayList<Float> magy;
    static ArrayList<Float> magz;
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
    static double[] accBias = new double[]{-3.29440713783652e-05, -7.02731077950524e-05, -0.0500218994564540};
    static double[] gyroBias = new double[]{-4.30931844660195e-06, 1.20660916504854e-05, 4.30931844660190e-07};
}
