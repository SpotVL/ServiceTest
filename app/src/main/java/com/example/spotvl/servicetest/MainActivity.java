package com.example.spotvl.servicetest;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.example.spotvl.servicetest.Utils.NetworkUtil;
import java.net.NetworkInterface;
import java.net.SocketException;
import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

  Singleton s;

  SwitchCompat swRecv, swSend;

  VoiceModuleService voiceModuleService;
  ServiceConnection serviceConnection = new ServiceConnection() {
    @Override public void onServiceConnected(ComponentName name, IBinder service) {
      voiceModuleService = ((VoiceModuleService.MyBinder)service).getService();
    }

    @Override public void onServiceDisconnected(ComponentName name) {
      voiceModuleService = null;
    }
  };


  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    getSupportActionBar().setTitle("Voice module service test");

    Intent bindIntent = new Intent(MainActivity.this, VoiceModuleService.class);
    bindService(bindIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    startService(bindIntent);

    s = Singleton.getInstance(getApplicationContext());
    swSend = (SwitchCompat)findViewById(R.id.switch_send_voice);
    swRecv = (SwitchCompat)findViewById(R.id.switch_recv_voice);

    swSend.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        s.isSendingVoice = swSend.isChecked();
      }
    });

    swRecv.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        s.isReceivingVoice = swRecv.isChecked();
      }
    });

    updateUI();
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

  private void updateUI()
  {
    try {
      ((TextView)findViewById(R.id.info_window)).setText((s.NetworkIntface==null)?"": NetworkUtil.getInet4NetworkAddress(s.NetworkIntface));
    } catch (SocketException e) {
      e.printStackTrace();
    }

    swSend.setChecked(s.isSendingVoice);
    swRecv.setChecked(s.isReceivingVoice);

  }

  private void initUI()
  {

  }

  @Override protected void onResume() {
    super.onResume();

    updateUI();
  }
}
