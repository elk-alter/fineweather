package com.example.fineweather.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.fineweather.R;
import com.example.fineweather.db.CityInfo;
import com.example.fineweather.db.ForecastDB;
import com.example.fineweather.db.HourlyDB;
import com.example.fineweather.db.NowDB;
import com.example.fineweather.util.HttpUtil;
import com.example.fineweather.util.WeatherUtil;

import org.jetbrains.annotations.NotNull;
import org.litepal.LitePal;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private static final String TAG = "WeatherActivity";

    private ScrollView weatherLayout;

    private TextView titleCity;

    private TextView titleUpdateTime;

    private TextView degreeText;

    private TextView weatherInfoText;

    private LinearLayout hourlyLayout;

    private LinearLayout forecastLayout;

    private TextView aqiText;

    private TextView pm25Text;

    private SwipeRefreshLayout swipeRefresh;

    private SharedPreferences preferences;

    private SharedPreferences.Editor editor;

    private ImageView bingPicImg;

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
        hourlyLayout = findViewById(R.id.hourly_layout);
        forecastLayout = findViewById(R.id.forecast_layout);
        aqiText = findViewById(R.id.aqi_text);
        pm25Text = findViewById(R.id.pm25_text);
        bingPicImg = findViewById(R.id.bing_pic_img);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        preferences = getPreferences(Activity.MODE_PRIVATE);

        //加载图片
        String binPic = preferences.getString("bing_pic", null);
        Log.d(TAG, "onCreate: "+ "加载图片" + binPic);
        if (binPic != null) {
            Glide.with(this).load(binPic).into(bingPicImg);
        } else {
            Log.d(TAG, "onCreate: " + "加载失败");
            loadBingPic();
        }

        //下拉刷新
        swipeRefresh = findViewById(R.id.swipe_reweather);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshWeather();
            }
        });
        String cityCode = getIntent().getStringExtra("cityCode");


        String defCode = preferences.getString("code", null);
        if (defCode != null) {
            if (isTrueCity(defCode)) {
                showWeatherInfo(defCode);
            } else {
                Intent intentCity = new Intent(WeatherActivity.this, CityPickerActivity.class);
                startActivity(intentCity);
                WeatherActivity.this.finish();
            }
        }

        //储存上次关闭应用时的城市
        editor = preferences.edit();
        editor.clear().apply();
        editor.putString("code", cityCode);
        editor.apply();
        if (getIntent().getIntExtra("step", 0) == 0) {
            requestWeather(cityCode);
            try {
                Thread.sleep(3000);
                showWeatherInfo(cityCode);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            showWeatherInfo(cityCode);
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
        weatherUtil.saveAirNowCity(cityCode);
        Log.d(TAG, "requestWeather: request");
        loadBingPic();
    }


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

        hourlyLayout.removeAllViews();
        for (HourlyDB h : hourlyDBList) {
            View view = LayoutInflater.from(this).inflate(R.layout.hourly_item, hourlyLayout, false);
            TextView timeText = view.findViewById(R.id.time_text);
            TextView weatherText = view.findViewById(R.id.weather_text);
            TextView tmpText = view.findViewById(R.id.tmp_text);
            timeText.setText(h.getTime()+"时");
            weatherText.setText(h.getCond_txt());
            tmpText.setText(h.getTmp());
            hourlyLayout.addView(view);
        }

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

        aqiText.setText(nowDB.getAqi());
        pm25Text.setText(nowDB.getPm25());

        weatherLayout.setVisibility(View.VISIBLE);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    //顶部菜单
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

    //下拉刷新天气
    public void refreshWeather() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                requestWeather(preferences.getString("code", null));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showWeatherInfo(preferences.getString("code", null));
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        }).start();
    }

    //禁用返回
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode == KeyEvent.KEYCODE_BACK;
    }

    //查询当前citycode是否有效
    public boolean isTrueCity(String cityCode) {
        List<CityInfo> cityInfoList = LitePal.where("cityCode = ?", cityCode).find(CityInfo.class);
        List<ForecastDB> forecastDBList = LitePal.where("cityCode = ?", cityCode).find(ForecastDB.class);
        List<HourlyDB> hourlyDBList = LitePal.where("cityCode = ?", cityCode).find(HourlyDB.class);
        List<NowDB> nowDBList = LitePal.where("cityCode = ?", cityCode).find(NowDB.class);

        return !cityInfoList.isEmpty() && !forecastDBList.isEmpty() && !hourlyDBList.isEmpty() && !nowDBList.isEmpty();
    }

    //加载图片
    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        Log.d(TAG, "loadBingPic: " + requestBingPic);
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d(TAG, "onFailure: 完蛋");
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                Log.d(TAG, "onResponse: " + "成功连接api");
                final String bingPic = response.body().string();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });
    }
}
