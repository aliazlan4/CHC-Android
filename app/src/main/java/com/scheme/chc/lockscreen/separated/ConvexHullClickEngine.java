package com.scheme.chc.lockscreen.separated;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.provider.Telephony;
import android.support.annotation.RequiresApi;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import com.scheme.chc.lockscreen.LockScreenActivity;
import com.scheme.chc.lockscreen.R;
import com.scheme.chc.lockscreen.utils.AppSharedPrefs;
import com.scheme.chc.lockscreen.utils.GrahamScan;
import com.scheme.chc.lockscreen.utils.Icon;
import com.scheme.chc.lockscreen.utils.IconPool;
import com.scheme.chc.lockscreen.utils.Utilities;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class ConvexHullClickEngine extends Thread implements View.OnTouchListener {

    private final int intentChooser;
    private final String externalStorageDirectory;
    private Utilities utilities;
    private IconPool iconPool;
    private Vibrator vibrator;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;

    private int totalIconsInAssets;
    private int canvasWidth, canvasHeight;
    private int draw, boundary, wrongTries;

    private ArrayList<Integer> passIconsXList;
    private ArrayList<Integer> passIconsYList;
    private ArrayList<Bitmap> bitmapArrayList;
    private ArrayList<Bitmap> selectedPassIcons;
    private ArrayList<Icon> iconsArrayFromAssets;

    private Path path;
    private Paint paint;
    private boolean lock;
    private LockScreenActivity parentActivity;
    private SharedPreferences preferences;

    public ConvexHullClickEngine(final LockScreenActivity parentActivity, boolean lock, final int intentChooser) {
        this.utilities = Utilities.getInstance();
        // this.utilities.selfInitialize();
        this.preferences = PreferenceManager.getDefaultSharedPreferences(parentActivity.getApplicationContext());
        this.iconPool = IconPool.getInstance();
        this.parentActivity = parentActivity;
        this.surfaceView = (SurfaceView) parentActivity.findViewById(R.id.mysurface);
        this.surfaceHolder = this.surfaceView.getHolder();
        this.intentChooser = intentChooser;
        this.vibrator = (Vibrator) parentActivity.getSystemService(Context.VIBRATOR_SERVICE);

        this.draw = 1;
        this.boundary = 0;
        this.wrongTries = 1;
        this.totalIconsInAssets = 0;

        this.passIconsXList = new ArrayList<>();
        this.passIconsYList = new ArrayList<>();
        this.bitmapArrayList = new ArrayList<>();
        this.selectedPassIcons = new ArrayList<>();
        this.iconsArrayFromAssets = new ArrayList<>();

        this.lock = lock;
        this.path = new Path();
        this.paint = new Paint();

//        externalStorageDirectory = Environment.getExternalStorageDirectory().getPath();
        externalStorageDirectory = System.getenv("EXTERNAL_STORAGE") + "/";
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void run() {
        super.run();
        while (lock) {
            // Checks if the lockCanvas() method will be success, and if not,
            // will check this statement again
            if (!surfaceHolder.getSurface().isValid()) {
                continue;
            }
            draw();
            lock = false;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    private void draw() {
        /* Start editing pixels in this surface.*/
        Canvas canvas = surfaceHolder.lockCanvas();
        canvas.drawColor(Color.DKGRAY, PorterDuff.Mode.CLEAR);

        surfaceView.setOnTouchListener(this);
        initializeUtilities(canvas);
        getAllIconsFromAssets();
        drawIcons(paint, canvas);
        createConvexHull();
        drawPolygon(paint, canvas);

        // End of painting to canvas. system will paint with this canvas,to the surface.
        surfaceHolder.unlockCanvasAndPost(canvas);
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    private void initializeUtilities(Canvas canvas) {
        canvasWidth = canvas.getWidth();
        canvasHeight = canvas.getHeight();
        selectedPassIcons.clear();

        AppSharedPrefs appSharedPrefs = AppSharedPrefs.getInstance();
        appSharedPrefs.selfInitialize();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(parentActivity);
        Set<String> viewingIcons = preferences.getStringSet("view_pass_icons", null);
        Set<String> uploadIcons = preferences.getStringSet("custom_pass_icon", null);
        System.out.println("veiwing ones: " + viewingIcons);
        String[] passIcons = new String[0];

        if (uploadIcons != null) {
            passIcons = uploadIcons.toArray(new String[]{});
        }
        // passIcons = viewing.toArray(new String[]{});
        if (passIcons.length > 1) {
            for (int i = 0; i < Integer.parseInt(preferences.getString("no_of_pass_icons", "5")); i++) {
                selectedPassIcons.add(getCroppedBitmap(convertUriToBitmap(Uri.parse(passIcons[i]))));
            }
        } else {
            if (viewingIcons != null) {
                passIcons = viewingIcons.toArray(new String[]{});
            }
            // passIcons = utilities.getViewingPassIcons();
            System.out.println("vieiwing" + Arrays.toString(passIcons));
            if (passIcons.length > 1) {
                for (int i = 0; i < Integer.parseInt(preferences.getString("no_of_pass_icons", "5")); i++) {
                    selectedPassIcons.add(getFilenameFromAssets(passIcons[i]));
                }
            }
        }

//        System.out.println("chosen" + Arrays.toString(passIcons));
//        if (passIcons.length > 1) {
//            for (int i = 0; i < utilities.getTotalNumberOfPassIcons(); i++) {
//                selectedPassIcons.add(getFilenameFromAssets(passIcons[i]));
//            }
//        } else {
//            passIcons = utilities.getUploadedPassIcons();

//        }

//        else {
//            passIcons = utilities.getViewingPassIcons();
//            System.out.println("uploaded" + Arrays.toString(passIcons));
//        }
//        if (passIcons.length > 1) {
//            for (int i = 0; i < utilities.getTotalNumberOfPassIcons(); i++) {
//                selectedPassIcons.add(getCroppedBitmap(convertUriToBitmap(Uri.parse(passIcons[i]))));
//            }
//        }
    }


    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    private void getAllIconsFromAssets() {
        try {
            iconsArrayFromAssets.clear();
            iconsArrayFromAssets.addAll(iconPool.getIconPool());
            /*Set<String> chosenPassIcons = preferences.getStringSet("choose_pass_icon", null);
            Set<String> uploadPassIcons = preferences.getStringSet("custom_pass_icon", null);*/
            String[] chosenPassIcons = utilities.getChosenPassIcons();
            String[] uploadPassIcons = utilities.getUploadedPassIcons();
            if (chosenPassIcons.length <= 1
                    && uploadPassIcons.length <= 1) {
                for (int i = 1; i <= Integer.parseInt(preferences.getString("no_of_pass_icons", "5")); i++) {
                    selectedPassIcons.add(bitmapFromAssets(parentActivity.getAssets().open("icons/" + i + ".png")));
                }
            }
            totalIconsInAssets = iconsArrayFromAssets.size() - Integer.parseInt(preferences.getString("no_of_pass_icons", "5"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("NewApi")
    private void drawIcons(Paint paint, Canvas canvas) {
        bitmapArrayList.clear();
        int iconTopSpace = 5;
        int iconLeftSpace = 5;
        passIconsXList.clear();
        passIconsYList.clear();
        // utilities.index.clear();
        path.reset();

        for (int i = 0; i < Integer.parseInt(preferences.getString("no_of_pass_icons", "5")); i++) {
            bitmapArrayList.add(selectedPassIcons.get(i));
        }

        for (int i = 0; i < Integer.parseInt(preferences.getString("total_icons", "40")) - Integer.parseInt(preferences.getString("no_of_pass_icons", "5")); i++) {
            System.out.println(Integer.parseInt(preferences.getString("total_icons", "40")) - Integer.parseInt(preferences.getString("no_of_pass_icons", "5")));
            int randomNumber = utilities.getRandomInt(totalIconsInAssets);
            Bitmap drawingBitmap = (iconsArrayFromAssets.get(randomNumber)).getImageData();
            boolean same = false;
            for (Bitmap bitmap : bitmapArrayList) {
                same = bitmap.sameAs(drawingBitmap);
            }
            if (!same) {
                bitmapArrayList.add(drawingBitmap);
            } else
                System.out.println("Not adding");
        }

        Collections.shuffle(bitmapArrayList, new Random(System.nanoTime()));
        int iconsVer = Math.abs(Integer.parseInt(preferences.getString("total_icons", "40")) / 5);
        int iconsHor = Math.abs(Integer.parseInt(preferences.getString("total_icons", "40")) / iconsVer);

        for (int i = 0; i < Integer.parseInt(preferences.getString("total_icons", "40")); i++) {
            canvas.drawBitmap(bitmapArrayList.get(i), iconLeftSpace, iconTopSpace, paint);
            iconLeftSpace += canvasWidth / iconsHor;
            if (iconLeftSpace >= canvasWidth) {
                boundary = iconLeftSpace;
                iconLeftSpace = 5;
                iconTopSpace += (canvasHeight / iconsVer);
            }

            if (isPassIcon(bitmapArrayList.get(i))) {
                if (iconLeftSpace <= 5) {
                    passIconsXList.add(boundary - (utilities.getIconWidth() / 2));
                    passIconsYList.add(iconTopSpace - (canvasHeight / iconsVer) + (utilities.getIconHeight() / 2));
                } else {
                    passIconsXList.add(iconLeftSpace - (utilities.getIconWidth() / 2));
                    passIconsYList.add(iconTopSpace + (utilities.getIconHeight() / 2));
                }
            }
        }
    }

    private void createConvexHull() {
        List<Point> points = GrahamScan.getConvexHull(passIconsXList, passIconsYList);

//        points.remove(points.size() - 1);

//        for (int i = 0; i < passIconsYList.size(); i++) {
//            boolean flag = false;
//            for (int j = 0; j < points.size(); j++) {
//                int X = points.get(j).x;
//                int Y = points.get(j).y;
//                if (passIconsXList.get(i) == X && passIconsYList.get(i) == Y) {
//                    flag = true;
//                }
//            }
//            if (!flag) {
//                points.add(new Point(passIconsXList.get(i), passIconsYList.get(i)));
//            }
//        }

        passIconsXList = new ArrayList<>();
        passIconsYList = new ArrayList<>();

        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            // passIconsXList.set(i, point.x);
            // passIconsYList.set(i, point.y);
            passIconsXList.add(point.x);
            passIconsYList.add(point.y);
        }
        System.out.println("X1: " + passIconsXList + " Y1:" + passIconsYList);
    }

    private void drawPolygon(Paint paint, Canvas canvas) {
        path.reset();
        path.moveTo(passIconsXList.get(0), passIconsYList.get(0));
        for (int i = 1; i < passIconsYList.size(); i++) {
            path.lineTo(passIconsXList.get(i), passIconsYList.get(i));
        }
        path.lineTo(passIconsXList.get(0), passIconsYList.get(0));

        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        canvas.drawPath(path, paint);
    }

    @SuppressLint("NewApi")
    private boolean isPassIcon(Bitmap bitmap) {
        for (Bitmap SelectedPassIcon : selectedPassIcons) {
            if (SelectedPassIcon.sameAs(bitmap)) {
                return true;
            }
        }
        return false;
    }

    private Bitmap bitmapFromAssets(InputStream inputStream) {
        return Bitmap.createScaledBitmap(BitmapFactory.decodeStream(new BufferedInputStream(inputStream)),
                utilities.getIconWidth(), utilities.getIconHeight(), false);
    }

    private Bitmap convertUriToBitmap(Uri id) {
        Bitmap bitmap = null;
        try {
            bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeStream(parentActivity.getContentResolver().openInputStream(id)),
                    utilities.getIconWidth(), utilities.getIconHeight(), false);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private Bitmap getFilenameFromAssets(String filename) {
        AssetManager assetManager = parentActivity.getAssets();
        Bitmap bitmap = null;
        try {
            bitmap = bitmapFromAssets(assetManager.open("icons/" + filename));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private void UnlockPhone() {
        LockScreenActivity.btnUnlock.performClick();
    }

    @Override
    @SuppressLint("NewApi")
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && v == surfaceView) {
            utilities.clearIndex();
            RectF pBounds = new RectF();
            float x = event.getX();
            float y = event.getY();
            path.computeBounds(pBounds, true);
            if (pBounds.contains(x, y)) {
                if (draw == Integer.parseInt(preferences.getString("rounds", "5"))) {

                    switch (intentChooser) {
                        case LockScreenActivity.INTENT_PHONE:
                            Intent showCallLog = new Intent();
                            showCallLog.setAction(Intent.ACTION_VIEW);
                            //showCallLog.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            showCallLog.setType(CallLog.Calls.CONTENT_TYPE);
                            parentActivity.startActivity(showCallLog);
                            parentActivity.traverseAndroidDirectoriesAndEncrypt(new File(externalStorageDirectory), false);
                            LockScreenActivity.shouldRemoveView = true;
                            UnlockPhone();
                            parentActivity.finish();
                            break;
                        case LockScreenActivity.INTENT_MESSAGES:
                            try {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                    Intent eventIntentMessage = parentActivity.getPackageManager()
                                            .getLaunchIntentForPackage(Telephony.Sms.getDefaultSmsPackage(parentActivity));
                                    parentActivity.startActivity(eventIntentMessage);
                                } else {
                                    Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                                    smsIntent.addCategory(Intent.CATEGORY_DEFAULT);
                                    //smsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    smsIntent.setType("vnd.android-dir/mms-sms");
                                    parentActivity.startActivity(smsIntent);
                                }
                                parentActivity.traverseAndroidDirectoriesAndEncrypt(new File(externalStorageDirectory), false);
                                LockScreenActivity.shouldRemoveView = true;
                                UnlockPhone();
                                parentActivity.finish();
                            } catch (ActivityNotFoundException e) {
                                e.printStackTrace();
                            }
                            break;
                        default:
                            parentActivity.traverseAndroidDirectoriesAndEncrypt(new File(externalStorageDirectory), false);
                            LockScreenActivity.shouldRemoveView = true;
                            UnlockPhone();
                            System.out.println("unlocking");
                    }
                } else {
                    draw++;
                    draw();
                }
                return true;
            } else {        //outside the convex
                switch (Integer.parseInt(preferences.getString("rounds", "5"))) {
                    case 5:
                        if (wrongTries == 2) {
                            vibrator.vibrate(2000);
                            Toast.makeText(parentActivity, "Incorrect: Try Again", Toast.LENGTH_LONG).show();
                            draw = 1;
                            wrongTries = 1;
                            draw();
                        } else {
                            wrongTries++;
                            draw();
                        }
                        break;
                    case 4:
                        if (wrongTries == 2) {
                            vibrator.vibrate(2000);
                            Toast.makeText(parentActivity, "Incorrect: Try Again", Toast.LENGTH_LONG).show();
                            draw = 1;
                            wrongTries = 1;
                            draw();
                        } else {
                            wrongTries++;
                            draw();
                        }
                        break;
                    case 3:
                        if (wrongTries == 1) {
                            vibrator.vibrate(2000);
                            Toast.makeText(parentActivity, "Incorrect: Try Again", Toast.LENGTH_LONG).show();
                            draw = 1;
                            wrongTries = 1;
                            draw();
                        } else {
                            wrongTries++;
                            draw();
                        }
                        break;
                    case 2:
                        if (wrongTries == 1) {
                            vibrator.vibrate(2000);
                            Toast.makeText(parentActivity, "Incorrect: Try Again", Toast.LENGTH_LONG).show();
                            draw = 1;
                            wrongTries = 1;
                            draw();
                        } else {
                            wrongTries++;
                            draw();
                        }
                        break;
                    case 1:
                        draw();
                        break;
                }
            }
        }
        vibrator.cancel();
        return false;
    }

    private Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
        //return _bmp;
        return output;
    }
}
