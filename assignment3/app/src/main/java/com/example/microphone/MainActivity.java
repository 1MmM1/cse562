package com.example.microphone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {
    static {
        System.loadLibrary("native-lib");
    }

    LineChart lineChart;
    private int grantResults[];
    EditText editTextNumber;
    int freq=0;
    Worker task;
    Activity av;
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO},1);
        onRequestPermissionsResult(1,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO},grantResults);

        av = this;
        tv = (TextView)findViewById(R.id.textView1);

        Constants.lineChart = (LineChart)findViewById(R.id.linechart);
        Constants.startButton = (Button)findViewById(R.id.button);
        Constants.stopButton = (Button)findViewById(R.id.button2);
        Constants.startButton.setEnabled(true);
        Constants.stopButton.setEnabled(false);
        Constants.startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fname=System.currentTimeMillis()+"";
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv.setText(fname);
                    }
                });

                closeKeyboard();
                task = new Worker(av,freq,48000,fname, av);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                Constants.startButton.setEnabled(false);
                Constants.stopButton.setEnabled(true);
            }
        });
        Constants.stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                task.cancel(true);
                Constants.startButton.setEnabled(true);
                Constants.stopButton.setEnabled(false);
            }
        });

        editTextNumber = (EditText)findViewById(R.id.editTextNumber);
        Constants.volEt = (EditText)findViewById(R.id.editTextNumber2);

        freq=Integer.parseInt(editTextNumber.getText().toString());
        editTextNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence cs, int start,
                                      int before, int count) {
                String s = editTextNumber.getText().toString();
                if (Utils.isInteger(s)) {
                    freq=Integer.parseInt(s);
                    Constants.frequency = freq;
                }
            }
        });
    }

    public void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}