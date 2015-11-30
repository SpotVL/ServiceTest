package com.example.spotvl.servicetest;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class VoiceModuleService extends Service {

  Singleton s;

  private final IBinder binder = new MyBinder();

  public class MyBinder extends Binder {
    VoiceModuleService getService() {
      return VoiceModuleService.this;
    }
  }

  public static final String TAG = "VOICE_MODULE_SERVICE";

  @Override public void onCreate() {
    super.onCreate();

    s = Singleton.getInstance(getApplicationContext());
  }


  @Nullable @Override public IBinder onBind(Intent intent) {
    return binder;
  }



  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    startBackgroundTask(intent, startId);
    return Service.START_REDELIVER_INTENT;
  }

  private void startBackgroundTask(Intent intent, int startId) {
    int NOTIFICATION_ID = 1;

    Intent intentMainActivity = new Intent(this,MainActivity.class);
    PendingIntent pi = PendingIntent.getActivity(this,1,intentMainActivity,0);


    Notification.Builder builder = new Notification.Builder(this);

    builder.setAutoCancel(false);
    builder.setTicker("this is ticker text");
    builder.setContentTitle("Voice Module Notification");
    builder.setContentText("You have a new message");
    builder.setSmallIcon(R.drawable.ic_network_wifi_24dp);
    builder.setContentIntent(pi);
    builder.setOngoing(true);
    builder.setSubText("This is subtext...");   //API level 16
    builder.setNumber(100);
    builder.build();
    Notification notification = builder.build();

    startForeground(NOTIFICATION_ID, notification);
  }
}
