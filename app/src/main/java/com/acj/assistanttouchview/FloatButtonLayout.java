package com.acj.assistanttouchview;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * 按钮层：
 * 管理两件事：1、按钮拖拽；2、计算弹出浮层位置
 *
 * Created by sharon on 2018/1/26.
 */

public class FloatButtonLayout extends RelativeLayout implements View.OnClickListener{

    private Context context;
    private ViewDragHelper viewDragHelper;

    private SpeechButton speechButton;
    private Handler handler;

    private int buttonLeft, buttonTop, buttonHeight, halfButtonHeight;
    private int dialogTop, talkViewHeight;
    private int layoutHeight, margin;

    private OnButtonLayoutEvent onButtonLayoutEvent;

    private boolean isTriggerButton = false;
    private boolean isFirstInit = true;

    public void setOnButtonLayoutEvent(OnButtonLayoutEvent onButtonLayoutEvent) {
        this.onButtonLayoutEvent = onButtonLayoutEvent;
    }

    public interface OnButtonLayoutEvent {
        void onButtonMove(int dialogTop);
        void onButtonClick();
        void onLayoutTouchDown();//点击layout时候需关闭dialog
    }

    public FloatButtonLayout(Context context) {
        super(context);
        this.context = context;
        initDragHelper();
    }

    public FloatButtonLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initDragHelper();
    }

    public FloatButtonLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initDragHelper();
    }


    public void initDragHelper(){
        isFirstInit = true;
        //要求在按钮正上方或正在方距离margin 15dp
        margin = DensityUtil.dip2px(context, 15);
        buttonHeight = DensityUtil.dip2px(context, 84) + margin;
        talkViewHeight = DensityUtil.dip2px(context, 160) + margin;
        halfButtonHeight = DensityUtil.dip2px(context, 42);

        viewDragHelper = ViewDragHelper.create(this, new ViewDragHelper.Callback() {
            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                return left;
            }

            @Override
            public int clampViewPositionVertical(View child, int top, int dy) {
                isTriggerButton = true;
                setTalkViewPosition(top);
                return top;
            }

            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                return child == speechButton;
            }

            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
                //当button释放后应该所处的位置
                int parentCenterX = getWidth()/2;
                int childCenterX = (releasedChild.getLeft() + releasedChild.getRight()) / 2;
                if (releasedChild.getTop() < 0) {
                    buttonTop = 0;
                } else if (releasedChild.getBottom() > getBottom()) {
                    buttonTop = getBottom() - releasedChild.getHeight();
                } else {
                    buttonTop = releasedChild.getTop();
                }

                if (childCenterX < parentCenterX) { //靠左停靠
                    buttonLeft = 0;
                } else { //靠右停靠
                    buttonLeft = (getWidth() - releasedChild.getWidth());
                }

                viewDragHelper.settleCapturedViewAt(buttonLeft, buttonTop);
                speechButton.setState(SpeechButton.ButtonState.Normal);
                isTriggerButton = false;
            }

            @Override
            public int getViewVerticalDragRange(View child) {
                return getHeight();
            }
        });

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 1) {
                    if (onButtonLayoutEvent != null) {
                        onButtonLayoutEvent.onButtonMove((int)msg.obj);
                    }
                }
            }
        };

    }



    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        layoutHeight = getMeasuredHeight();

        speechButton = (SpeechButton) findViewById(R.id.speech_button);

        if (isFirstInit) {
            //第一次按钮初始化位置在右下角
            buttonLeft = getWidth() - buttonHeight;
            buttonTop = layoutHeight - buttonHeight;
            isFirstInit = false;
        }
        speechButton.setOnClickListener(this);
        speechButton.offsetLeftAndRight(buttonLeft);
        speechButton.offsetTopAndBottom(buttonTop);
    }

    @Override
    public void computeScroll() {
        viewDragHelper.continueSettling(true);
        invalidate();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return viewDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        viewDragHelper.processTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            if (!isTriggerButton && onButtonLayoutEvent != null) {
                onButtonLayoutEvent.onLayoutTouchDown();
            }
        }
        return true;
    }

    /**
     * 显示对话浮层后buttonLayout的变化
     */
    public void afterShowTalkView(){
        setBackgroundResource(R.drawable.bg_talk_show);
        setAlpha(0.6f);
        speechButton.setState(SpeechButton.ButtonState.Unable);
    }

    /**
     * 隐藏对话浮层后buttonLayout的变化
     */
    public void afterHideTalkView(){
        setBackgroundColor(Color.TRANSPARENT);
        speechButton.setState(SpeechButton.ButtonState.Normal);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.speech_button:
                speechButton.startAnim();
                setTalkViewPosition(buttonTop);
                if (onButtonLayoutEvent != null) {
                    onButtonLayoutEvent.onButtonClick();
                }
                break;
        }
    }




    /**
     * 当对话框显示时，移动button计算对话框当位置
     * @param btnTop button Y的位置
     */
    private void setTalkViewPosition(final int btnTop) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (btnTop + halfButtonHeight > layoutHeight/2) {
                    dialogTop = btnTop - talkViewHeight;
                } else {
                    dialogTop = btnTop + buttonHeight;
                }
                Message msg = new Message();
                msg.what = 1;
                msg.obj = dialogTop;
                handler.sendMessage(msg);
            }
        }).start();

    }
}
