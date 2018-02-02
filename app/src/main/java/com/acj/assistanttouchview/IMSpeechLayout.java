package com.acj.assistanttouchview;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * 两件事：1、整合弹窗和按钮；2、对外接口设置
 *
 * Created by sharon on 2018/2/1.
 */

public class IMSpeechLayout extends RelativeLayout {

    public static final int PERMISSIONS_RECORD_AUDIO = 1;

    private Context context;
    private FloatTalkView talkLayout;
    private FloatButtonLayout buttonLayout;
    private Activity activity;

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public IMSpeechLayout(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public IMSpeechLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public IMSpeechLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init(){
        inflate(context, R.layout.layout_im_speech, this);
        talkLayout = (FloatTalkView) findViewById(R.id.dialog_speech_talk);
        buttonLayout = (FloatButtonLayout)findViewById(R.id.layout_talk);
        buttonLayout.setOnButtonLayoutEvent(new FloatButtonLayout.OnButtonLayoutEvent() {
            @Override
            public void onButtonMove(final int dialogTop) {
                //更改speech float的浮层位置
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) talkLayout.getLayoutParams();
                layoutParams.topMargin = dialogTop;
                talkLayout.setLayoutParams(layoutParams);
            }

            @Override
            public void onButtonClick() {
                if (activity == null) return;
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            PERMISSIONS_RECORD_AUDIO);
                } else {
                    startTalk();
                }
            }

            @Override
            public void onLayoutTouchDown() {
                if (talkLayout.getVisibility() == View.VISIBLE) {
                    talkLayout.setVisibility(View.GONE);
                    talkLayout.onViewDismiss();
                    buttonLayout.afterHideTalkView();
                }
            }
        });
    }

    /**
     * 设置 弹窗事件分发的响应接口
     * @param onFloatTalkEvent
     */
    public void setFloatTalkViewListener(FloatTalkView.OnFloatTalkEvent onFloatTalkEvent){
        talkLayout.setOnFloatTalkEvent(onFloatTalkEvent);
    }

    public void startTalk(){
        talkLayout.setVisibility(View.VISIBLE);
        talkLayout.onViewShow();
        buttonLayout.afterShowTalkView();
    }

    public void onPause(){
        talkLayout.setVisibility(View.GONE);
        talkLayout.onViewDismiss();
        buttonLayout.afterHideTalkView();
    }
}
