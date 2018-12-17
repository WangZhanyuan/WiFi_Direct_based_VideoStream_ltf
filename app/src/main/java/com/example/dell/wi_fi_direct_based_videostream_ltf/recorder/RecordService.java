package com.example.dell.wi_fi_direct_based_videostream_ltf.recorder;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaMuxer;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.HandlerThread;
import android.os.IBinder;
import android.widget.Toast;

import com.googlecode.javacv.FFmpegFrameRecorder;

import java.io.File;
import java.io.IOException;


public class RecordService extends Service {
  private MediaProjection mediaProjection;
  private MediaRecorder mediaRecorder;
  private MediaCodec mediaCodec;
  private MediaMuxer mediaMuxer;
  private VirtualDisplay virtualDisplay;

  private boolean running;
  private int width = 720;
  private int height = 1080;
  private int dpi;
  @Override
  public IBinder onBind(Intent intent) {
    return new RecordBinder();
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    return START_STICKY;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    HandlerThread serviceThread = new HandlerThread("service_thread",
        android.os.Process.THREAD_PRIORITY_BACKGROUND);
    serviceThread.start();
    running = false;
    mediaRecorder = new MediaRecorder();

  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }

  public void setMediaProject(MediaProjection project) {
    mediaProjection = project;
  }

  public boolean isRunning() {
    return running;
  }

  public void setConfig(int width, int height, int dpi) {
    this.width = width;
    this.height = height;
    this.dpi = dpi;
  }

  public boolean startRecord() {
    if (mediaProjection == null || running) {
      return false;
    }
    initRecorder();
    createVirtualDisplay();
    mediaRecorder.start();
    running = true;
    return true;
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)//适用于API在21以上的手机
  public boolean stopRecord() {
    if (!running) {
      return false;
    }
    running = false;
    mediaRecorder.stop();
    mediaRecorder.reset();
    virtualDisplay.release();
    mediaProjection.stop();

    return true;
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  private void createVirtualDisplay() {
    virtualDisplay = mediaProjection.createVirtualDisplay("MainScreen", width, height, dpi,
        DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mediaRecorder.getSurface(), null, null);
  }

  /**
   * 初始化函数
   */
  private void initRecorder() {
    mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
    mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
    mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
    mediaRecorder.setOutputFile(getsaveDirectory() + System.currentTimeMillis() + ".mp4");
    mediaRecorder.setVideoSize(width, height);
    mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
    mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
    mediaRecorder.setVideoEncodingBitRate(5*1024*1024);//关乎视频的清晰度，可设计动态码率调节算法
    mediaRecorder.setVideoFrameRate(24);//人的肉眼所能观看到的极限

    try {
      mediaRecorder.prepare();
    } catch (IOException e) {
      e.printStackTrace();

    }
  }
  @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
  public void initmyRecoder(){
    mediaCodec.signalEndOfInputStream();
  }

  public String getsaveDirectory() {
    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
      String rootDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "ScreenRecord" + "/";

      File file = new File(rootDir);
      if (!file.exists()) {
        if (!file.mkdirs()) {
          return null;
        }
      }
      Toast.makeText(getApplicationContext(), rootDir, Toast.LENGTH_SHORT).show();
      return rootDir;
    } else {
      return null;
    }
  }

  public class RecordBinder extends Binder {
    public RecordService getRecordService() {
      return RecordService.this;
    }
  }
}