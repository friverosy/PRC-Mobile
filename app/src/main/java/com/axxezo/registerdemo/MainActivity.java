package com.axxezo.registerdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.device.ScanManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //declaration
    private TextView VERSION;
    private EditText editText_dni;
    private String barcodeStr;
    private Vibrator mVibrator;
    private ScanManager mScanManager;
    private final static String SCAN_ACTION = "urovo.rcv.message";
    private final static String baseUrl = "http://192.168.1.115:3000/api/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //content views call
        VERSION = (TextView) findViewById(R.id.version);
        editText_dni = (EditText) findViewById(R.id.editText_dni);
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //put version application in textview;
        VERSION.setText(getApplicationVersionString(this));
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private BroadcastReceiver mScanReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            // TODO Auto-generated method stub
            try {
                DatabaseHelper db=DatabaseHelper.getInstance(getApplicationContext());
                mVibrator.vibrate(100);
                reset();
                byte[] barcode = intent.getByteArrayExtra("barocode");
                int barocodelen = intent.getIntExtra("length", 0);
                byte barcodeType = intent.getByteExtra("barcodeType", (byte) 0);
                barcodeStr = new String(barcode, 0, barocodelen);
                String rawCode = barcodeStr;

                if (barcodeType == 28) {
                    if (barcodeStr.startsWith("https://")) { // Its a new DNI Cards.
                        barcodeStr = barcodeStr.substring(
                                barcodeStr.indexOf("RUN=") + 4,
                                barcodeStr.indexOf("&type"));
                        // Remove DV.
                        barcodeStr = barcodeStr.substring(0, barcodeStr.indexOf("-") + 2);
                        editText_dni.setText(barcodeStr.trim());
                        db.insert("insert into registers (person, date, pda, sync) values ('"+barcodeStr.trim()+"',"+new Date().getTime()+", '"+Build.SERIAL+"', 0)");

                    } else
                        editText_dni.setError("cedula o pasaporte incorrecto, verifique");

                } else if (barcodeType == 17) { // PDF417->old dni // 1.- validate if the rut is > 10 millions
                    //else old dni
                    String rutValidator = barcodeStr.substring(0, 9);
                    rutValidator = rutValidator.replace(" ", "");
                    boolean isvalid = validateRut(rutValidator);
                    if (isvalid) {
                        barcodeStr = rutValidator;
                        rutValidator = rutValidator.substring(0, rutValidator.length() - 1) + "-" + rutValidator.substring(rutValidator.length() - 1);
                        editText_dni.setText(rutValidator);
                        db.insert("insert into registers (person, date, pda, sync) values ('"+barcodeStr.trim()+"',"+new Date().getTime()+", '"+Build.SERIAL+"', 0)");
                    } else { //try validate rut size below 10.000.000
                        rutValidator = barcodeStr.substring(0, 8);
                        rutValidator = rutValidator.replace(" ", "");
                        isvalid = validateRut(rutValidator);
                        if (isvalid) {
                            barcodeStr = rutValidator;
                            rutValidator = rutValidator.substring(0, rutValidator.length() - 1) + "-" + rutValidator.substring(rutValidator.length() - 1);
                            editText_dni.setText(rutValidator);
                            db.insert("insert into registers (person, date, pda, sync) values ('"+rutValidator.trim()+"',"+new Date().getTime()+", '"+Build.SERIAL+"', 0)");
                        } else {
                            // log.writeLog(getApplicationContext(), "Main:line 412", "ERROR", "rut invalido " + barcodeStr);
                            barcodeStr = "";
                            editText_dni.setError("cedula o pasaporte incorrecto, verifique");
                        }
                    }
                }

            } catch (NullPointerException e) {
                e.printStackTrace();
                //     log.writeLog(getApplicationContext(), "Main:line 408", "ERROR", e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                //     log.writeLog(getApplicationContext(), "Main:line 411", "ERROR", e.getMessage());
            }
        }
    };

    public static String getApplicationVersionString(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageInfo(context.getPackageName(), 0);
            return "v" + info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean validateRut(String rut) {

        boolean validacion = false;
        try {
            rut = rut.toUpperCase();
            rut = rut.replace(".", "");
            rut = rut.replace("-", "");
            int rutAux = Integer.parseInt(rut.substring(0, rut.length() - 1));

            char dv = rut.charAt(rut.length() - 1);

            int m = 0, s = 1;
            for (; rutAux != 0; rutAux /= 10) {
                s = (s + rutAux % 10 * (9 - m++ % 6)) % 11;
            }
            if (dv == (char) (s != 0 ? s + 47 : 75)) {
                validacion = true;
            }

        } catch (java.lang.NumberFormatException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return validacion;
    }

    private void initScan() {
        // TODO Auto-generated method stub
        mScanManager = new ScanManager();
        mScanManager.openScanner();
        mScanManager.switchOutputMode(0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mScanManager != null) {
            mScanManager.stopDecode();
        }
        unregisterReceiver(mScanReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initScan();
        IntentFilter filter = new IntentFilter();
        filter.addAction(SCAN_ACTION);
        registerReceiver(mScanReceiver, filter);
        UpdateDb();
    }

    public void reset() {
        try {
            initScan();
            IntentFilter filter = new IntentFilter();
            filter.addAction(SCAN_ACTION);
            registerReceiver(mScanReceiver, filter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void UpdateDb() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    DatabaseHelper db = new DatabaseHelper(getApplicationContext());
                    try {
                        if (db.register_desync_count() >= 1)
                            offlineRegisterSynchronizer();
                        db.close();
                        Thread.sleep(3000); // 5 Min = 300000
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    db.close();
                }
            }
        };
        new Thread(runnable).start();
    }

    public void offlineRegisterSynchronizer() {
        DatabaseHelper db = new DatabaseHelper(this);
        Register[] registers = db.get_desynchronized_registers();
        db.close();

        JSONObject json = new JSONObject();
        for (int i = 0; i <= registers.length-1; i++) {
            try {
                json.put("person", registers[i].person);
                json.put("date", registers[i].date);
                json.put("pda", registers[i].pda);
                registerTask rt = new registerTask();
                rt.execute(json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public class registerTask extends AsyncTask<JSONObject,Void, String> {

        @Override
        protected String doInBackground(JSONObject... json) {
            String resp;
            try {

                Http http = new Http();
                String out = json[0].toString()+"";
                resp = http.Post(baseUrl+"registers", out.toString()+"", "application/json; charset=utf-8");

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            return resp;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);

            boolean updated = false;
            if (response != null){
                DatabaseHelper db = DatabaseHelper.getInstance(getApplicationContext());
                try {
                    JSONObject json = new JSONObject(response);
                    updated = db.update_register(json.getLong("date"));
                    // Use updated var to display if is updated or not like WhatsApp.
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
