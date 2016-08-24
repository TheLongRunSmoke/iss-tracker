package ru.tlrs.iss;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = MainActivity.class.getSimpleName();

    public final static String TLE = "1 25544U 98067A 16237.33327761 .00016717 00000-0 10270-3 0 9020\n2 25544 51.6431 77.9774 0003027 246.5903 113.4932 15.54303859 15589";

    private TextView tle, orbit;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tle = (TextView) findViewById(R.id.tle);
        orbit = (TextView) findViewById(R.id.orbitalData);
        tle.setText(TLE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.button:{
                Log.d(TAG, "onClick: RUN");
                mHandler.post(DoOrbitProcessing);
                break;
            }
            default:
        }
    }

    public native boolean tleProcessing(String tle);

    static {
        System.loadLibrary("OrbitProcessing");
    }

    private Runnable DoOrbitProcessing = new Runnable() {
        public void run() {
            tleProcessing(TLE);
        }
    };
}
