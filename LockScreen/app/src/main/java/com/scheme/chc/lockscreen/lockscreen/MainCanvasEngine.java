package com.scheme.chc.lockscreen.lockscreen;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import com.scheme.chc.lockscreen.utils.GrahamScan;
import com.scheme.chc.lockscreen.utils.Utilities;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

class MainCanvasEngine extends Thread implements View.OnTouchListener{

    @SuppressLint("StaticFieldLeak")
    private static MainCanvasEngine mainCanvasEngine;
    private final SurfaceView surfaceView;
    private Utilities utilities;
    private SurfaceHolder surfaceHolder;
    private Boolean lock = false;
    private Context context;
    private int CanvasWidth;
    private int CanvasHeight;
    private int TotalIconsToDisplay;
    private int TotalRounds;
    private int TotalNumberOfPassIcons;
    private int IconWidth;
    private int IconHeight;
    private ArrayList<Bitmap> SelectedPassIcons = new ArrayList<>() ;
    private ArrayList<Bitmap> iconsArrayFromAssets = new ArrayList<>();
    private int TotalIconsInAssets = 0;
    private int numberOfIconsVertically;
    private int numberOfIconsHorizontally;
    private int TotalNumberOfRegularIcons;
    private ArrayList<Integer> passIconsXlist = new ArrayList<>();
    private ArrayList<Integer> passIconsYlist = new ArrayList<>();
    private ArrayList<Bitmap> bitmapArrayList = new ArrayList<>();
    private List iconscopylist = new ArrayList<>();
    private int Boundry = 0;
    private Path path = new Path();
    private int draw = 1;
    private Paint paint = new Paint();
    private int WrongTrys = 1;
    private Vibrator vibrator;


    MainCanvasEngine(Context context, Boolean lock, SurfaceHolder surfaceHolder, SurfaceView surfaceView) {
        this.context = context;
        this.lock = lock;
        this.surfaceHolder = surfaceHolder;
        this.surfaceView = surfaceView;
        vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    public void run() {
        super.run();
        while (lock) {
            //checks if the lockCanvas() method will be success,and if not, will check this statement again
            if (!surfaceHolder.getSurface().isValid()) {continue;}
            draw();
            lock = false;
        }
    }

    private void draw() {
        /** Start editing pixels in this surface.*/
        Canvas canvas = surfaceHolder.lockCanvas();
        canvas.drawColor(Color.DKGRAY, PorterDuff.Mode.CLEAR);
        surfaceView.setOnTouchListener(this);

        InitializeUtilities(canvas);
        getAllIconsFromAssets();
        drawIcons(paint, canvas);
        createConvexHull();
        drawpolygon(paint, canvas);

        // End of painting to canvas. system will paint with this canvas,to the surface.
        surfaceHolder.unlockCanvasAndPost(canvas);
    }

    private void InitializeUtilities(Canvas canvas) {
        utilities = Utilities.getInstance(context);
        CanvasWidth = canvas.getWidth();
        CanvasHeight = canvas.getHeight();
        TotalIconsToDisplay = utilities.getTotalIconsToDisplay();
        TotalNumberOfPassIcons = utilities.getTotalNumberOfPassIcons();
        TotalNumberOfRegularIcons = utilities.getTotalNumberOfRegularIcons();
        numberOfIconsVertically = utilities.getNumberOfIconsVertically();
        numberOfIconsHorizontally = utilities.getNumberOfIconsHorizontally();
        IconWidth = CanvasWidth/numberOfIconsHorizontally;
        IconHeight = CanvasWidth/numberOfIconsHorizontally;
        TotalRounds = utilities.getTotalRounds();

        if(utilities.getChoosenPassIcons().length > 1) {
            SelectedPassIcons.clear();
            iconscopylist.clear();
            Collections.addAll(iconscopylist,utilities.getChoosenPassIcons());
            for (int i = 0; i < TotalNumberOfPassIcons; i++)
                SelectedPassIcons.add(getFilenameFromAssets((String) iconscopylist.get(i)));
        }
        else if(utilities.getUploadedPassIcons().length > 1){
            SelectedPassIcons.clear();
            iconscopylist.clear();
            Collections.addAll(iconscopylist,utilities.getUploadedPassIcons());
            for (int i = 0; i <TotalNumberOfPassIcons; i++)
                SelectedPassIcons.add(getFilenameFromAssets((String) iconscopylist.get(i)));
        }
    }

    private void drawpolygon(Paint paint, Canvas canvas) {
        path.reset();
        path.moveTo(passIconsXlist.get(0), passIconsYlist.get(0));
        for (int i = 1; i < passIconsYlist.size(); i++) {
            path.lineTo(passIconsXlist.get(i), passIconsYlist.get(i));
        }
        path.lineTo(passIconsXlist.get(0), passIconsYlist.get(0));

        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        canvas.drawPath(path,paint);
    }

    private void createConvexHull() {
        List<Point> points = GrahamScan.getConvexHull(passIconsXlist, passIconsYlist);

        points.remove(points.size() - 1);

        for (int i = 0; i < passIconsYlist.size(); i++) {
            boolean flag = false;
            for (int j = 0; j < points.size(); j++) {
                int X = points.get(j).x;
                int Y = points.get(j).y;
                if (passIconsXlist.get(i) == X && passIconsYlist.get(i) == Y) {
                    flag = true;
                }
            }
            if (!flag) {
                points.add(new Point(passIconsXlist.get(i), passIconsYlist.get(i)));
            }
        }

        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            passIconsXlist.set(i, point.x);
            passIconsYlist.set(i, point.y);
        }
        System.out.println("X1: " +passIconsXlist + " Y1:"+ passIconsYlist);
    }

    @SuppressLint("NewApi")
    private void drawIcons(Paint paint, Canvas canvas) {
        bitmapArrayList.clear();
        int iconTopSpace = 5;
        int iconLeftSpace = 5;
        passIconsXlist.clear();
        passIconsYlist.clear();
        utilities.index.clear();
        path.reset();

        for(int i = 0; i < TotalNumberOfPassIcons ; i++) {
            bitmapArrayList.add(SelectedPassIcons.get(i));
        }

        for (int i = 0; i < TotalNumberOfRegularIcons ; i++) {
            int randomNumber = utilities.getRandomInt(TotalIconsInAssets);
            Bitmap drawingbitmap = (iconsArrayFromAssets.get(randomNumber));
            boolean same = false;
            for (Bitmap bitmap : bitmapArrayList) {
                same = bitmap.sameAs(drawingbitmap);
            }
            if(!same) {
                bitmapArrayList.add(drawingbitmap);
            }
            else
                System.out.println("Not adding");
        }

        Collections.shuffle(bitmapArrayList, new Random(System.nanoTime()));

        for (int i = 0; i < TotalIconsToDisplay ; i++) {
            canvas.drawBitmap(bitmapArrayList.get(i), iconLeftSpace, iconTopSpace, paint);
            iconLeftSpace += CanvasWidth/numberOfIconsHorizontally;
            if(iconLeftSpace >= CanvasWidth) {
                Boundry = iconLeftSpace;
                iconLeftSpace = 5;
                iconTopSpace +=  (CanvasHeight/numberOfIconsVertically) ;
            }

            if(isPassIcon(bitmapArrayList.get(i))){
//                System.out.println("space " +IconLeftSpace);
                if(iconLeftSpace <= 5) {
                    passIconsXlist.add(Boundry - (IconWidth/2));
                    passIconsYlist.add(iconTopSpace - (CanvasHeight/numberOfIconsVertically) + (IconHeight/2));
                }
                else {
                    passIconsXlist.add(iconLeftSpace - (IconWidth / 2));
                    passIconsYlist.add(iconTopSpace + (IconHeight / 2));
                }
            }
        }
//        System.out.println("X: " +passIconsXlist + " Y:"+ passIconsYlist);
    }

    @SuppressLint("NewApi")
    private boolean isPassIcon(Bitmap bitmap) {
        for (Bitmap SelectedPassIcon : SelectedPassIcons) {
            if (SelectedPassIcon.sameAs(bitmap)) {
                return true;
            }
        }
        return false;
    }

    private Bitmap bitmapFromAssets(InputStream inputStream) {
        return Bitmap.createScaledBitmap(BitmapFactory.decodeStream(new BufferedInputStream(inputStream)), IconWidth, IconHeight, false);
    }

    @SuppressLint("NewApi")
    private void getAllIconsFromAssets() {
        try {

            AssetManager assetManager = context.getAssets();
            String[] fileList = assetManager.list("icons");
            if (iconsArrayFromAssets == null) {
                iconsArrayFromAssets = new ArrayList<>();
            } else {
                iconsArrayFromAssets.clear();
            }

            System.out.println(utilities.getChoosenPassIcons().length + " " +utilities.getUploadedPassIcons().length );
            if(utilities.getChoosenPassIcons().length <= 1 && utilities.getUploadedPassIcons().length <= 1) {
                for (int i = 1; i <= TotalNumberOfPassIcons; i++) {
//                    System.out.println("icons/" + i +".png");
                    SelectedPassIcons.add(bitmapFromAssets(assetManager.open("icons/" + i + ".png")));
                }
            }

            for (int i = 1; i <= fileList.length;i++) {
                iconsArrayFromAssets.add(bitmapFromAssets(assetManager.open("icons/" +  i +".png")));
            }

            for (int i = 0; i<iconsArrayFromAssets.size();i++) {
                for (int j = 0; j < SelectedPassIcons.size(); j++) {
                    if (iconsArrayFromAssets.get(i).sameAs(SelectedPassIcons.get(j))) {
                        iconsArrayFromAssets.remove(i);
//                        System.out.println("removed from array");
                    }
                }
            }
            TotalIconsInAssets = iconsArrayFromAssets.size();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Bitmap getFilenameFromAssets(String filename){
        AssetManager assetManager = context.getAssets();
        Bitmap bitmap = null;
        try {
            bitmap = bitmapFromAssets(assetManager.open("icons/" +  filename));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private void UnlockPhone(){
        MainLockScreenWindow.btnUnlock.performClick();
    }

    @SuppressLint("NewApi")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && v == surfaceView) {
            RectF pBounds = new RectF();
            float x = event.getX();
            float y = event.getY();
            path.computeBounds(pBounds, true);
            if (pBounds.contains(x, y)) {
                if (draw == TotalRounds) {
                    MainLockScreenWindow.opensettings = true;
                    MainLockScreenWindow.removeview = true;
                    UnlockPhone();
                }
                else {
                    draw++;
                    draw();
                }
                return true;
            }
            else{
                if(WrongTrys == 3) {
                    vibrator.vibrate(2000);
                    Toast.makeText(context, "Incorrect: Try Again", Toast.LENGTH_LONG).show();
                    draw = 1;
                    WrongTrys = 1;
                    draw();
                }
                else {
                    WrongTrys++;
                    draw();
                }
            }
        }
        vibrator.cancel();
        return false;
    }
}
