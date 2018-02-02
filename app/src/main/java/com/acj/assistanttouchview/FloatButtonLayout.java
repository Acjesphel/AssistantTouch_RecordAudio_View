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

public class FloatButtonLayout extends RelativeLayout {

    private Context context;
    private ViewDragHelper viewDragHelper;

    private View followView;
    private SpeechButton speechButton;
    private Handler handler;

    private int buttonLeft, buttonTop, buttonHeight, halfButtonHeight;
    private int dialogTop, talkViewHeight;
    private int layoutHeight, margin;

    private boolean isTriggerButton = false;
    private boolean isFirstInit = true;

    private SpeechButton.OnButtonTouch onButtonTouch;

    public void setOnButtonTouchEvent(SpeechButton.OnButtonTouch onButtonTouchEvent){
        this.onButtonTouch = onButtonTouchEvent;
    }

    public void setFollowView(View followView) {
        this.followView = followView;
    }

    public FloatButtonLayout(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public FloatButtonLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public FloatButtonLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init(){
        isFirstInit = true;
        //要求在按钮正上方或正在方距离margin 15dp
        margin = DensityUtil.dip2px(context, 15);
        buttonHeight = DensityUtil.dip2px(context, 84) + margin;
        talkViewHeight = DensityUtil.dip2px(context, 160) + margin;
        halfButtonHeight = DensityUtil.dip2px(context, 42);

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 1) {
                    if (followView != null) {
                        //更改speech float的浮层位置
                        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) followView.getLayoutParams();
                        layoutParams.topMargin = dialogTop;
                        followView.setLayoutParams(layoutParams);
                    }
                }
            }
        };


        initDragHelper();
    }

    public void initDragHelper(){
        viewDragHelper = ViewDragHelper.create(this, new ViewDragHelper.Callback() {
            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                return left;
            }

            @Override
            public int clampViewPositionVertical(View child, int top, int dy) {
                isTriggerButton = true;
                speechButton.setMoving(true);
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
                isTriggerButton = false;
                speechButton.setMoving(false);
            }

            @Override
            public int getViewVerticalDragRange(View child) {
                return getHeight();
            }
        });

    }



    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        layoutHeight = getMeasuredHeight();

        speechButton = (SpeechButton)getChildAt(0);
        speechButton.setOnButtonTouch(onButtonTouch);

        if (isFirstInit) {
            //第一次按钮初始化位置在右下角
            buttonLeft = getWidth() - buttonHeight;
            buttonTop = layoutHeight - buttonHeight;
            setTalkViewPosition(buttonTop);
            isFirstInit = false;
        }
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
    }


    /**
     * 当对话框显示时，移动button计算对话框当位置
     * @param btnTop button Y的位置
     */
    public void setTalkViewPosition(final int btnTop) {
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
