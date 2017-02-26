//package com.scheme.chc.lockscreen.activities;
//
//import android.content.Context;
//import android.content.ContextWrapper;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//
//import com.scheme.chc.lockscreen.utils.Utilities;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.util.ArrayList;
//
//public class UploadPassIconsActivity {
//
//    private final Context context;
//    private final Utilities utilities;
//    private final String[] imagenames = new String[5];
//    private Bitmap bitmapToSaveIn;
//
//    public UploadPassIconsActivity(Context context) {
//        this.context = context;
//        utilities = Utilities.getInstance(context);
//        saveToInternalStorage(convertToBitmap(utilities.getUploadedPassIcons()), imagenames);
//    }
//
//    private ArrayList<Bitmap> convertToBitmap(String[] id) {
//        ArrayList<Bitmap> bitmap = new ArrayList<>();
//        for (String bitmapid : id)
//            bitmap.add(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(), Integer.parseInt(bitmapid)), utilities.getIconWidth(), utilities.getIconHeight(), false));
//        return bitmap;
//    }
//
//    private boolean saveToInternalStorage(ArrayList<Bitmap> bitmapImage, String[] imagenames){
//        for (int i = 0; i < bitmapImage.size(); i++) {
////            Bitmap bitmap = bitmapImage.get(i);
//            ContextWrapper cw = new ContextWrapper(context);
//            // path to /data/data/yourapp/app_data/imageDir
//            File directory = cw.getDir("chc", Context.MODE_PRIVATE);
//            // Create imageDir
//            File mypath = new File(directory, imagenames[i]);
//
//            FileOutputStream imagefile = null;
//            try {
//                imagefile = new FileOutputStream(mypath);
//                // Use the compress method on the BitMap object to write image to the OutputStream
//                bitmapToSaveIn.compress(Bitmap.CompressFormat.PNG, 100, imagefile);
//            } catch (Exception e) {
//                e.printStackTrace();
//            } finally {
//                try {
//                    assert imagefile != null;
//                    imagefile.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            return true;
//        }
//        return false;
//    }
//}
