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

        //normale afbeelding
//        canvas.drawCircle((float) (locationX / 491.3385827) * width, (float) (locationY / 226.7716535) * height, 3, locationPaint);

        //kleine afbeelding
        canvas.drawCircle((float) (locationX / 193.9653543) * width, (float) (locationY / 147.9307087) * height, 3, locationPaint);


//        canvas.rotate((float) -19.27, (float) 245.6692913, (float) 113.3858268);
////        // Zone 1
////        canvas.rotate((float) -18.19, (float) (100.119685 / 491.3385827) * width, (float) (203.111811 / 226.7716535) * height);
//        canvas.drawRect((float) (45.35433071 / 491.3385827) * width, (float) (171.3259843 / 226.7716535) * height, (float) (100.119685 / 491.3385827) * width, (float) (203.111811 / 226.7716535) * height, locationPaint);
//        canvas.save();
//        canvas.restore();
//
////        // Zone 2
////        canvas.rotate((float) -19.27, (float) (36.01889764 / 491.3385827) * width, (float) (138.3307087 / 226.7716535) * height);
//        canvas.drawRect((float) (36.01889764 / 491.3385827) * width, (float) (138.3307087 / 226.7716535) * height, (float) (73.20944882 / 491.3385827) * width, (float) (172.119685 / 226.7716535) * height, locationPaint);
//        canvas.save();
//        canvas.restore();
//
//        // Zone 3
////        canvas.rotate((float) -19.27, (float) (20.37165354  / 491.3385827) * width, (float) (95.81102362 / 226.7716535) * height);
//        canvas.drawRect((float) (20.37165354 / 491.3385827) * width, (float) (95.81102362 / 226.7716535) * height, (float) (54.38740157 / 491.3385827) * width, (float) (136.1385827 / 226.7716535) * height, locationPaint);
//        canvas.save();
//        canvas.restore();
////
//////        // Zone 4
////        canvas.rotate((float) -19.27, (float) (101.6314961 / 491.3385827) * width, (float) (112.2897638 / 226.7716535) * height);
//        canvas.drawRect((float) (53.21574803 / 491.3385827) * width, (float) (81.37322835 / 226.7716535) * height, (float) (101.6314961 / 491.3385827) * width, (float) (112.2897638 / 226.7716535) * height, locationPaint);
//        canvas.save();
//        canvas.restore();
////
//////        // Zone 5
////        canvas.rotate((float) -19.27, (float) (163.3133858 / 491.3385827) * width, (float) (92.14488189 / 226.7716535) * height);
//        canvas.drawRect((float) (102.6141732 / 491.3385827) * width, (float) (61.68188976 / 226.7716535) * height, (float) (163.3133858 / 491.3385827) * width, (float) (92.14488189 / 226.7716535) * height, locationPaint);
//        canvas.save();
//        canvas.restore();
////
//////        // Zone 6
////        canvas.rotate((float) -19.27, (float) (185.4992126 / 491.3385827) * width, (float) (77.51811024 / 226.7716535) * height);
//        canvas.drawRect((float) (163.2 / 491.3385827) * width, (float) (47.01732283 / 226.7716535) * height, (float) (185.4992126 / 491.3385827) * width, (float) (77.51811024 / 226.7716535) * height, locationPaint);
//        canvas.save();


    }


    public void drawLocation(float x, float y) {
        locationX = x;
        locationY = y;
        invalidate();
    }

}
