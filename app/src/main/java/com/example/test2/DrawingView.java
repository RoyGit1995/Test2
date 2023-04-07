package com.example.test2;

import android.content.Context;
import android.graphics.*;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

public class DrawingView extends View {

    private Path drawingPath;
    private Paint drawingPaint;
    private Canvas drawingCanvas;
    private Bitmap drawingBitmap;



    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        drawingPath = new Path();
        drawingPaint = new Paint();
        drawingPaint.setColor(Color.BLACK);
        drawingPaint.setStrokeWidth(5f);
        drawingPaint.setStyle(Paint.Style.STROKE);
        drawingPaint.setStrokeJoin(Paint.Join.ROUND);
        drawingPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        drawingBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawingCanvas = new Canvas(drawingBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(drawingBitmap, 0, 0, null);
        canvas.drawPath(drawingPath, drawingPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                drawingPath.moveTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_MOVE:
                drawingPath.lineTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
                drawingCanvas.drawPath(drawingPath, drawingPaint);
                drawingPath.reset();
                break;
            default:
                return false;
        }

        invalidate();
        return true;
    }

    public void setDrawingColor(int color) {
        drawingPaint.setColor(color);
    }

    public void setDrawingWidth(float width) {
        drawingPaint.setStrokeWidth(width);
    }

    public void clearDrawing() {
        drawingBitmap.eraseColor(Color.WHITE);
        invalidate();
    }

    public Bitmap getDrawing() {
        return drawingBitmap;
    }

    public void setDrawing(Bitmap bitmap) {
        drawingBitmap = bitmap;
        drawingCanvas = new Canvas(drawingBitmap);
        invalidate();
    }
}
