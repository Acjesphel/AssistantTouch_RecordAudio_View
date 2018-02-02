package com.acj.assistanttouchview;

import android.media.MediaRecorder;
import android.os.Environment;

import java.io.File;
import java.io.IOException;

/**
 * 获取声音工具，主要获取声音，计算出分贝值
 * Created by sharon on 2018/1/27.
 */

public class VoiceRecordHelper {

    private MediaRecorder mediaRecorder;
    private VoiceChangedListener listener;
    private boolean isRunning;

    public VoiceRecordHelper(VoiceChangedListener l){
        this.listener = l;
    }

    private void initMediaRecorder(){
        try {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            mediaRecorder.setOutputFile(Environment.getExternalStorageDirectory() +
                    File.separator +
                    System.currentTimeMillis() + ".mp4");
            mediaRecorder.setMaxDuration(1000 * 60 * 10);
            mediaRecorder.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startRecord(){
        stopRecord();
        initMediaRecorder();
        mediaRecorder.start();
        isRunning = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRunning) {
                    double db = getDB();
                    if (listener != null) {
                        listener.onVolumeChanged((int)db);
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public synchronized void stopRecord(){
        isRunning = false;
        if (mediaRecorder != null) {
            mediaRecorder.release();
        }
        mediaRecorder = null;
    }

    /**
     * 分贝计算公式
     */
    private synchronized double getDB(){
        if (mediaRecorder == null) return 0;
        if (isRunning == false) return 0;
        double ratio = mediaRecorder.getMaxAmplitude() / 100;
        double db = 0;
        if (ratio > 1) {
            db = 20 * Math.log10(ratio);
        }

        return db;
    }

    public interface VoiceChangedListener{
        void onVolumeChanged(int db);
    }
}
