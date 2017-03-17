package com.scheme.chc.lockscreen.weather;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Weather {


    private double latitude;
    private double longitude;

    public Weather(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
//    private static String IMG_URL = "http://openweathermap.org/img/w/";

    public String getWeatherData() {
        HttpURLConnection con = null ;
        InputStream is = null;
        System.out.println((int)latitude +": long: " +(int)longitude);
        String BASE_URL = "http://api.openweathermap.org/data/2.5/weather?lat="
                + (int) latitude + "&lon=" + (int) longitude + "&units=metric&appid=83fd0ca56a1443005dd7210832cf3938";

        try {
            con = (HttpURLConnection) ( new URL(BASE_URL)).openConnection();
            con.setRequestMethod("GET");
            con.setDoInput(true);
            con.setDoOutput(true);
            con.connect();

            // Let's read the response
            StringBuilder buffer = new StringBuilder();
            is = con.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while (  (line = br.readLine()) != null )
                buffer.append(line).append("\r\n");

            is.close();
            con.disconnect();
            return buffer.toString();
        }
        catch(Throwable t) {
            t.printStackTrace();


        }
        finally {
            try {
                assert is != null;
                is.close(); } catch(Throwable ignored) {}
            try {
                assert con != null;
                con.disconnect(); } catch(Throwable ignored) {}
        }

        return null;

    }

//    byte[] getImage(String code) {
//        HttpURLConnection con = null ;
//        InputStream is = null;
//        try {
//            con = (HttpURLConnection) ( new URL(IMG_URL + code)).openConnection();
//            con.setRequestMethod("GET");
//            con.setDoInput(true);
//            con.setDoOutput(true);
//            con.connect();
//
//            // Let's read the response
//            is = con.getInputStream();
//            byte[] buffer = new byte[1024];
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//
//            while ( is.read(buffer) != -1)
//                baos.write(buffer);
//
//            return baos.toByteArray();
//        }
//        catch(Throwable t) {
//            t.printStackTrace();
//        }
//        finally {
//            try {
//                assert is != null;
//                is.close(); } catch(Throwable ignored) {}
//            try {
//                assert con != null;
//                con.disconnect(); } catch(Throwable ignored) {}
//        }
//
//        return null;
//
//    }
}