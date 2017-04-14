package com.scheme.chc.lockscreen.utils;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.util.Base64;

import com.scheme.chc.lockscreen.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

@SuppressWarnings("ALL")
public class Cryptographer {

    private static Cryptographer instance;
    private int BUFFER_SIZE = 0;

    private Context context;
    private String key, initVector;

    private Cryptographer(Context context, String key) {
        this.context = context;
        this.key = key;
        this.initVector = context.getString(R.string.initVector);
        BUFFER_SIZE = new StatFs(Environment.getDataDirectory().getPath()).getBlockSize();
        System.out.println(BUFFER_SIZE);
    }

    public static Cryptographer getInstance(Context context) {
        if (instance == null) {
            instance = new Cryptographer(context, context.getString(R.string.key));
        }
        return instance;
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
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

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
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

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
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

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
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

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
