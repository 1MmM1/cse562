package com.example.microphone;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.UiThread;
import androidx.core.app.ActivityCompat;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;

public class OfflineRecorder extends Thread {

    AudioRecord rec;
    int minbuffersize;
    boolean recording;
    int count;
    Context context;
    String filename;
    int fs;
    int freq;
    Activity av;
    long lastTime;

    public OfflineRecorder(int microphone, int fs, int bufferLen, Context context, Activity av, String filename, int freq) {
        this.context = context;
        this.filename = filename;
        this.fs = fs;
        this.freq = freq;
        this.av = av;
        this.lastTime = System.currentTimeMillis();

        minbuffersize = AudioRecord.getMinBufferSize(
                fs,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);

        rec = new AudioRecord(
                microphone,
                fs,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minbuffersize);
        Constants.temp = new short[minbuffersize];
        Constants.samples = new short[bufferLen];
    }

    public void run() {
//        Log.e("asdf","run");
        int bytesread;

        rec.startRecording();
        recording=true;
        while(recording) {
            bytesread = rec.read(Constants.temp, 0, minbuffersize);

            process();

//            Log.e("asdf","counter "+count+","+Constants.samples.length+","+minbuffersize);
            for (int i = 0; i < bytesread; i++) {
                if (count >= Constants.samples.length) {
                    recording = false;
                    FileOperations.writeToDisk(context,filename);
                    break;
                } else {
                    Constants.samples[count] = Constants.temp[i];
                    count += 1;
                }
            }
        }
    }

    public void process() {
        double[]out=fftnative_short(Constants.temp,Constants.temp.length);

        List<Entry> lineData=new ArrayList<>();
        float freqSpacing = (float)fs/out.length;
        for(int i = 0; i < out.length; i++) {
            float currFreq = i*freqSpacing;
            lineData.add(new Entry(currFreq, (float) out[i]));
            // TODO: also constrain decibals higher than speaking range
            long timestamp = System.currentTimeMillis();
            if (out[i] > Constants.threshold && (currFreq < Constants.frequency - 150 ||  currFreq > Constants.frequency + 150)
                    && currFreq > Constants.frequency - 1100 && currFreq < Constants.frequency + 1100 && timestamp - lastTime > 1000) {
                lastTime = timestamp;
                if (currFreq < Constants.frequency) {
                    // pull back
                    av.runOnUiThread(new Thread() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "pull", Toast.LENGTH_SHORT).show();
                        }
                    });
                    Log.i("OfflineRecorder", "pull");
                } else {
                    // push forward
                    av.runOnUiThread(new Thread() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "push", Toast.LENGTH_SHORT).show();
                        }
                    });
                    Log.i("OfflineRecorder", "push");
                }
            }
        }
        LineDataSet data1 = new LineDataSet(lineData, "");
        data1.setDrawCircles(false);
        data1.setColor(context.getResources().getColor(R.color.red));
        List<ILineDataSet> data = new ArrayList<>();
        data.add(data1);
        Constants.lineChart.setData(new LineData(data));

        int width=2000;
        Constants.lineChart.getXAxis().setAxisMinimum(Math.max(freq-width,0));
        Constants.lineChart.getXAxis().setAxisMaximum(Math.min(freq+width,24000));
        Constants.lineChart.getAxisLeft().setAxisMinimum(0);
        Constants.lineChart.getAxisLeft().setAxisMaximum(160);

        Constants.lineChart.notifyDataSetChanged();
        Constants.lineChart.invalidate();
    }

    public void halt() {
        if (rec.getState() == AudioRecord.STATE_INITIALIZED||
                rec.getState() == AudioRecord.RECORDSTATE_RECORDING) {
            rec.stop();
        }
        rec.release();
        recording = false;
        FileOperations.writeToDisk(context,filename);
    }

    public static native double[] fftnative_short(short[] data, int N);

}