package com.example.fineweather.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.fineweather.R;
import com.example.fineweather.db.CityInfo;
import com.example.fineweather.db.ForecastDB;
import com.example.fineweather.db.HourlyDB;
import com.example.fineweather.db.NowDB;
import com.example.fineweather.gson.Hourly;
import com.example.fineweather.gson.Weather;
import com.example.fineweather.util.WeatherUtil;

import org.litepal.LitePal;

import java.util.List;

public class WeatherActivity extends AppCompatActivity {

    private static final String TAG = "WeatherActivity";

    private ScrollView weatherLayout;

    private TextView titleCity;

    private TextView titleUpdateTime;

    private TextView degreeText;

    private TextView weatherInfoText;

    private LinearLayout forecastLayout;

    private TextView aqiText;

    private TextView pm25Text;

    private TextView comfortText;

    private TextView carWashText;

    private TextView sportText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        LitePal.getDatabase();
        //初始化各控件
        weatherLayout = findViewById(R.id.weather_layout);
        titleCity = findViewById(R.id.title_city);
        titleUpdateTime = findViewById(R.id.title_update_time);
        degreeText = findViewById(R.id.degree_text);
        weatherInfoText = findViewById(R.id.weather_info_text);
        forecastLayout = findViewById(R.id.forecast_layout);
        aqiText = findViewById(R.id.aqi_text);
        pm25Text = findViewById(R.id.pm25_text);
        comfortText = findViewById(R.id.comfort_text);
        carWashText = findViewById(R.id.car_wash_text);
        sportText = findViewById(R.id.sport_text);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String cityCode = getIntent().getStringExtra("cityCode");

        requestWeather(cityCode);

        try {
            Thread.sleep(3000);
            showWeatherInfo(cityCode);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 根据天气id请求城市天气信息
     */
    public void requestWeather(final String cityCode) {
        WeatherUtil weatherUtil = new WeatherUtil();
        weatherUtil.saveNowInfo(cityCode);
        weatherUtil.saveForecastInfo(cityCode);
        weatherUtil.saveHourlyInfo(cityCode);
        Log.d(TAG, "requestWeather: request");
    }


    //TODO 空气质量
    /**
     * 处理并显示Weather实体类中的数据
     */
    private void showWeatherInfo(String cityCode) {
        CityInfo cityInfo = LitePal.where("cityCode = ?", cityCode).find(CityInfo.class).get(0);
        Log.d(TAG, "showWeatherInfo: " + cityInfo.getCityName());
        NowDB nowDB = LitePal.where("cityCode = ?", cityCode).find(NowDB.class).get(0);
        List<ForecastDB> forecastDBList = LitePal.where("cityCode = ?", cityCode).find(ForecastDB.class);
        List<HourlyDB> hourlyDBList = LitePal.where("cityCode = ?", cityCode).find(HourlyDB.class);


        Log.d(TAG, "showWeatherInfo: " + nowDB.getCond_txt());
        titleCity.setText(cityInfo.getCityName());
        titleUpdateTime.setText(nowDB.getLocTime());
        degreeText.setText(nowDB.getTmp());
        weatherInfoText.setText(nowDB.getCond_txt());

        forecastLayout.removeAllViews();
        for (ForecastDB f : forecastDBList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = view.findViewById(R.id.date_text);
            TextView infoText = view.findViewById(R.id.info_text);
            TextView maxText = view.findViewById(R.id.max_text);
            TextView minText = view.findViewById(R.id.min_text);
            dateText.setText(f.getDate());
            infoText.setText(f.getCond_txt());
            maxText.setText(f.getTmp_max());
            minText.setText(f.getTmp_min());
            forecastLayout.addView(view);
        }

        aqiText.setText("1");
        pm25Text.setText("2");

        comfortText.setText("3");
        carWashText.setText("4");
        sportText.setText("5");
        weatherLayout.setVisibility(View.VISIBLE);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.city:
                Intent intentCity = new Intent(WeatherActivity.this, CityPickerActivity.class);
                startActivity(intentCity);
                break;
            case R.id.settings:
                break;
            case R.id.about:
                break;
            default:
        }
        return true;
    }
}
