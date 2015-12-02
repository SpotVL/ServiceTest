package com.example.spotvl.servicetest.Utils;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.net.wifi.WifiManager;
import java.net.MulticastSocket;
import java.net.NetworkInterface;

/**
 *
 * Constants used by multiple classes in this package
 */
public final class Constants {

  public static final int SAMPLE_RATE = 8000;
  public static final int SAMPLE_INTERVAL = 20; // milliseconds
  public static final int SAMPLE_SIZE = 2; // bytes per sample
  public static final int CHANEL_IN_CONFIG =  AudioFormat.CHANNEL_IN_MONO;
  public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
  public static final int minInternalBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,CHANEL_IN_CONFIG, AUDIO_FORMAT);
  public static final int BUF_SIZE = SAMPLE_INTERVAL*SAMPLE_INTERVAL*SAMPLE_SIZE*2;
  public static final int INT_BUF_SIZE = minInternalBufferSize * 2;

  public static final  int mMulticastPort = 5513;
  public static final String mMulticastAdress = "224.0.0.13";

  public static final String TAG = "com.example.spotvl.servicetest";

}
