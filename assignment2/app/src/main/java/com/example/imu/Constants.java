package com.example.imu;

import android.widget.Button;

import java.util.ArrayList;

public class Constants {
    static Button startButton;
    static Button stopButton;
    static boolean start=false;
    static ArrayList<Float> laccx;
    static ArrayList<Float> laccy;
    static ArrayList<Float> laccz;
    static ArrayList<Float> gravx;
    static ArrayList<Float> gravy;
    static ArrayList<Float> gravz;
    static ArrayList<Float> magx;
    static ArrayList<Float> magy;
    static ArrayList<Float> magz;
    static ArrayList<Float> gyrox;
    static ArrayList<Float> gyroy;
    static ArrayList<Float> gyroz;
    static ArrayList<Float> accx;
    static ArrayList<Float> accy;
    static ArrayList<Float> accz;

    // Noise and bias
    static double[] accBias = new double[]{0.0655831434092409, 0.00551829715174917, 9.77265714851485};
    static double[] gyroBias = new double[]{-4.30931844660195e-06, 1.20660916504854e-05, 4.30931844660190e-07};
}
