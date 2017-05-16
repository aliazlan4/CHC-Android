package com.scheme.chc.lockscreen.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.util.Base64;

import com.scheme.chc.lockscreen.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

@SuppressWarnings("ALL")
public class Cryptographer {

    private static Cryptographer instance;
    private static String firstinstalltime = null;
    private static byte[] key;
    private int BUFFER_SIZE = 0;
    private Context context;
    private String initVector;

    private Cryptographer(Context context, byte[] key) {
        this.context = context;
//        this.key = key;
        this.initVector = context.getString(R.string.initVector);
        BUFFER_SIZE = new StatFs(Environment.getDataDirectory().getPath()).getBlockSize();
        System.out.println(BUFFER_SIZE);
    }

    public static Cryptographer getInstance(Context context) {
        if (instance == null) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            Set<String> viewingIcons = preferences.getStringSet("view_pass_icons", null);
            try {
                firstinstalltime = String.valueOf(context.getPackageManager().getPackageInfo(context.getPackageName(), 0).firstInstallTime);
                System.out.println("first" + firstinstalltime);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            if (viewingIcons == null) {
                key = get256bitHash(
                        "12345",
                        firstinstalltime
                );
                System.out.println("salt is : 12345 ");
            } else {
                key = get256bitHash(
                        String.valueOf(viewingIcons),
                        firstinstalltime
                );
                System.out.println("salt is : " + String.valueOf(viewingIcons));
            }

//            key = "O2OaVZr6tGHKAacVO2OaVZr6tGHKAacV";
            System.out.println("key is: " + key.toString());

            System.out.println("key is: " + key.length);
            instance = new Cryptographer(context, key);
        }
        return instance;
    }

    public static byte[] get256bitHash(String key, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt.getBytes("UTF-8"));
            byte[] hash = digest.digest(key.getBytes("UTF-8"));
            return hash;
//            StringBuffer hexString = new StringBuffer();
//
//            for (int i = 0; i < hash.length; i++) {
//                String hex = Integer.toHexString(0xff & hash[i]);
//                if(hex.length() == 1) hexString.append('0');
//                hexString.append(hex);
//            }
//
//            String hex = hexString.toString();
//            StringBuilder output = new StringBuilder();
//            for (int i = 0; i < hex.length(); i+=2) {
//                String str = hex.substring(i, i+2);
//                output.append((char)Integer.parseInt(str, 16));
//            }
//            return output.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public String encryptText(String plainText) {
        return Base64.encodeToString(encryptBytes(plainText.getBytes()), Base64.NO_PADDING);
    }

    public String decryptText(String cipherText) {
        return new String(decryptBytes(Base64.decode(cipherText, Base64.NO_PADDING)));
    }

    public byte[] encryptBytes(byte[] plainBytes) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(plainBytes);
            return encrypted;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public byte[] decryptBytes(byte[] cipherBytes) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            byte[] original = cipher.doFinal(cipherBytes);
            return original;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void encryptFile(File file) {
        String externalStorageDirectory = System.getenv("EXTERNAL_STORAGE") + "/";
        String extension = file.getName().substring(file.getName().lastIndexOf(".") + 1, file.getName().length());
        File temp = new File(externalStorageDirectory + "encrypted" /*"/storage/emulated/0/Download/encrypted" */ + extension);
        try {
            temp.createNewFile();
            byte[] b = new byte[BUFFER_SIZE];
            FileInputStream in = new FileInputStream(file);
            FileOutputStream out = new FileOutputStream(temp);
            int c;
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            System.out.println(key.toString());
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            while ((c = in.read(b)) != -1) {
//                System.out.println("4");
                byte[] output = cipher.update(b, 0, c);
                if (output != null) {
                    out.write(output);
                }
            }
//            System.out.println(temp.getUsableSpace());
            byte[] output = cipher.doFinal();
            if (output != null)
                out.write(output);

            if (in != null)
                in.close();
            if (out != null)
                out.close();
            copy(temp, file);
            temp.delete();
//            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void decryptFile(File file) {
        String externalStorageDirectory = System.getenv("EXTERNAL_STORAGE") + "/";
        String extension = file.getName().substring(file.getName().lastIndexOf(".") + 1, file.getName().length());
        File temp = new File(externalStorageDirectory + "decrypted" /*"/storage/emulated/0/Download/encrypted" */ + extension);
        try {
            temp.createNewFile();

            byte[] b = new byte[BUFFER_SIZE];
            FileInputStream in = new FileInputStream(file);
            FileOutputStream out = new FileOutputStream(temp);

            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            int c;
            while ((c = in.read(b)) != -1) {
                byte[] output = cipher.update(b, 0, c);
                if (output != null) {
                    out.write(output);
                }
            }

            byte[] output = cipher.doFinal();
            if (output != null)
                out.write(output);

            if (in != null)
                in.close();
            if (out != null)
                out.close();
            copy(temp, file);
            temp.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int encryptDirectory(File directory) {
        File[] kids = directory.listFiles();
        int count = 0;

        if (kids != null) {
            for (File kid : kids) {
                if (kid.isFile()) {
                    encryptFile(kid);
                    count++;
                } else {
                    count += encryptDirectory(kid);
                }
            }
        }
        return count;
    }

    public int decryptDirectory(File directory) {
        File[] kids = directory.listFiles();
        int count = 0;

        if (kids != null) {
            for (File kid : kids) {
                if (kid.isFile()) {
                    decryptFile(kid);
                    count++;
                } else {
                    count += decryptDirectory(kid);
                }
            }
        }
        return count;
    }

    public void copy(File src, File dst) {
        FileInputStream inStream = null;
        try {
            inStream = new FileInputStream(src);
            FileOutputStream outStream = new FileOutputStream(dst);
            FileChannel inChannel = inStream.getChannel();
            FileChannel outChannel = outStream.getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
            inStream.close();
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
