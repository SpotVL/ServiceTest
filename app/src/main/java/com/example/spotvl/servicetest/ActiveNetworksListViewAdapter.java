package com.example.spotvl.servicetest;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import com.example.spotvl.servicetest.Utils.NetworkUtil;
import com.example.spotvl.servicetest.Utils.WifiApControl;
import com.example.spotvl.servicetest.Utils.NetworkDevice;

public class ActiveNetworksListViewAdapter extends BaseAdapter {

  private final String NET_NAME_LOCAL = "lo";
  private final String NET_NAME_WLAN = "wlan";
  private final String NET_NAME_MOBILE = "rmnet";
  private final String NET_NAME_P2P = "p2p";

  Context context;
  ArrayList<NetworkInterface> networkInterfaces;
  LayoutInflater inflater;

  public ActiveNetworksListViewAdapter(Context context,
      ArrayList<NetworkInterface> networkInterfaces) {
    this.context = context;
    this.networkInterfaces = networkInterfaces;
    this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }

  @Override public int getCount() {
    try {
      int size = networkInterfaces.size();
      return size;
    } catch(NullPointerException ex) {
      return 0;
    }
  }

  @Override public NetworkInterface getItem(int position) {
    return networkInterfaces.get(position);
  }

  @Override public long getItemId(int position) {
    return position;
  }

  public void updateNetworks(ArrayList<NetworkInterface> networkInterfaces)
  {
    this.networkInterfaces.clear();
    this.networkInterfaces.addAll(networkInterfaces);
    this.notifyDataSetChanged();
  }

  @Override public View getView(int position, View convertView, ViewGroup parent) {
    View view = convertView;

    NetworkInterface act = Singleton.getInstance(context).NetworkIntface;
    NetworkInterface ni = getItem(position);

    if(view == null)
    {
      view = inflater.inflate(R.layout.list_item_active_networks, parent, false);
    }

    ((TextView) view.findViewById(R.id.list_item_network_name)).
        setText(getDisplayNetworkName(ni.getName()));

    ((TextView) view.findViewById(R.id.list_item_network_type)).
        setText(getNetworkName(ni.getName()));

    try {
      ((TextView) view.findViewById(R.id.list_item_network_ip)).setText(NetworkUtil.getInet4NetworkAddress(ni) + ", " + ni.getName());
    } catch (SocketException | NullPointerException e) {
      ((TextView) view.findViewById(R.id.list_item_network_ip)).setText("0.0.0.0");
      e.printStackTrace();
    }

    if(ni.equals(act)){
      ((TextView) view.findViewById(R.id.list_item_network_usage)).setText("Used");
    } else {
      ((TextView) view.findViewById(R.id.list_item_network_usage)).setText("Not used");
    }

    return view;
  }

  private String getDisplayNetworkName(String netName) {

    WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);

    if (netName.contains(NET_NAME_LOCAL)) {
      return "localhost";
    } else if (netName.contains(NET_NAME_MOBILE)) {
      return "Mobile network";
    } else if (netName.contains(NET_NAME_WLAN)) {
      if (NetworkUtil.isHotspotOn(context)) {

        WifiConfiguration apConfig = WifiApControl.getApControl(
            wifiManager).getWifiApConfiguration();

        return apConfig.SSID;
      } else {
        String wifiName = wifiManager.getConnectionInfo().getSSID();


        wifiName = wifiName.substring(1,wifiName.length()-1);

        return wifiName;
      }
    }  else {
      return netName;
    }
  }

  private String getNetworkName(String netName) {
    if (netName.contains(NET_NAME_LOCAL)) {
      return "localhost";
    } else if (netName.contains(NET_NAME_MOBILE)) {
      return "Mobile network";
    } else if (netName.contains(NET_NAME_WLAN)) {
      if (NetworkUtil.isHotspotOn(context)) {
        return "Hotspot";
      } else {
        return "WiFi";
      }
    }  else {
      return netName;
    }
  }
}
