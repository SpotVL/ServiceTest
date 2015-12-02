package com.example.spotvl.servicetest;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.spotvl.servicetest.Utils.NetworkUtil;
import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Singleton {

  private Context appContext;
  private static Singleton sSingleton;

  private NetworkInterface networkInterface;
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

    SharedPreferences mSettings = appContext.getSharedPreferences("Settings", 0);
    String ipStored = mSettings.getString("Ip4Address", "null");

    if(ipStored.equals("null"))
    {
      networkInterface = NetworkUtil.getWlanEth(appContext);
    } else {
      try {
        networkInterface = NetworkInterface.getByInetAddress(Inet4Address.getByName(ipStored));
      } catch (SocketException e) {
        networkInterface = NetworkUtil.getWlanEth(appContext);
        e.printStackTrace();
      } catch (UnknownHostException e) {
        networkInterface = NetworkUtil.getWlanEth(appContext);
        e.printStackTrace();
      }
    }


    isSendingVoice=false;
    isReceivingVoice=false;

  }

  public NetworkInterface getNetworkInterface() {
    return networkInterface;
  }

  public void setNetworkInterface(NetworkInterface networkInterface) {
    this.networkInterface = networkInterface;

    SharedPreferences mSettings = appContext.getSharedPreferences("Settings", 0);
    SharedPreferences.Editor editor = mSettings.edit();
    if(networkInterface != null){
      String ip;
      try {
        ip = NetworkUtil.getInet4NetworkAddress(networkInterface);
      } catch (SocketException e) {
        ip = "null";
        e.printStackTrace();
      }
      editor.putString("Ip4Address", ip);
    }else{
      editor.putString("Ip4Address", "null");
    }


  }
}
