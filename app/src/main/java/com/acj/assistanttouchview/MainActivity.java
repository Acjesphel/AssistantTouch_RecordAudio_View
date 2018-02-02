package com.acj.assistanttouchview;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static final int PERMISSIONS_RECORD_AUDIO = 1;

    private FloatTalkView talkLayout;
    private FloatButtonLayout buttonLayout;
    private VoiceRecordHelper voiceRecordHelper; //声音获取工具

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        talkLayout = (FloatTalkView) findViewById(R.id.dialog_speech_talk);
        buttonLayout = (FloatButtonLayout)findViewById(R.id.layout_talk);
        buttonLayout.setFollowView(talkLayout);
        buttonLayout.setOnButtonTouchEvent(new SpeechButton.OnButtonTouch() {
            @Override
            public void touchDown() {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            PERMISSIONS_RECORD_AUDIO);
                } else {
                    //开启弹窗
                    talkLayout.showView();
                    buttonLayout.afterShowTalkView();
                    voiceRecordHelper.startRecord();
                }

            }

            @Override
            public void touchUp() {
                Log.e("PRESS", "button up");
                //关闭弹窗
                talkLayout.dismissView();
                buttonLayout.afterHideTalkView();
            }
        });

        voiceRecordHelper = new VoiceRecordHelper(new VoiceRecordHelper.VoiceChangedListener() {
            @Override
            public void onVolumeChanged(final int db) {
//                Log.e("DB",db + "db");
                int value = 0;
                if (db < 20) {//去除噪音
                    value = 0;
                } else {
                    value = db;
                }
                talkLayout.setVolume(value);
            }
        });


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_RECORD_AUDIO) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //开启弹窗
                talkLayout.showView();
                buttonLayout.afterShowTalkView();
                voiceRecordHelper.startRecord();
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        //页面onPause必须要暂停录音
        talkLayout.dismissView();
        buttonLayout.afterHideTalkView();
        voiceRecordHelper.stopRecord();
    }
}
