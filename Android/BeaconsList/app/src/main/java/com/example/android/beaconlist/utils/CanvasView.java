package com.example.android.beaconlist.utils;

/**
 * Created by lottejespers.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class CanvasView extends View {

    private Paint paint;
    private Paint locationPaint;
    private float x;
    private float y;
    private float[][] grid;

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
        paint = new Paint();
        paint.setStrokeWidth(8);
        paint.setStyle(Paint.Style.STROKE);
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

        paint.setStyle(Paint.Style.STROKE);

        for (int i = 0; i < grid.length; i++) {
            x = grid[i][0];
            y = grid[i][1];
            paint.setColor((int) grid[i][2]);
            canvas.drawCircle((float) (x / 491.3385827) * width, (float) (y / 226.7716535) * height, 1, paint);
        }
    }

    public void drawBeacons(float[][] grid) {
        this.grid = grid;
        invalidate();
    }
}