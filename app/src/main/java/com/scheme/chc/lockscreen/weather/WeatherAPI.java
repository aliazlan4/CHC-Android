package com.scheme.chc.lockscreen.weather;

import android.os.AsyncTask;

import com.scheme.chc.lockscreen.utils.AppSharedPrefs;

import org.json.JSONException;

/**
 * Created by Ender on 08-Apr-17 for CHC-Android-master
 */

public class WeatherAPI extends AsyncTask<Void, Void, WeatherModel> {

    private double latitude, longitude;
    private WeatherListener weatherListener;

    public WeatherAPI(double latitude, double longitude, WeatherListener weatherListener) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.weatherListener = weatherListener;
    }

    @Override
    protected WeatherModel doInBackground(Void... params) {
        WeatherModel weather = new WeatherModel();
        String data = ((new Weather(latitude, longitude)).getWeatherData());
        try {
            weather = JSONWeatherParser.getWeather(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return weather;
    }

    @Override
    protected void onPostExecute(WeatherModel weatherModel) {
        super.onPostExecute(weatherModel);

        AppSharedPrefs appSharedPrefs = AppSharedPrefs.getInstance();
        LocationModel locationModel = new LocationModel();
        appSharedPrefs.setSunset(locationModel.getSunset());
        appSharedPrefs.setSunrise(locationModel.getSunrise());
        appSharedPrefs.setWeatherId(weatherModel.currentCondition.getWeatherId());
        appSharedPrefs.setTemperature(String.valueOf((int) (weatherModel.temperature.getTemp())));
        appSharedPrefs.setCondition(String.valueOf(weatherModel.currentCondition.getCondition()));

        weatherModel.temperature.setTemp(0);
        System.out.println("hi: " + (int) (weatherModel.temperature.getTemp()));
        weatherListener.onWeatherReturned(locationModel, weatherModel);
    }

    public interface WeatherListener {
        void onWeatherReturned(LocationModel locationModel, WeatherModel weatherModel);
    }
}
