package ru.tlrs.iss;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = MainActivity.class.getSimpleName();

    public final static String TLE = "1 25544U 98067A 17063.15557486 .00016717 00000-0 10270-3 0 9184\n2 25544 51.6422 201.8740 0007726 271.9848 88.0419 15.54051147 5418";

    private TextView tle, orbit;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    @BindView(R.id.tle)
    TextView mTle;
    @BindView(R.id.orbitalData)
    TextView mOrbit;
    @BindView(R.id.button)
    Button mCalculateButton;
    @BindView(R.id.map)
    Button mMapButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mTle.setText(TLE);
        mCalculateButton.setOnClickListener(this);
        mMapButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == mCalculateButton){
            mHandler.post(DoOrbitProcessing);
        }else if (view == mMapButton){
            startActivity(new Intent(this, MapActivity.class));
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
