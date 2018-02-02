package com.acj.assistanttouchview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 对话dialog：
 * 管理两件事：1、获取声音传给VoiceView完成动画；2、画圆角背景
 * Created by sharon on 2018/1/30.
 */

public class FloatTalkView extends RelativeLayout {


    private VoiceRecordHelper voiceRecordHelper; //声音获取工具
    private VoiceView voiceView;
    private ImageView btnQuestion;
    private OnFloatTalkEvent onFloatTalkEvent;

    private Context context;
    private Drawable drawable;
    private int radius;

    public FloatTalkView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public FloatTalkView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public FloatTalkView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    public void init() {
        inflate(this.context, R.layout.layout_float_speech_talk, this);
        btnQuestion = (ImageView) findViewById(R.id.btn_question);
        btnQuestion.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onFloatTalkEvent != null) {
                    onFloatTalkEvent.clickOnQuestion();
                }
            }
        });
        voiceView = (VoiceView) findViewById(R.id.voice_view);
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
                voiceView.setVolume(value);
            }
        });

        /**
         * dialog圆形背景数据
         */
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = context.getResources().getDrawable(R.drawable.bg_talk_dialog);
        } else {
            drawable = context.getDrawable(R.drawable.bg_talk_dialog);
        }
        radius = DensityUtil.dip2px(context, 6);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        int height = getMeasuredHeight();
        int width = getMeasuredWidth();
        setBackground(makeRoundCorner(context,drawable,radius, width, height));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    public void setOnFloatTalkEvent(OnFloatTalkEvent onFloatTalkEvent) {
        this.onFloatTalkEvent = onFloatTalkEvent;
    }

    public void onViewShow(){
        voiceRecordHelper.startRecord();
    }

    /**
     * 页面消失时需处理
     */
    public void onViewDismiss(){
        voiceView.hideAnimation();
        voiceRecordHelper.stopRecord();
    }
    /**
     * 图片去圆角
     *
     * @param context  context
     * @param drawable drawable
     * @param pixels   圆角像素
     * @param width    width
     * @param height   height
     * @return Drawable
     */
    public static Drawable makeRoundCorner(Context context, Drawable drawable, int pixels, int width, int height) {
        if (width <= 0 || height <= 0 || drawable == null) {
            return null;
        }
        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, width, height);
        RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(0xff424242);
        canvas.drawRoundRect(rectF, pixels, pixels, paint);

        setMode(drawable, PorterDuff.Mode.SRC_IN);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        setMode(drawable, null);

        return new BitmapDrawable(context.getResources(), output);
    }

    /**
     * 设置Mode
     *
     * @param drawable drawable
     * @param mode     PorterDuff.Mode
     */
    public static void setMode(Drawable drawable, PorterDuff.Mode mode) {
        Xfermode xferMode = null;
        if (mode != null) {
            xferMode = new PorterDuffXfermode(mode);
        }
        try {
            Method method = Drawable.class.getDeclaredMethod("setXfermode", Xfermode.class);
            method.invoke(drawable, xferMode);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    public interface OnFloatTalkEvent{
        void clickOnQuestion();
    }

}