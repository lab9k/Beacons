package com.example.android.beaconzone.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by lottejespers.
 */

public class CanvasView extends View {

    private Paint locationPaint;
    private float locationX;
    private float locationY;
    private double top;
    private double bottom;
    private double left;
    private double right;
    private double middenX;
    private double middenY;
    private double graden;
    private boolean zone = false;


    public CanvasView(Context context) {
        super(context, null);
        init();
    }

    public CanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CanvasView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        locationPaint = new Paint();
        locationPaint.setColor(Color.BLUE);
        locationPaint.setStrokeWidth(10);
        locationPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        canvas.drawCircle((float) (locationX / 239.6220752) * width, (float) (locationY / 226.77168) * height, 3, locationPaint);

        // ZONES

        if (left != 0.0) {
            canvas.drawRect((float) ((left / 239.6220752) * width), (float) ((top / 226.77168) * height), (float) ((right / 239.6220752) * width), (float) ((bottom / 226.77168) * height), locationPaint);
            canvas.save();
            canvas.restore();
            zone = false;
        }

    }

    public void drawLocation(float x, float y) {
        locationX = x;
        locationY = y;
        invalidate();
    }

    public void drawZone(double left, double top, double right, double bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.zone = true;
        invalidate();
    }

}

