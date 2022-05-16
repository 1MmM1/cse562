package com.example.microphone;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;

public class Constants {
    static Button startButton, stopButton;
    static EditText volEt;
    static TextView classificationTv;
    static LineChart lineChart;
    static double frequency;
    static double threshold = 90; // amplitude
    static short[] samples;
    static short[] temp;
}
