package com.example.spotvl.servicetest;

import android.content.Context;
import java.net.NetworkInterface;


public class Singleton {

  private Context appContext;
  private static Singleton sSingleton;
  public static final String  TAG= "com.example.spotvl.servicetest";

  public NetworkInterface NetworkIntface;
  public boolean isSendingVoice;
  public boolean isReceivingVoice;



  public static Singleton getInstance(Context c) {

    if(sSingleton == null)
    {
      sSingleton = new Singleton(c.getApplicationContext());
    }
    return sSingleton;
  }

  private Singleton(Context appContext) {
    this.appContext = appContext;

    NetworkIntface = null;
    isSendingVoice=false;
    isReceivingVoice=false;

  }
}
