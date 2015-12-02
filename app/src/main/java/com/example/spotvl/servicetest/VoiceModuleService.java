package com.example.spotvl.servicetest;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.net.wifi.WifiManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.example.spotvl.servicetest.Utils.Constants;
import com.example.spotvl.servicetest.Utils.NetworkUtil;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class VoiceModuleService extends IntentService {
  public static final String ACTION = "com.example.spotvl.servicetest.VoiceModuleService";
  public static final String TAG = Constants.TAG;

  Singleton s;

  private WifiManager wifi;
  private WifiManager.MulticastLock multicastLock;
  private AudioRecord audioRecord      ;
  private MulticastSocket sSocket;
  private MulticastSocket rSocket;
  private InetAddress sessAddr;
  //private SocketAddress sa;
  private AudioTrack track;

  private Thread sendingThread;
  private Thread receivingThread;

  // Must create a default constructor
  public VoiceModuleService() {
    // Used to name the worker thread, important only for debugging.
    super("voice module test-service");
  }

  @Override
  public void onCreate() {
    super.onCreate(); // if you override onCreate(), make sure to call super().
    // If a Context object is needed, call getApplicationContext() here.
    Log.d(Constants.TAG,"IntentService onCreate().");

    s = Singleton.getInstance(getApplicationContext());

    IntentFilter filter = new IntentFilter(MainActivity.ACTION);
    LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);

    startForeground(1231234, notificationSetup());

    sendingThread = new Thread(sendingRunnable);
    receivingThread = new Thread(receivingRunnable);

    try {
      sessAddr = InetAddress.getByName(Constants.mMulticastAdress);
      //sa = new InetSocketAddress(sessAddr,Constants.mMulticastPort);
    } catch (UnknownHostException e) {
      e.printStackTrace();
    }

    wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
    multicastLock = wifi.createMulticastLock(TAG);
    multicastLock.setReferenceCounted(true);
    multicastLock.acquire();
    Log.d(TAG, "multicastLock acquired, isHeld " + multicastLock.isHeld());

    createAudioRecorder();

  }


  @Override protected void onHandleIntent(Intent intent){
    // This describes what will happen when service is triggered
    Log.d(Constants.TAG, "IntentService onHandleIntent().");

/*    sendVoice(s.isSendingVoice);
    recvVoice(s.isReceivingVoice);*/

    while (true)
    {

    }
  }

  private void sendVoice(boolean send) {
    Log.d(TAG, "Send voice state : " + send);
    if(sendingThread != null)
      Log.d(TAG, "sendingThread Start State : " + sendingThread.getState().toString());

    if(send)
    {
      if (audioRecord == null) {
        Log.d(TAG, "AudioRecord null in StartSend");
        createAudioRecorder();
        if(audioRecord.getState()==0) {
          Log.d(TAG, "AudioRecord recording state = " + audioRecord.getRecordingState());
          Log.d(TAG, "return from StartSend");
          return;
        }
      }

      Log.d(TAG, "AudioRecord recording state = " + audioRecord.getRecordingState());

      try{
        audioRecord.startRecording();
      } catch (Exception e)
      {
        e.printStackTrace();
      }

      Log.d(TAG, "Start reading from mic.");

      s.isSendingVoice = true;


      if(sendingThread != null && sendingThread.getState() == Thread.State.TERMINATED){
        sendingThread.start();
      } else {
        sendingThread = new Thread(sendingRunnable);
        sendingThread.start();
      }




    } else {

      s.isSendingVoice=false;
        sendingThread.interrupt();
        sendingThread =null;

      try {
        if(audioRecord!=null)
        {
          audioRecord.stop();
          audioRecord.release();
          audioRecord=null;
          Log.d(TAG,"Audio recorder stoped, flushed, released and nulled.");
        }

        if(sSocket!=null) {
          sSocket.leaveGroup(sessAddr);
          sSocket = null;
          Log.d(TAG,"sSocket leaved group and nulled.");
        }

      } catch (Exception e){
        e.printStackTrace();
      }
    }

    if(sendingThread != null)
      Log.d(TAG, "sendingThread Finish State : " + sendingThread.getState().toString());
  }

  Runnable sendingRunnable = new Runnable() {
    @Override public void run() {
      Log.e(TAG, "start send thread, thread id: " + Thread.currentThread().getId());
      try {
        //ni=NetworkUtil.getWlanEth(getApplicationContext());
        sSocket = new MulticastSocket(Constants.mMulticastPort);
        sSocket.setNetworkInterface(s.getNetworkInterface());
        sSocket.joinGroup(new InetSocketAddress(Constants.mMulticastAdress, Constants.mMulticastPort), s.getNetworkInterface());
        Log.d(TAG,
            "sSocket : NI-" + sSocket.getNetworkInterface().getDisplayName() +
                ", Addr-" + Inet4Address.getByName(sSocket.getInterface().getHostAddress()).toString()
                + ", TTL-" + sSocket.getTimeToLive());

        byte[] myBuffer = new byte[Constants.BUF_SIZE];
        int readCount;

        while (s.isSendingVoice)
        {
          readCount = audioRecord.read(myBuffer, 0, Constants.BUF_SIZE);
          DatagramPacket sPack = new DatagramPacket(myBuffer, readCount, sessAddr, Constants.mMulticastPort);
          sSocket.setSendBufferSize(sPack.getLength());
          sSocket.send(sPack);

          Thread.sleep(Constants.SAMPLE_INTERVAL, 0);
        }
      } catch (IOException | IllegalArgumentException | InterruptedException e) {
        Log.d(TAG, "send thread ex :" + e.getMessage());
        e.printStackTrace();
      }

    }
  };

  private void recvVoice(boolean recv) {
    Log.d(TAG, "Recv voice state : " + recv);
    if(receivingThread != null) {
      Log.d(TAG, "receivingThread Start State : " + receivingThread.getState().toString());
      Log.d(TAG, "receivingThread id : " + receivingThread.getId());
    }


    if(recv){

      s.isReceivingVoice = true;

      if(receivingThread != null && receivingThread.getState() == Thread.State.TERMINATED){
        receivingThread.start();
        Log.d(TAG, "receivingThread RESTART");
      } else {
        receivingThread = new Thread(receivingThread);
        receivingThread.start();
        Log.d(TAG, "receivingThread START FROM NEW");
      }


    } else {
      s.isReceivingVoice=false;
        receivingThread.interrupt();
      receivingThread = null;


      try {
        if(rSocket!=null){
          rSocket.leaveGroup(sessAddr);
          rSocket=null;
          Log.d(TAG,"rSocket leaved group and nulled.");
        }
        if(track!=null)
        {
          track.stop();
          track.flush();
          track.release();
          track=null;

          Log.d(TAG,"Audio player stopped, flushed, released and nilled.");
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    if(receivingThread != null)
      Log.d(TAG, "receivingThread Finish State : " + receivingThread.getState().toString());



  }

  Runnable receivingRunnable = new Runnable() {
    @Override public void run() {
      Log.e(TAG, "start recv thread, thread id: " + Thread.currentThread().getId());


      track = new AudioTrack(AudioManager.STREAM_MUSIC,
          Constants.SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
          AudioFormat.ENCODING_PCM_16BIT, Constants.BUF_SIZE,
          AudioTrack.MODE_STREAM);

      Log.d(TAG, "Audio player state : " + track.getState());

      track.play();

      try{
        //ni= NetworkUtil.getWlanEth(getApplicationContext());
        rSocket = new MulticastSocket(Constants.mMulticastPort);
        rSocket.setNetworkInterface(s.getNetworkInterface());
        rSocket.joinGroup(
            new InetSocketAddress(Constants.mMulticastAdress, Constants.mMulticastPort),
            s.getNetworkInterface());
        Log.d(TAG, "mulicastLock isHeld : " + multicastLock.isHeld());
        Log.d(TAG, "rSocket : NI-"+rSocket.getNetworkInterface().getDisplayName()+", Addr-"+ rSocket.getInterface().getHostAddress()+", TTL-"+rSocket.getTimeToLive());

        byte[] buf = new byte[Constants.BUF_SIZE];
        DatagramPacket rPack = new DatagramPacket(buf, Constants.BUF_SIZE);

        InetAddress myIp = InetAddress.getByName(NetworkUtil.getInet4NetworkAddress(s.getNetworkInterface()));

        while (s.isReceivingVoice)
        {
          rPack.setLength(buf.length);
          rSocket.receive(rPack);

          //if(!rPack.getAddress().equals(myIp)) {
          Log.d(TAG,
              "ip : " + myIp.getHostAddress() + " sender : " + rPack.getAddress().getHostAddress());
          track.write(rPack.getData(), 0, rPack.getLength());
          //}
        }
      }
      catch (IOException e) {
        e.printStackTrace();
      }
    }
  };

  private void createAudioRecorder() {
    Log.d(TAG, "minInternalBufferSize = " + Constants.minInternalBufferSize
        + ", BUF_SIZE = " + Constants.BUF_SIZE
        + ", INT_BUF_SIZE = "+ Constants.INT_BUF_SIZE);

    audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, Constants.SAMPLE_RATE,    Constants.CHANEL_IN_CONFIG, Constants.AUDIO_FORMAT,Constants.INT_BUF_SIZE);

    Log.d(TAG, "AudioRecord init state = " + audioRecord.getState());

  }

  private Notification notificationSetup()
  {
    Notification.Builder mBuilder = new Notification.Builder(this);

    mBuilder.setSmallIcon(R.drawable.ic_hearing_24dp);
    mBuilder.setContentTitle("Road Intercom");
    try {
      mBuilder.setContentText(NetworkUtil.getInet4NetworkAddress(s.getNetworkInterface()));
    } catch (SocketException e) {
      mBuilder.setContentText("IP unknown");
      e.printStackTrace();
    }

    // First let's define the intent to trigger when notification is selected
    // Start out by creating a normal intent (in this case to open an activity)
    Intent intent = new Intent(this, MainActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

    // Next, let's turn this into a PendingIntent using
    //   public static PendingIntent getActivity(Context context, int requestCode,
    //       Intent intent, int flags)
    int requestID = (int) System.currentTimeMillis(); //unique requestID to differentiate between various notification with same NotifId
    int flags = PendingIntent.FLAG_CANCEL_CURRENT;
    PendingIntent pIntent = PendingIntent.getActivity(this, requestID, intent, flags);

    // Now we can attach this to the notification using setContentIntent
    mBuilder.setContentIntent(pIntent);


    int flagsSP = PendingIntent.FLAG_UPDATE_CURRENT;

    Intent iSoundProp = new Intent("VoiceModule_Voice_Send");
    iSoundProp.putExtra ("VoiceSendChange", false );
    requestID = (int) System.currentTimeMillis();
    PendingIntent piVoiceSendChange = PendingIntent.getBroadcast(this, requestID, iSoundProp, flagsSP);
    mBuilder.addAction(R.drawable.ic_mic_none_24dp, "Voice Send switch", piVoiceSendChange);

    Intent iRecvProp = new Intent("VoiceModule_Voice_Send");
    iRecvProp.putExtra("ReceiveVoiceChange", false );
    requestID = (int) System.currentTimeMillis();
    PendingIntent piReceiveVoiceChange = PendingIntent.getBroadcast(this, requestID, iRecvProp, flagsSP);
    mBuilder.addAction(R.drawable.ic_mic_none_24dp,"Receive Voice switch", piReceiveVoiceChange);

    return mBuilder.build();
  }


  private BroadcastReceiver receiver = new BroadcastReceiver() {
    @Override public void onReceive(Context context, Intent intent) {

      Log.d(Constants.TAG,"IntentService receiver onReceive().");

      boolean swSend,swRecv;
      swSend=swRecv=false;

      if(intent.hasExtra("swSend"))
      {
        swSend = intent.getBooleanExtra("swSend", false);
        sendVoice(swSend);
      }
      if(intent.hasExtra("swRecv"))
      {
        swRecv = intent.getBooleanExtra("swRecv", false);
        recvVoice(swRecv);
      }

      Intent in = new Intent(ACTION);
      in.putExtra("resultValue", "Send back switchers state: " + swSend + " " + swRecv);
      LocalBroadcastManager.getInstance(VoiceModuleService.this).sendBroadcast(in);

    }
  };

  @Override public void onDestroy() {
    super.onDestroy();

    Log.d(Constants.TAG, "IntentService onDestroy().");

    sendVoice(false);
    recvVoice(false);

    // Unregister the listener when the application is paused
    LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    // or `unregisterReceiver(testReceiver)` for a normal broadcast
  }
}
