package com.acj.assistanttouchview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * 当条音量线
 * Created by sharon on 2018/1/27.
 */

public class VoiceLineView extends View{

    private Paint paint;
    private int radius = 0;
    private int halfRadius = 0;
    private int height = 0;
    private int maxHeight = 0;
    private int unitHeight = 0;//条状的单元长度，以7dp作为单元长度
    private int startY = 0; //Y起始点
    private int changeHeight = 0; //动画时，每次变化的长度
    private int normalRight = 0;//正常右下角
    private int normalTop = 0;//动画正常左上top
    private int shortRight = 0;//动画短时右下角
    private int shortTop = 0;//动画短时左上top

    private boolean isShort = false;
    private int multiple = 0;

    public VoiceLineView(Context context) {
        super(context);
        initPaint(context);
    }

    public VoiceLineView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initPaint(context);
    }

    public VoiceLineView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint(context);
    }

    private void initPaint(Context context){
        paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.FILL);
        radius = height = DensityUtil.dip2px(context, 6);
        halfRadius = radius /2;

        changeHeight = DensityUtil.dip2px(context, 4);
        unitHeight = DensityUtil.dip2px(context, 7);

    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    public synchronized void setHeight(int multi) {
        this.multiple = multi;
        if (multi < 1) {
            this.height = maxHeight/2 + halfRadius + changeHeight/2;
            this.startY = maxHeight/2 - halfRadius - changeHeight/2;
            this.normalRight = this.height;
            this.normalTop = this.startY;
            this.shortRight = maxHeight/2 + halfRadius;
            this.shortTop = maxHeight/2 - halfRadius;
        } else {
            this.height = maxHeight/2 + multi * unitHeight/2 + changeHeight/2;
            this.startY = maxHeight/2 - multi * unitHeight/2 - changeHeight/2;
            this.normalRight = this.height;
            this.normalTop = this.startY;
            this.shortRight = maxHeight/2 + multi * unitHeight/2;
            this.shortTop = maxHeight/2 - multi * unitHeight/2;
        }
        invalidate();
    }

    public synchronized void hide() {
        this.height = maxHeight/2 + halfRadius;
        this.startY = maxHeight/2 - halfRadius;
        invalidate();
    }

    public void setAnimStartWith(boolean shortOrLong){
        isShort = shortOrLong;
    }


    public synchronized void noticeAnimOnce() {
        isShort = !isShort;
        if (isShort) {
            this.height = this.shortRight;
            this.startY = this.shortTop;
        } else {
            this.height = this.normalRight;
            this.startY = this.normalTop;
        }
        invalidate();
    }


    public void setColor(int color) {
        paint.setColor(color);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        RectF oval = new RectF(0 , startY,
                 radius, height);
        canvas.drawRoundRect(oval, halfRadius, halfRadius, paint);
    }



}
