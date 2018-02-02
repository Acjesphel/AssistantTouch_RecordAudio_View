package com.acj.assistanttouchview;

import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private IMSpeechLayout imSpeechLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imSpeechLayout = (IMSpeechLayout) findViewById(R.id.im_speech_layout);
        imSpeechLayout.setFloatTalkViewListener(new FloatTalkView.OnFloatTalkEvent() {
            @Override
            public void clickOnQuestion() {
                Toast.makeText(MainActivity.this, "?",Toast.LENGTH_SHORT).show();
            }
        });

    }


    @Override
    protected void onResume() {
        super.onResume();
        imSpeechLayout.setActivity(MainActivity.this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == IMSpeechLayout.PERMISSIONS_RECORD_AUDIO) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                imSpeechLayout.startTalk();
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        //页面onPause必须要暂停录音
        imSpeechLayout.onPause();
    }
}
