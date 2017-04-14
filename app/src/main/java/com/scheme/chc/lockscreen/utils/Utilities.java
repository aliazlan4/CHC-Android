package com.scheme.chc.lockscreen.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

@SuppressLint("StaticFieldLeak")
@SuppressWarnings({"WeakerAccess", "unused"})
public class Utilities {

    private static Utilities instance;
    private final int totalIconsToDisplay;
    private final int totalNumberOfPassIcons;
    private final int numberOfIconsVertically;
    private final int numberOfIconsHorizontally;
    private final int totalNumberOfRegularIcons;
    private final int totalRounds, iconWidth, iconHeight;
    public ArrayList<Integer> index;
    private Context context;
    private String[] viewingPassIcons;
    private String[] chosenPassIcons;
    private String[] uploadedPassIcons;
    private SimpleDateFormat simpleDateFormat;

    private Utilities(Context context) {
        this.context = context;
        this.index = new ArrayList<>();

        AppSharedPrefs appSharedPrefs = AppSharedPrefs.getInstance();
        this.totalRounds = Integer.parseInt(appSharedPrefs.getRounds());
        this.totalIconsToDisplay = Integer.parseInt(appSharedPrefs.getTotalIcons());
        this.totalNumberOfPassIcons = Integer.parseInt(appSharedPrefs.getNumPassIcons());
        this.totalNumberOfRegularIcons = Integer.parseInt(appSharedPrefs.getTotalIcons()) - totalNumberOfPassIcons;

        this.numberOfIconsVertically = Math.abs(totalIconsToDisplay / 5);
        this.numberOfIconsHorizontally = Math.abs(totalIconsToDisplay / numberOfIconsVertically);

        this.viewingPassIcons = new String[totalNumberOfPassIcons];
        this.chosenPassIcons = new String[totalNumberOfPassIcons];
        this.uploadedPassIcons = new String[totalNumberOfPassIcons];

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            Set<String> viewingIcons = appSharedPrefs.getViewPassIcons();
            Set<String> selectionsChosenGallery = appSharedPrefs.getCustomPassIcons();
            Set<String> selectionsCustomUploaded = appSharedPrefs.getCustomPassIcons();
            this.viewingPassIcons = viewingIcons != null
                    ? viewingIcons.toArray(new String[]{})
                    : new String[0];
            this.chosenPassIcons = selectionsChosenGallery != null
                    ? selectionsChosenGallery.toArray(new String[]{})
                    : new String[0];
            this.uploadedPassIcons = selectionsCustomUploaded != null
                    ? selectionsCustomUploaded.toArray(new String[]{})
                    : new String[0];
        }
        // System.out.println("Chosen " + Arrays.toString(chosenPassIcons));
        // System.out.println("viewing " + Arrays.toString(viewingPassIcons));
        // System.out.println("Uploaded " + Arrays.toString(uploadedPassIcons));

        DisplayMetrics displayMetrics = getScreenMetrics();
        this.iconWidth = displayMetrics.widthPixels / numberOfIconsHorizontally;
        //noinspection SuspiciousNameCombination
        this.iconHeight = this.iconWidth;

        // Date format
        this.simpleDateFormat = new SimpleDateFormat("dd MMMM, yyyy, EEEE", Locale.getDefault());
    }

    public static void initialize(Context context) {
        instance = new Utilities(context);
    }

    public static Utilities getInstance() {
        if (instance == null) {
            throw new ExceptionInInitializerError("Context not initialized. User " +
                    "initialize(Context) or getInstance(Context) first.");
        }
        return instance;
    }

    public static Utilities getInstance(Context context) {
        instance = new Utilities(context);
        return instance;
    }

    public int getRandomInt(int limit) {
        int random = new Random().nextInt(limit - 5) + 5;

        if (index.contains(random)) {
            random = getRandomInt(limit);
        } else {
            index.add(random);
        }
        return random;
    }

    private DisplayMetrics getScreenMetrics() {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        return metrics;
    }

    private String getDateModifier(String date) {
        int val = Integer.parseInt(date);
        if (val >= 10 && val <= 20) {
            return "th";
        } else if ((val % 10) == 1) {
            return "st";
        } else if ((val % 10) == 2) {
            return "nd";
        } else if ((val % 10) == 3) {
            return "rd";
        } else {
            return "th";
        }
    }

    public int getTotalIconsToDisplay() {
        return totalIconsToDisplay;
    }

    public int getTotalNumberOfPassIcons() {
        return totalNumberOfPassIcons;
    }

    public int getNumberOfIconsVertically() {
        return numberOfIconsVertically;
    }

    public int getNumberOfIconsHorizontally() {
        return numberOfIconsHorizontally;
    }

    public int getTotalNumberOfRegularIcons() {
        return totalNumberOfRegularIcons;
    }

    public int getTotalRounds() {
        return totalRounds;
    }

    public int getIconWidth() {
        return iconWidth;
    }

    public int getIconHeight() {
        return iconHeight;
    }

    public String[] getViewingPassIcons() {
        return viewingPassIcons;
    }

    public String[] getChosenPassIcons() {
        return chosenPassIcons;
    }

    public String[] getUploadedPassIcons() {
        return uploadedPassIcons;
    }

    public Spanned getDate() {
        String date = simpleDateFormat.format(new Date());
        date = date.substring(0, 2)
                + "<sup><small>"
                + getDateModifier(date.substring(0, 2))
                + "</small></sup>"
                + date.substring(2, date.length());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(date, Html.FROM_HTML_MODE_LEGACY);
        } else {
            //noinspection deprecation
            return Html.fromHtml(date);
        }
    }
}
