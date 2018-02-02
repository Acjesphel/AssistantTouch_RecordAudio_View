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
 *
 * 按钮长按（位置不改变的长按）：onButtonTouch.touchDown()
 * 按钮放开，以及被告知拖拽释放：onButtonTouch.touchUp()
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
    private OnButtonTouch onButtonTouch;
    private boolean isMoving = false;

    public void setOnButtonTouch(OnButtonTouch onButtonTouch) {
        this.onButtonTouch = onButtonTouch;
    }

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
        isMoving = false;

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
        } else if (currentState == ButtonState.Press) {
            startAnim();
        }
        invalidate();
    }

    public void startAnim(){
        circleAlpha = maxAlpha;
        circleRadius = minRadius;
        invalidate();
    }

    private float lastX, lastY;
    private long lastDownTime;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setState(ButtonState.Press);
                lastX = event.getX();
                lastY = event.getY();
                lastDownTime = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_MOVE:
                if (isLongPressed(lastX, lastY, event.getX(), event.getY(),
                        lastDownTime, System.currentTimeMillis(), 200)){
                    if (onButtonTouch != null){
                        onButtonTouch.touchDown();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (onButtonTouch != null) {
                    onButtonTouch.touchUp();
                }
                setState(ButtonState.Normal);
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * * 判断是否有长按动作发生 * @param lastX 按下时X坐标 * @param lastY 按下时Y坐标 *
     *
     * @param thisX
     *            移动时X坐标 *
     * @param thisY
     *            移动时Y坐标 *
     * @param lastDownTime
     *            按下时间 *
     * @param thisEventTime
     *            移动时间 *
     * @param longPressTime
     *            判断长按时间的阀值
     */
    static boolean isLongPressed(float lastX, float lastY, float thisX,
                                 float thisY, long lastDownTime, long thisEventTime,
                                 long longPressTime) {
        float offsetX = Math.abs(thisX - lastX);
        float offsetY = Math.abs(thisY - lastY);
        long intervalTime = thisEventTime - lastDownTime;
        if (offsetX <= 10 && offsetY <= 10 && intervalTime >= longPressTime) {
            return true;
        }
        return false;
    }


    public void setMoving(boolean moving) {
        isMoving = moving;
        if (!isMoving){
            setState(ButtonState.Normal);
            if (onButtonTouch != null) {
                onButtonTouch.touchUp();
            }
        }
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

    public interface OnButtonTouch{
        void touchDown();
        void touchUp();
    }




}
