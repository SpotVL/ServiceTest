package com.example.spotvl.servicetest;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.example.spotvl.servicetest.Utils.NetworkUtil;
import java.net.NetworkInterface;
import java.util.ArrayList;

public class NetworksActivity extends AppCompatActivity {

  Singleton s;

  public ActiveNetworksListViewAdapter activeNetworksAdapter;

  ListView listViewActiveNetworks;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_networks);

    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setTitle("Networks");

    s = Singleton.getInstance(getApplicationContext());

    ArrayList<NetworkInterface> networkInterfaces = NetworkUtil.updateNetworkInterfaces();
    activeNetworksAdapter = new ActiveNetworksListViewAdapter(this, networkInterfaces );

    //List of active networks updates by CONNECTIVITY_ACTION intent.
    listViewActiveNetworks = (ListView) findViewById(R.id.list_view_avail_nets);
    listViewActiveNetworks.setAdapter(activeNetworksAdapter);
    // listener active network list click
    listViewActiveNetworks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Context c = getApplicationContext();

        String msg = activeNetworksAdapter.getItem(position).toString();
        Singleton.getInstance(c).NetworkIntface = activeNetworksAdapter.getItem(position);
        activeNetworksAdapter.notifyDataSetChanged();

        finish();
      }
    });
  }

  @Override protected void onResume() {
    super.onResume();
    activeNetworksAdapter.updateNetworks(NetworkUtil.updateNetworkInterfaces());
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.networks_menu, menu);
    return true;
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    switch (id) {
      case R.id.atn_discover:
        activeNetworksAdapter.updateNetworks(NetworkUtil.updateNetworkInterfaces());
        return true;
    }
    return  false;
  }
}
