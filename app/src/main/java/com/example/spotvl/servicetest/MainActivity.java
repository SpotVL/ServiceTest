package com.example.spotvl.servicetest;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.spotvl.servicetest.Utils.NetworkUtil;
import java.net.NetworkInterface;
import java.net.SocketException;
import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

  public static final String ACTION = "com.example.spotvl.servicetest.MainActivity";

  Singleton s;

  SwitchCompat swRecv, swSend;
  TextView statusWindow;


  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    initUI();



  }


  private void updateUI()
  {
    try {
      addText((s.NetworkIntface == null) ? "": NetworkUtil.getInet4NetworkAddress(s.NetworkIntface));
    } catch (SocketException e) {
      e.printStackTrace();
    }

    swSend.setChecked(s.isSendingVoice);
    swRecv.setChecked(s.isReceivingVoice);

  }


  private void initUI()
  {
    getSupportActionBar().setTitle("Voice module service test");

    s = Singleton.getInstance(getApplicationContext());

    statusWindow = (TextView) findViewById(R.id.info_window);
    swSend = (SwitchCompat)findViewById(R.id.switch_send_voice);
    swRecv = (SwitchCompat)findViewById(R.id.switch_recv_voice);

    swSend.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        s.isSendingVoice = swSend.isChecked();
        sendToService();
      }
    });

    swRecv.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        s.isReceivingVoice = swRecv.isChecked();
        sendToService();
      }
    });

    findViewById(R.id.btn_start_vm_service).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        launchVoiceModuleService();
      }
    });

    findViewById(R.id.btn_stop_vm_service).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        stopVoiceModuleService();
      }
    });

    updateUI();
  }

  private void sendToService(){
    Intent in = new Intent(ACTION);
    in.putExtra("swSend", swSend.isChecked());
    in.putExtra("swRecv", swRecv.isChecked());
    LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(in);
  }


  //receive from Service
  private BroadcastReceiver voiceModuleReceiver = new BroadcastReceiver() {
    @Override public void onReceive(Context context, Intent intent) {

      if (intent.hasExtra("resultValue")) {
        String resultValue = intent.getStringExtra("resultValue");
        addText(resultValue);
      }
    }
  };

  private void addText(String newTxt) {

    statusWindow.append("\n"+newTxt);
    findViewById(R.id.scroll_status).post(new Runnable() {
      @Override public void run() {
        ((ScrollView)findViewById(R.id.scroll_status)).smoothScrollTo(0,statusWindow.getBottom());
      }
    });
  }

  // Call `launchVoiceModuleService()` in the activity
  // to startup the service
  public void launchVoiceModuleService() {
    // Construct our Intent specifying the Service
    Intent i = new Intent(this, VoiceModuleService.class);
    // Start the service
    startService(i);
  }

  // Call `stopVoiceModuleService()` in the activity
  // to startup the service
  public void stopVoiceModuleService() {
    // Construct our Intent specifying the Service
    Intent i = new Intent(this, VoiceModuleService.class);
    // Stop the service
    stopService(i);
  }

  @Override protected void onResume() {
    super.onResume();

    updateUI();

    // Register for the particular broadcast based on ACTION string
    IntentFilter filter = new IntentFilter(VoiceModuleService.ACTION);

    LocalBroadcastManager.getInstance(this).registerReceiver(voiceModuleReceiver, filter);
    // or `registerReceiver(testReceiver, filter)` for a normal broadcast
  }

  @Override protected void onPause() {


    super.onPause();

    // Unregister the listener when the application is paused
    LocalBroadcastManager.getInstance(this).unregisterReceiver(voiceModuleReceiver);
    // or `unregisterReceiver(testReceiver)` for a normal broadcast
  }


  @Override public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    getMenuInflater().inflate(R.menu.main_menu, menu);
    return true;
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    super.onOptionsItemSelected(item);

    switch (item.getItemId()){
      case R.id.menu_networks:
        Intent intent = new Intent(this, NetworksActivity.class);
        startActivity(intent);
        return true;
    }
    return false;
  }

  @Override public void onSaveInstanceState(Bundle outState) {

    Log.d(Singleton.TAG, "MainActivity onSaveInstanceState()");

    String txtToSave = statusWindow.getText().toString();
    outState.putString("statusWindow", txtToSave);

    super.onSaveInstanceState(outState);
  }

  @Override protected void onRestoreInstanceState(Bundle savedInstanceState) {
    Log.d(Singleton.TAG, "MainActivity onRestoreInstanceState()");
    super.onRestoreInstanceState(savedInstanceState);

    statusWindow.setText(savedInstanceState.getString("statusWindow","oops"));
  }
}
