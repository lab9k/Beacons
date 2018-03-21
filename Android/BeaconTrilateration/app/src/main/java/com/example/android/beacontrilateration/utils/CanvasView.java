package com.example.android.beacontrilateration.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by lottejespers.
 */

public class CanvasView extends View {

    private Paint locationPaint;
    private float locationX;
    private float locationY;

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

        canvas.drawCircle((float) (locationX / 1106.668346) * width, (float) (locationY / 651.9987402) * height, 3, locationPaint);
    }


    public void drawLocation(float x, float y) {
        locationX = x;
        locationY = y;
        invalidate();
    }

}
