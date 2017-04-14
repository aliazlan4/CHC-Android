package com.scheme.chc.lockscreen.separated;

import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;

import com.scheme.chc.lockscreen.LockScreenActivity;
import com.scheme.chc.lockscreen.R;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by Ender on 09-Apr-17 for CHC-Android-master
 */
@SuppressWarnings({"deprecation", "FieldCanBeLocal"})
public class CameraLayout implements SurfaceHolder.Callback, Camera.PictureCallback, View.OnClickListener {

    public boolean cameraActivated;
    public SurfaceView camView;
    private ImageButton switchCamera;
    private ImageButton captureImage;
    private Camera camera;
    private SurfaceHolder cameraSurfaceHolder;

    private int camId;
    private String imagePath;
    private boolean camCondition;

    private LockScreenActivity parentActivity;

    public CameraLayout(LockScreenActivity parentActivity) {
        this.parentActivity = parentActivity;
        initialize();
        configure();
    }

    private void initialize() {
        switchCamera = (ImageButton) parentActivity.findViewById(R.id.switchcamera);
        camView = (SurfaceView) parentActivity.findViewById(R.id.camerapreview);
        captureImage = (ImageButton) parentActivity.findViewById(R.id.capture);
        cameraSurfaceHolder = camView.getHolder();

        imagePath = "";
        camCondition = false;
        cameraActivated = true;
        camId = Camera.CameraInfo.CAMERA_FACING_BACK;
    }

    private void configure() {
        parentActivity.getWindow().setFormat(PixelFormat.UNKNOWN);
        captureImage.setOnClickListener(this);
        switchCamera.setOnClickListener(this);
        cameraSurfaceHolder.addCallback(this);
        cameraSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
    }

    @Override
    public void onClick(View v) {
        if (v == captureImage) {
            camera.takePicture(null, null, null, this);
        } else if (v == switchCamera) {
            if (camCondition) {
                camera.stopPreview();
            }
            camera.release();
            if (camId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                camId = Camera.CameraInfo.CAMERA_FACING_FRONT;
            } else {
                camId = Camera.CameraInfo.CAMERA_FACING_BACK;
            }
            camera = Camera.open(camId);
            try {
                camera.setPreviewDisplay(cameraSurfaceHolder);
                camera.setDisplayOrientation(90);
                Camera.Parameters parameters = camera.getParameters();
                List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
                Camera.Size optimalSize = getOptimalPreviewSize(sizes,
                        parentActivity.getResources().getDisplayMetrics().widthPixels,
                        parentActivity.getResources().getDisplayMetrics().heightPixels);
                parameters.setPreviewSize(optimalSize.width, optimalSize.height);
                camera.setParameters(parameters);

            } catch (IOException e) {
                e.printStackTrace();
            }
            camera.startPreview();
        }
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.05;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        // Find size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - h) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - h);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - h) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - h);
                }
            }
        }
        return optimalSize;
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        FileOutputStream fileOutputStream;
        System.out.println(Environment.getExternalStorageDirectory());
        try {
            imagePath = "/sdcard/" + System.currentTimeMillis() + ".png";
            fileOutputStream = new FileOutputStream(imagePath);
            fileOutputStream.write(data);
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        camera = Camera.open();
        camera.setDisplayOrientation(90);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Stopping
        if (camCondition) {
            camera.stopPreview(); // Stop preview using stopPreview() method
            camCondition = false; // Setting condition to false means stop
        }
        // Condition to check whether your device have camera or not
        if (camera != null) {
            try {
                Camera.Parameters parameters = camera.getParameters();
                List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
                Camera.Size optimalSize = getOptimalPreviewSize(sizes,
                        parentActivity.getResources().getDisplayMetrics().widthPixels,
                        parentActivity.getResources().getDisplayMetrics().heightPixels);
                parameters.setPreviewSize(optimalSize.width, optimalSize.height);
                camera.setPreviewDisplay(cameraSurfaceHolder); // setting preview of camera
                camera.setParameters(parameters);
                camera.startPreview();  // starting  preview
                camCondition = true; // setting  to true which means having camera
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.stopPreview();
        camera.release();
        camera = null;
        camCondition = false;
        cameraActivated = false;
    }
}
