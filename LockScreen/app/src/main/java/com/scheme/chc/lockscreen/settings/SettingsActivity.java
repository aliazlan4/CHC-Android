package com.scheme.chc.lockscreen.settings;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.scheme.chc.lockscreen.R;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @SuppressLint({"NewApi","CommitPrefEdits"})
    public static class GeneralPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference("rounds"));
            bindPreferenceSummaryToValue(findPreference("total_icons"));
//            pref_enablechc = (SwitchPreference) findPreference("enablechc");
//            pref_totalicons =  findPreference("total_icons");
//            pref_totalrounds =  findPreference("rounds");
//            pref_enablechc.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//                @Override
//                public boolean onPreferenceChange(Preference preference, Object newValue) {
//                    System.out.println(newValue);
//                    pref_enablechc.setEnabled((Boolean) newValue);
//                    pref_enablechc.setChecked((Boolean) newValue);
//                    pref_enablechc.setSelectable(true);
////                    if(pref_enablechc.isEnabled()) {
////                        Intent intent = new Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD);
////                        startActivity(intent);
////                    }
//                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
//                    SharedPreferences.Editor edit = preferences.edit();
//                    edit.putBoolean("enablechc", (Boolean) newValue);
//                    edit.apply();
//                    return false;
//                }
//            });

        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows PassIcons preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @SuppressLint({"NewApi","CommitPrefEdits"})
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class PassIconsPreferenceFragment extends PreferenceFragment {
        int PICK_IMAGE_MULTIPLE = 1;
        private ArrayList<String> imagesPathList = new ArrayList<>();
        private ArrayList<Bitmap> imagebitmaps = new ArrayList<>();
        private Bitmap yourbitmap;
        private int noofpassicons;
        private Intent intent = new Intent();
        private Preference pref_uploadpassicon;
        private Preference pref_choosepassicon;
        private GridView imagegrid;
        private GridViewAdapter gridAdapter;
        private String[] passiconarray = new String[5];
        private int itemclickcount = 0;
        private Preference pref_viewpassicon;
        private int totalpassicontoview;
        private AlertDialog dialog;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_pass_icons);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference("no_of_pass_icons"));

            pref_uploadpassicon = findPreference("custom_pass_icon");
            pref_choosepassicon = findPreference("choose_pass_icon");
            pref_viewpassicon = findPreference("view_pass_icons");

            pref_uploadpassicon.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @SuppressLint("InlinedApi")
                public boolean onPreferenceClick(Preference preference) {
                    imagesPathList.clear();
                    imagebitmaps.clear();
                    passiconarray = null;
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                    noofpassicons = Integer.parseInt(preferences.getString("no_of_pass_icons", ""));

                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    intent.setAction(Intent.ACTION_GET_CONTENT);        //                    intent.setAction(Intent.ACTION_PICK);
                    for (int i = 0; i < noofpassicons; i++)
                        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_MULTIPLE);
                    return true;
                }
            });

            pref_choosepassicon.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    imagesPathList.clear();
                    imagebitmaps.clear();
                    itemclickcount = 0;
                    InflateChooseIconGallery(getActivity());
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                    SharedPreferences.Editor edit = preferences.edit();
                    Set<String> mySet = new HashSet<>(Collections.singletonList(String.valueOf(imagebitmaps)));
                    edit.putStringSet("choose_pass_icon", mySet );
                    edit.putStringSet("view_pass_icons", mySet );
                    edit.apply();
                    return false;
                }
            });

            pref_viewpassicon.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    InflateViewingGallery(getActivity());
                    return false;
                }
            });

        }

        @SuppressLint({ "PrivateResource"})
        private void InflateViewingGallery(Activity activity) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("Your Gallery");

            RelativeLayout gridlayout;

            gridlayout = (RelativeLayout) View.inflate(new ContextThemeWrapper(activity, R.style.Theme_AppCompat_Dialog), R.layout.gridview, null);

            imagegrid = (GridView) gridlayout.findViewById(R.id.gridView);
            gridAdapter = new GridViewAdapter(activity, R.layout.gridicons, getViewingData());
            imagegrid.setAdapter(gridAdapter);
            imagegrid.setFastScrollEnabled(true);

            builder.setView(gridlayout);
            builder.setCancelable(true);
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            builder.create().show();
        }

        @SuppressLint("PrivateResource")
        private void InflateChooseIconGallery(Activity activity) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            noofpassicons = Integer.parseInt(preferences.getString("no_of_pass_icons", ""));
            passiconarray = new String[noofpassicons];
            builder.setTitle("Please Select " +noofpassicons+" icons");

            RelativeLayout gridlayout;

            gridlayout = (RelativeLayout) View.inflate(new ContextThemeWrapper(activity, R.style.Theme_AppCompat_Dialog), R.layout.gridview, null);

            imagegrid = (GridView) gridlayout.findViewById(R.id.gridView);
            gridAdapter = new GridViewAdapter(activity, R.layout.gridicons, getData());
            imagegrid.setAdapter(gridAdapter);
            imagegrid.setFastScrollEnabled(true);
            builder.setView(gridlayout);
            builder.setCancelable(false);
            builder.setPositiveButton("Select", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.setNegativeButton ("Cancel", null);
            dialog = builder.create();

            dialog.show();
            imagegrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    GridViewIcon item = (GridViewIcon) parent.getItemAtPosition(position);
                    passiconarray[itemclickcount] = item.getTitle();

                    itemclickcount ++;
                    if(itemclickcount == noofpassicons) {
                        System.out.println(itemclickcount +" " + (noofpassicons-1));
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        SharedPreferences.Editor edit = preferences.edit();
                        Set<String> mySet = new HashSet<>(Arrays.asList(passiconarray));
                        edit.putStringSet("choose_pass_icon", mySet );
                        edit.putStringSet("view_pass_icons", mySet );
                        edit.apply();
                    }
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setText("Select (" +itemclickcount+"/"+noofpassicons+")");
                }
            });

            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(itemclickcount != noofpassicons){
                        Toast.makeText(getActivity(),"Try Again: You selected " +itemclickcount+"/" +noofpassicons+ " icons", Toast.LENGTH_LONG).show();
                        itemclickcount = 0;
                    }
                    else dialog.dismiss();
                }
            });
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setText("Select (" +itemclickcount+"/"+noofpassicons+")");
        }

        private Bitmap bitmapFromAssets(InputStream inputStream) {
            return Bitmap.createScaledBitmap(BitmapFactory.decodeStream(new BufferedInputStream(inputStream)), 200, 200, false);
        }

        private ArrayList<GridViewIcon> getData() {
            final ArrayList<GridViewIcon> gridViewIcons = new ArrayList<>();
            AssetManager assetManager = getActivity().getAssets();
            String[] fileList;
            try {
                fileList = assetManager.list("icons");
                for (int i = 1; i < fileList.length; i++) {
                    try {
                        gridViewIcons.add(new GridViewIcon(bitmapFromAssets(assetManager.open("icons/" + i + ".png")),i+".png"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return gridViewIcons;
        }


        private ArrayList<GridViewIcon> getViewingData() {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            Set<String> viewingIcons = preferences.getStringSet("view_pass_icons", null);
            totalpassicontoview = Integer.parseInt(preferences.getString("no_of_pass_icons", "5"));
            final ArrayList<GridViewIcon> gridViewIcons = new ArrayList<>();
            ArrayList<String> arrayList = new ArrayList<>();
            AssetManager assetManager = getActivity().getAssets();


            if(viewingIcons == null || viewingIcons.size() <= 1)
                for (int i = 1; i <= totalpassicontoview; i++)
                    try {
                        gridViewIcons.add(new GridViewIcon(bitmapFromAssets(assetManager.open("icons/" + i + ".png")), i + ".png"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            else{
                String[] ViewPassIcons = viewingIcons.toArray(new String[]{});
                Collections.addAll(arrayList, ViewPassIcons);
                System.out.println(" " + viewingIcons + " " + arrayList);
                for (int i = 0; i < ViewPassIcons.length; i++)
                    gridViewIcons.add(new GridViewIcon(getFilenameFromAssets(arrayList.get(i)), i + ".png"));
            }
            return gridViewIcons;

        }

        private Bitmap getFilenameFromAssets(String filename){
            AssetManager assetManager = getActivity().getAssets();
            Bitmap bitmap = null;
            try {
                bitmap = bitmapFromAssets(assetManager.open("icons/" +  filename));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            System.out.println(data.getData());
            if (requestCode == PICK_IMAGE_MULTIPLE && resultCode == RESULT_OK && data.getData() != null) {
                String imagesPath = data.getDataString();
                imagesPathList.add(imagesPath);
                yourbitmap = BitmapFactory.decodeFile(imagesPath);
                imagebitmaps.add(yourbitmap);
                System.out.println(imagebitmaps);
                System.out.println(imagesPathList);
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }


    /**
     * This fragment shows Feedback preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class FeedbackPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_feedback);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference("name"));
            bindPreferenceSummaryToValue(findPreference("comment"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows Exit preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class ExitPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            System.exit(1);
        }

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        if (resultCode == RESULT_OK) {
            Uri selectedImage = imageReturnedIntent.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            assert cursor != null;
            cursor.moveToFirst();

//            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//            String filePath = cursor.getString(columnIndex);
            cursor.close();

        }
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);
            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };


    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName)
                || PassIconsPreferenceFragment.class.getName().equals(fragmentName)
                || FeedbackPreferenceFragment.class.getName().equals(fragmentName)
                || ExitPreferenceFragment.class.getName().equals(fragmentName);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
