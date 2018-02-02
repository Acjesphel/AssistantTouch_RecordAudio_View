package com.acj.assistanttouchview;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.HashMap;

/**
 * 管理两件事：1、动画；2、按钮状态变化
 *
 * button状态：及其转换规则
 * 1、正常：变"正常"那刻，5s后变"透明"
 * 2、按下："不可使用"状态下不可"按下"
 * 3、透明：button 被 TouchDown 就变"正常"
 * 4、不可使用：出现dialog 不可使用，同时不能变"透明"，不能变"按下"
 * Created by sharon on 2018/1/29.
 */

public class SpeechButton extends View {

    public enum ButtonState{
        Normal,
        Press,
        Translucent,
        Unable
    }
    private static HashMap<ButtonState, Integer> resMap = new HashMap<>();
    static {
        resMap.put(ButtonState.Normal, R.drawable.btn_ai_normal);
        resMap.put(ButtonState.Press, R.drawable.btn_ai_pressed);
        resMap.put(ButtonState.Translucent, R.drawable.btn_ai_translucent);
        resMap.put(ButtonState.Unable, R.drawable.btn_ai_unable);
    }


    private Context context;

    private Paint paint;
    private Bitmap bitmap;
    /**
     * 半径变化
     */
    private int circleRadius;
    private int minRadius, maxRadius;
    private int speedRadius = 0;
    /**
     * 透明度变化
     */
    private int circleAlpha;
    private int maxAlpha;
    private int speedAlpha = 0;

    private ButtonState currentState = ButtonState.Normal;

    private Handler handler;

    public SpeechButton(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public SpeechButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public SpeechButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        handler = new Handler();

        setBackgroundColor(Color.TRANSPARENT);
        setState(ButtonState.Normal);
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);

        //半径
        minRadius = DensityUtil.dip2px(context, 37);
        maxRadius = DensityUtil.dip2px(context, 42);
        circleRadius = minRadius;
        speedRadius = (maxRadius - minRadius) / 10;
        if (speedRadius < 1) speedRadius = 1;
        //透明度
        maxAlpha = (int)(255 * 0.2);
        circleAlpha = 0;
        speedAlpha = maxAlpha / 10;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //button 动画
        paint.setAlpha(circleAlpha);
        canvas.drawCircle(getWidth()/2, getHeight()/2, circleRadius, paint);

        if (bitmap != null) {
            paint.setAlpha(255);
            canvas.drawBitmap(bitmap, getWidth()/2 - minRadius, getHeight()/2 - minRadius, paint);
        }

        if (circleAlpha > 0) {//以circleAlpha不等于0，作为是否draw动画的标志
            circleRadius = circleRadius + speedRadius;
            circleAlpha = circleAlpha - speedAlpha;
            if (circleAlpha < 0) circleAlpha = 0;
            invalidate();
        }
    }

    public void setState(ButtonState state) {
        currentState = state;
        this.bitmap = BitmapFactory.decodeResource(getResources(), resMap.get(state));
        if (currentState == ButtonState.Normal) {
            afterReleaseButton();
        } else if (currentState == ButtonState.Unable) {
            useButton();
        }
        invalidate();
    }

    public void startAnim(){
        circleAlpha = maxAlpha;
        circleRadius = minRadius;
        invalidate();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            if (currentState != ButtonState.Unable) {
                setState(ButtonState.Press);
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (currentState != ButtonState.Unable) {
                setState(ButtonState.Normal);
            }
        }
        return super.onTouchEvent(event);
    }

    /**
     * 不操作按钮之后5秒，需设置按钮 Translucent 状态
     */
    public void afterReleaseButton() {
        handler.removeCallbacks(translucentRunnable);
        handler.postDelayed(translucentRunnable, 5000);
    }

    public void useButton(){
        handler.removeCallbacks(translucentRunnable);
    }

    private Runnable translucentRunnable = new Runnable() {
        @Override
        public void run() {
            setState(SpeechButton.ButtonState.Translucent);
        }
    };
}
