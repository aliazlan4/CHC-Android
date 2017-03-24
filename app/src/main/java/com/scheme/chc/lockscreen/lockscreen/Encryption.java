package com.scheme.chc.lockscreen.lockscreen;

import android.util.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Encryption{
	/*******************************************************
	 * Author: Ali Azlan
	 * Email: i@aliazlan.com
	 * Website: https://www.aliazlan.com
	 * 
	 * Version: 1.0
	 * 
	 * Algorithm: AES
	 * Key Size: 16 bytes
	 * IV Size: 16 bytes
	 * Details: A cryptography library that encrypts and
	 * decrypts
	 * 		1: text in string format
	 * 		2: files
	 * 		3: directories (recursively)
	 *******************************************************/

	private String key, initVector;
	private final int BUFFER_SIZE = 8192; // Buffer size for file IO in bytes
	
	///////////////////////////////////////////////////////
	//////// Initialize 16 bytes key and IV ///////////////
	///////////////////////////////////////////////////////
	public Encryption(String key, String initVector){
		this.key = key;
		this.initVector = initVector;
	}
	
	///////////////////////////////////////////////////////
	///////// Encrypt plain text and output result ////////
	///////// without characters like = /  ////////////////
	///////////////////////////////////////////////////////
	private String encryptText(String plainText) {
//		URLEncoder urlEncoder =  Base64.getUrlEncoder().withoutPadding();
        return URLEncoder.encode(Arrays.toString(encryptBytes(plainText.getBytes())));
    }

	///////////////////////////////////////////////////////
	//////////////// Decrypt cipher text //////////////////
	///////////////////////////////////////////////////////
	private String decryptText(String cipherText) {
//    	Decoder urlDecoder = Base64.getUrlDecoder();
    	return new String(decryptBytes(URLDecoder.decode(cipherText)));
    }
    
	///////////////////////////////////////////////////////
	/////////////////// Encrypt Bytes  ////////////////////
	///////////////////////////////////////////////////////
	private byte[] encryptBytes(byte[] plainBytes){
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
    
	///////////////////////////////////////////////////////
	/////////////////// Decrypt Bytes  ////////////////////
	///////////////////////////////////////////////////////
	private byte[] decryptBytes(String cipherString) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

			byte[] cipherBytes = cipherString.getBytes();
            byte[] original = cipher.doFinal(cipherBytes);

            return original;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }
    
	///////////////////////////////////////////////////////
	////////// Encrypt file along with its name ////////////
    ///////////// It will delete old file /////////////////
	///////////////////////////////////////////////////////
	private void encryptFile(File file){
    	File temp = new File(file.getParentFile().getAbsolutePath() + "/" + encryptText(file.getName()));
    	try {
			temp.createNewFile();
			
			byte[] b = new byte[BUFFER_SIZE];
			FileInputStream in = new FileInputStream(file);
			FileOutputStream out = new FileOutputStream(temp);
			
			IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
			
			int c;
			while ((c = in.read(b)) != -1){
				byte[] output = cipher.update(b, 0, c);
				if (output != null){
					out.write(output);
				}
			}
			
			byte[] output = cipher.doFinal();
			if (output != null)
				out.write(output);

			if(in != null)
				in.close();
			if(out != null)
				out.close();
			
			file.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
	///////////////////////////////////////////////////////
	////////// Decrypt file along with its name ///////////
	///////////// It will delete old file /////////////////
	///////////////////////////////////////////////////////
	private void decryptFile(File file){
    	File temp = new File(file.getParentFile().getAbsolutePath() + "/" + decryptText(file.getName()));
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
			while ((c = in.read(b)) != -1){
				byte[] output = cipher.update(b, 0, c);
				if (output != null){
					out.write(output);
				}
			}
			
			byte[] output = cipher.doFinal();
			if (output != null)
				out.write(output);
			
			if(in != null)
				in.close();
			if(out != null)
				out.close();
			
			file.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
	///////////////////////////////////////////////////////
	/////// Encrypt files recursively file names //////////
	///////////// It will delete old files ////////////////
    /////// output: Number of files encrypted /////////////
	///////////////////////////////////////////////////////
    public int encryptDirectory(File directory){
    	File[] kids = directory.listFiles();
    	int count = 0;
    	
    	if(kids != null){
    		for(File kid : kids){
    			if(kid.isFile()){
    				encryptFile(kid);
    				count++;
    			}
    			else{
    				count += encryptDirectory(kid);
    			}
    		}
    	}
    	
    	return count;
    }
    
	///////////////////////////////////////////////////////
	/////// Decrypt files recursively file names //////////
	///////////// It will delete old files ////////////////
    /////// output: Number of files decrypted /////////////
	///////////////////////////////////////////////////////
	private int decryptDirectory(File directory){
    	File[] kids = directory.listFiles();
    	int count = 0;
    	
    	if(kids != null){
    		for(File kid : kids){
    			if(kid.isFile()){
    				decryptFile(kid);
    				count++;
    			}
    			else{
    				count += decryptDirectory(kid);
    			}
    		}
    	}
    	
    	return count;
    }
}