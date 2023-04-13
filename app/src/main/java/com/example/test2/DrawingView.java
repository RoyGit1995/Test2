package com.example.test2;

import android.content.Context;
import android.graphics.*;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class DrawingView extends View {

    private int fileNumber = 0;
    private String fileName;
    private File file;

    private Path drawingPath;
    private Paint drawingPaint;
    private Canvas drawingCanvas;
    private Bitmap drawingBitmap;

    private Canvas playCanvas;
    private Bitmap playBitmap;

    private List<Bitmap> mBitmapList = new ArrayList<>();
    private int saveBitmapIndex = 0;
    private int mCurrentBitmapIndex = 0;

    private File[] images;

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

    public void saveToFile(String fileNameAudio) {

        Log.d("fileName from audio    " , fileNameAudio.toString());

        File parentFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyDrawings");
        if (!parentFolder.exists()) {
            parentFolder.mkdirs();
        }

        int dotIndex = fileNameAudio.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileNameAudio.length() - 1) {
            fileNameAudio = fileNameAudio.substring(0, dotIndex);
        }


        String fileName = fileNameAudio + ".png";

        Log.d("fileNameAudio edited from audio    " , fileNameAudio.toString());

        Log.d("fileName for save with png    " , fileName.toString());
        File file = new File(parentFolder, fileName);

        Log.d("file folderrrrrrrr    " , file.getAbsolutePath());

        try {
            FileOutputStream fos = new FileOutputStream(file);
            drawingBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        ++saveBitmapIndex;

    }

    public void loadFromFile(String fileNameAudio) {
        try {

            Log.d("fileNameAudio before edit from for loading    " , fileNameAudio.toString());

            int dotIndex = fileNameAudio.lastIndexOf('.');
            if (dotIndex > 0 && dotIndex < fileNameAudio.length() - 1) {
                fileNameAudio = fileNameAudio.substring(0, dotIndex);
            }

            fileNameAudio = fileNameAudio + ".png";
            Log.d("fileNameAudio edited from for loading    " , fileNameAudio.toString());



           // images = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).listFiles((dir, name) -> name.endsWith(".png"));


            File parentFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyDrawings");
            File fileLoad = new File(parentFolder, fileNameAudio);
            FileInputStream fis = new FileInputStream(fileLoad);
            Bitmap bitmap = BitmapFactory.decodeStream(fis);
            fis.close();

            clear();

            drawingBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            drawingCanvas = new Canvas(drawingBitmap);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clear() {
        if (drawingBitmap != null) {
            drawingBitmap.recycle();
            drawingBitmap = null;
        }
        drawingCanvas = null;
        drawingPath.reset();
        invalidate();
    }


}
