package com.sage.sage_android.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.sage.sage_android.R;

/**
 * Created by cyberpirate on 4/20/16.
 */
public class BigSwitch extends View {

    public BigSwitch(Context context) {
        super(context);
        init();
    }

    public BigSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BigSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public BigSwitch(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    Bitmap background, foreground;
    boolean isOn = true;
    float movPerc = 1.0f;

    Paint paint = new Paint();
    int bgColor = 0;
    private void init() {
        background = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.switch_backpiece);
        foreground = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.switch_frontpiece);
        bgColor = getContext().getResources().getColor(R.color.switch_bg);
        toggle();
    }


    Rect bgSrc = new Rect();
    Rect bgDst = new Rect();
    Rect fgSrc = new Rect();
    Rect fgDst = new Rect();
    @Override
    public void onDraw(Canvas c) {

        double bgRatio = (double) background.getHeight() / (double) background.getWidth();

        int bgTargetWidth = c.getWidth();
        int bgTargetHeight = (int) (bgTargetWidth*bgRatio);

        bgSrc.set(0, 0, background.getWidth(), background.getHeight());
        bgDst.set(0, 0, bgTargetWidth, bgTargetHeight);

        double fgRatio = (double) foreground.getHeight() / (double) foreground.getWidth();

        int fgTargetWidth = c.getWidth();
        int fgTargetHeight = (int) (fgTargetWidth*fgRatio);

        int offset = (int) (movPerc*(bgTargetHeight-fgTargetHeight));

        fgSrc.set(0, 0, foreground.getWidth(), foreground.getHeight());
        if(isOn) fgDst.set(0, offset, fgTargetWidth, fgTargetHeight+offset);
        else fgDst.set(0, bgTargetHeight-fgTargetHeight-offset, fgTargetWidth, bgTargetHeight-offset);

        c.drawColor(bgColor);
        c.drawBitmap(background, bgSrc, bgDst, paint);
        c.drawBitmap(foreground, fgSrc, fgDst, paint);
    }

    public void toggle() {
        isOn = !isOn;
        animStart = System.currentTimeMillis();
        animRunner.run();
    }

    public boolean getIsOn() {
        return isOn;
    }

    public void setIsOn(boolean is) {
        isOn = is;
        movPerc = 0.0f;
        invalidate();
    }

    long animStart;
    Runnable animRunner = new Runnable() {

        @Override
        public void run() {

            long delta = System.currentTimeMillis() - animStart;

            movPerc = 1-(delta*0.005f);

            if(movPerc < 0)
                movPerc = 0;

            invalidate();

            if(movPerc != 0)
                post(this);
        }
    };
}
