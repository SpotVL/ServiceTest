package com.example.spotvl.servicetest;

import android.app.Activity;
import android.app.IntentService;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class VoiceModuleService extends IntentService {
  public static final String ACTION = "com.example.spotvl.servicetest.VoiceModuleService";

  Singleton s;

  // Must create a default constructor
  public VoiceModuleService() {
    // Used to name the worker thread, important only for debugging.
    super("voice module test-service");
  }

  @Override
  public void onCreate() {
    super.onCreate(); // if you override onCreate(), make sure to call super().
    // If a Context object is needed, call getApplicationContext() here.
    Log.d(Singleton.TAG,"IntentService onCreate().");

    s = Singleton.getInstance(getApplicationContext());

    IntentFilter filter = new IntentFilter(MainActivity.ACTION);
    LocalBroadcastManager.getInstance(this).registerReceiver(receiver,filter);
  }


  @Override protected void onHandleIntent(Intent intent){
    // This describes what will happen when service is triggered
    Log.d(Singleton.TAG,"IntentService onHandleIntent().");


    startForeground(1231234, new Notification());

    while (true)
    {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  private BroadcastReceiver receiver = new BroadcastReceiver() {
    @Override public void onReceive(Context context, Intent intent) {

      Log.d(Singleton.TAG,"IntentService receiver onReceive().");

      boolean swSend,swRecv;
      swSend=swRecv=false;

      if(intent.hasExtra("swSend"))
      {
        swSend = intent.getBooleanExtra("swSend", false);
      }
      if(intent.hasExtra("swRecv"))
      {
        swRecv = intent.getBooleanExtra("swRecv", false);
      }

      Intent in = new Intent(ACTION);
      in.putExtra("resultValue", "Send back switchers state: " + swSend + " " + swRecv);
      LocalBroadcastManager.getInstance(VoiceModuleService.this).sendBroadcast(in);

    }
  };

  @Override public void onDestroy() {
    super.onDestroy();

    Log.d(Singleton.TAG, "IntentService onDestroy().");

    // Unregister the listener when the application is paused
    LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    // or `unregisterReceiver(testReceiver)` for a normal broadcast
  }
}
