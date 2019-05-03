package android.app.pelagaggi.gsm;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.os.Binder;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.CellIdentityGsm;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private LocationManager lm;
    public TelephonyManager teleephonyManager;
    TextView scanResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        scanResult = findViewById(R.id.scanResult);
        ((Button) findViewById(R.id.scanBtn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("wifi", "onclick");
                scanGsm();
            }
        });
        lm = (LocationManager) getApplicationContext().getSystemService(this.getBaseContext().LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(myIntent);
        }
        teleephonyManager = (TelephonyManager) getApplicationContext().getSystemService(this.getBaseContext().TELEPHONY_SERVICE);
        scanGsm();
    }

    private void scanGsm() {
        Log.d("gsm", "scanGsm");

        Binder.clearCallingIdentity();
        new Timer().scheduleAtFixedRate(new TimerTask(){

            @Override
            public void run(){
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 99);
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }

                List<CellInfo> cellInfoList = teleephonyManager.getAllCellInfo();
                Iterator iterator = cellInfoList.iterator();
                ArrayList<String>JSONarr = new ArrayList<>();
                while(iterator.hasNext()) {
                    try {
                        CellInfoGsm cellInfo= (CellInfoGsm) iterator.next();

                        CellIdentityGsm identity = cellInfo.getCellIdentity();
                        CellSignalStrengthGsm signalStrength = cellInfo.getCellSignalStrength();
                        String json = String.format("{\"CellIdentity\":%d,\"CellSignalStrengthGsm\":{\"dbm\":%d,\"rssi\":%d,\"level\":%d}}",
                                identity.getCid(),
                                signalStrength.getDbm(),
                                signalStrength.getAsuLevel(),
                                signalStrength.getLevel()
                        );
                        JSONarr.add(json);
                        Log.d("gsm",json);
                    }catch (Exception e){
                        //Log.d("gsm","Error:\t"+e.toString());
                    }
                }
                Log.d("gsm",JSONarr.toString());
                scanResult.setText(JSONarr.toString());
                this.cancel();
            }
        },0,500);

        //scanResult.setText(cellInfoList.toString());
    }

}
