package com.acj.assistanttouchview;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Random;

/**
 * 声音动画View
 * View组成解析：
 * 1、该View 是由 19条 VoiceLineView 在 LinearLayout 中平均分布组合而成
 * 2、他们的颜色是从"#b2f95b" -> "#7dde22"渐变取值，分为20等分
 * 3、每条最大的长度取值分别为7dp的 volumeList = {2,3,4,5,7,6,3,8,10,8,4,6,5,3,2,5,2,3,5} 倍
 * 4、每条最小长度取值为6dp的圆形，即当倍数为0时，取值6dp作为每条长度
 * 5、当每条发生动画时，长度变化为，当前长度加上4dp，即(7dp*n + 4dp)
 * 动画：
 * 1、以50ms为单位时间长度，
 * 2、声音出现时， 以50ms的1倍作为出现动画的速度，即每条 VoiceLineView 出现的间隔时间
 * 3、声音消失时，以50ms的1倍作为消失动画的速度，即每条 VoiceLineView 消失的间隔时间
 * 4、每个 VoiceLineView 长短变化的速度为100ms/次
 * 声音：
 * 1、以100db作为最大声音，分为10等分
 * 2、当获取到声音时，取当前 volume 的级数 multi = volume/100
 * 3、对应到当前每一条 VoiceLineView 的长度为 multi * volumeList[i] 取整
 * 流程：
 * 1、出现动画：当声音出现时，调用 showAnimation()，每隔50ms逐条出现，并对之前已出现的 VoiceLineView 每隔100ms做一次长短变化
 * 2、消失动画：当声音消失时，调用 hideAnimation()，每隔50ms逐条消失，并对还在当 VoiceLineView 每隔100ms做一次长短变化
 * 3、当声音为0时，如果当前还有动画持续 isActive = true，做一次消失动画
 * 4、当声音不为0时，之前为0，如果当前已无动画 isActive = false, 做出现动画一次
 * 5、当声音不为0时，之前不为0，只修改 VoiceLineView的长度
 *
 * Created by sharon on 2018/1/27.
 */

public class VoiceView extends RelativeLayout {

    private static int speed = 50;//以50ms作为，单元速度，也是目前显示，消失的速度
    private static int multipleOf = 2;// 长短变化动画的速度是单元速度的几倍
    private int preVolume = 0;
    private ArrayList<VoiceLineView> voiceList;
    private Context context;
    private int[] volumeList = {2,3,4,5,7,6,3,8,10,8,4,6,5,3,2,5,2,3,5};
    private boolean isActive = false;
    private Handler handler;
    private Random random;
    private int volume;

    public VoiceView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public VoiceView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public VoiceView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    /**
     * 初始化19个音量条
     * 包括颜色，大小，初始为圆点
     */
    private void init(){
        random = new Random();
        setGravity(CENTER_VERTICAL);
        handler = new Handler(Looper.getMainLooper()){

            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0) { //当前 VoiceLineView 到最后一条做一次动画
                    int current = (int)msg.obj;
                    for (int i = current; i< voiceList.size(); i++) {
                        voiceList.get(i).noticeAnimOnce();
                    }
                } else if (msg.what == 1) { //隐藏
                    voiceList.get((int)msg.obj).hide();
                } else if (msg.what == 2) { //显示
                    VoiceInfo current = (VoiceInfo) msg.obj;
                    voiceList.get(current.index).setAnimStartWith(random.nextBoolean());
                    voiceList.get(current.index).setHeight(current.multiOfHeight);
                } else if (msg.what == 3) { //0 到当前 VoiceLineView 做一次动画
                    int current = (int)msg.obj;
                    for (int i=0; i< current; i++) {
                        voiceList.get(i).noticeAnimOnce();
                    }
                }

            }
        };

        voiceList = new ArrayList<>();
        ArgbEvaluator argbEvaluator = new ArgbEvaluator();
        int startColor = Color.parseColor("#b2f95b");
        int endColor = Color.parseColor("#7dde22");
        for (int i = 0; i < 19; i++) {
            //一开始设置更大的高度
            int maxHeight = volumeList[i] * DensityUtil.dip2px(context, 7) + DensityUtil.dip2px(context, 8);

            VoiceLineView view = new VoiceLineView(context);
            view.setColor((int) argbEvaluator.evaluate(0.05f * i, startColor, endColor));
            view.setMaxHeight(maxHeight);
            view.hide();
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
                view.setId(View.generateViewId());
            } else {
                view.setId(i);
            }

            LayoutParams lp = new LayoutParams(DensityUtil.dip2px(context,6), maxHeight);
            lp.addRule(CENTER_VERTICAL);
            if (i > 0) {
                lp.addRule(RIGHT_OF, voiceList.get(i - 1).getId());
                lp.leftMargin = DensityUtil.dip2px(context, 7);
            }
            addView(view, lp);
            voiceList.add(view);
        }
    }


    /**
     * 音量大小设置
     * @param volume
     */
    public void setVolume(int volume) {
        float multi = (float)volume / 100.00f;
        if (volume == 0) { //结束
            if (isActive) {
                stopAnimation();
                hideAnimation();
            }
        } else if (volume > 0 && preVolume == 0) {// 开始
            if (!isActive) {
                showAnimation(multi);
            } else {
                setHeightToVolume(multi);
            }
        } else if (volume > 0 && preVolume != 0) {//声音大小变化
            setHeightToVolume(multi);
        }
        preVolume = volume;
    }

    public void setHeightToVolume(float multi){
        for (int i = 0; i< voiceList.size(); i++) {
            VoiceInfo info = new VoiceInfo();
            info.index = i;
            info.multiOfHeight = (int)(volumeList[i] * multi);
            Message msg = new Message();
            msg.what = 2;
            msg.obj = info;
            handler.sendMessage(msg);
        }
    }

    public void showAnimation(final float multiples){
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i< voiceList.size(); i++) {
                    VoiceInfo info = new VoiceInfo();
                    info.index = i;
                    info.multiOfHeight = (int)(volumeList[i] * multiples);
                    Message msg = new Message();
                    msg.what = 2;
                    msg.obj = info;
                    handler.sendMessage(msg);
                    if (i%multipleOf == 0){
                        Message msg1 = new Message();
                        msg1.what = 3;
                        msg1.obj = i;
                        handler.sendMessage(msg1);
                    }
                    try {
                        Thread.sleep(speed);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                startAnimation();
            }
        }).start();

    }

    public void hideAnimation(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i< voiceList.size(); i++) {
                    Message msg = new Message();
                    msg.what = 1;
                    msg.obj = i;
                    handler.sendMessage(msg);
                    if (i%multipleOf == 0){
                        Message msg1 = new Message();
                        msg1.what = 0;
                        msg1.obj = i+1; //当前消失了，但下一条还有动画，从下一条开始
                        handler.sendMessage(msg1);
                    }
                    try {
                        Thread.sleep(speed);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void startAnimation() {
        isActive = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isActive) {
                    Message msg = new Message();
                    msg.what = 0;
                    msg.obj = 0;
                    handler.sendMessage(msg);
                    try {
                        Thread.sleep(multipleOf * speed);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();
    }

    private void stopAnimation() {
        isActive = false;
    }

    private class VoiceInfo{
        public int index;
        public int multiOfHeight;
    }
}
