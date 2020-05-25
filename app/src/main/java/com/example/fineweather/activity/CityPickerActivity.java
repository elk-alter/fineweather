package com.example.fineweather.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.fineweather.R;
import com.example.fineweather.adapter.CityAdapter;
import com.example.fineweather.db.CityInfo;
import com.example.fineweather.db.NowDB;
import com.example.fineweather.util.WeatherUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.zaaach.citypicker.CityPicker;
import com.zaaach.citypicker.adapter.OnPickListener;
import com.zaaach.citypicker.model.City;
import com.zaaach.citypicker.model.HotCity;
import com.zaaach.citypicker.model.LocateState;
import com.zaaach.citypicker.model.LocatedCity;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CityPickerActivity extends AppCompatActivity {

    private static final String TAG = "CityPickerActivity";

    private CityAdapter cityAdapter;

    private SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_picker);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        //城市卡片设置
        List<CityInfo> cityList = getCityWeather();
        if (cityList != null) {
            RecyclerView recyclerView = findViewById(R.id.recycler_view);
            GridLayoutManager layoutManager = new GridLayoutManager(this, 1);
            recyclerView.setLayoutManager(layoutManager);
            cityAdapter = new CityAdapter(cityList);
            recyclerView.setAdapter(cityAdapter);
        }

        //下拉刷新
        swipeRefresh = findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshCity();
            }
        });

        //城市选择
        FloatingActionButton fab = findViewById(R.id.add_city_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<HotCity> hotCities = new ArrayList<>();
                hotCities.add(new HotCity("北京", "北京", "101010100")); //code为城市代码
                hotCities.add(new HotCity("上海", "上海", "101020100"));
                hotCities.add(new HotCity("广州", "广东", "101280101"));
                hotCities.add(new HotCity("深圳", "广东", "101280601"));
                hotCities.add(new HotCity("杭州", "浙江", "101210101"));

                CityPicker.from(CityPickerActivity.this)
                        .enableAnimation(true)
                        .setLocatedCity(new LocatedCity("杭州", "浙江", "101210101"))
                        .setHotCities(hotCities)
                        .setOnPickListener(new OnPickListener() {
                            @Override
                            public void onPick(int position, City data) {
                                Toast.makeText(getApplicationContext(), data.getName(), Toast.LENGTH_SHORT).show();
                                String cityCode = "CN" + data.getCode();
                                Intent intent = new Intent(CityPickerActivity.this, WeatherActivity.class);
                                saveCityInfo(data.getName(), cityCode);
                                Log.d(TAG, "onPick: " + data.getName() + cityCode);
                                intent.putExtra("cityCode", cityCode);
                                startActivity(intent);
                                finish();
                            }

                            @Override
                            public void onCancel(){
                                Toast.makeText(getApplicationContext(), "取消选择", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onLocate() {
                                //定位接口，需要APP自身实现，这里模拟一下定位
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        //定位完成之后更新数据
                                        CityPicker.from(CityPickerActivity.this)
                                                .locateComplete(new LocatedCity("深圳", "广东", "101280601"), LocateState.SUCCESS);
                                    }
                                }, 3000);
                            }
                        })
                        .show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

    public void saveCityInfo (String cityName, String cityCode) {
        List<CityInfo> cityInfoList = LitePal.findAll(CityInfo.class);
        for (CityInfo c : cityInfoList) {
            if (c.getCityCode().equals(cityCode)) {
                c.delete();
            }
        }

        CityInfo cityInfo = new CityInfo();
        cityInfo.setCityName(cityName);
        cityInfo.setCityCode(cityCode);
        cityInfo.save();
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

    /**
     * 获取CityList以及保存城市天气数据
     */
    public List<CityInfo> getCityWeather() {
        List<CityInfo> cityInfoList = LitePal.findAll(CityInfo.class);
        if (cityInfoList != null) {
            for (CityInfo cityInfo : cityInfoList) {
                String cityCode = cityInfo.getCityCode();
                Log.d(TAG, "getCityWeather: " + cityCode);
                List<NowDB> nowDBList = LitePal.where("cityCode = ?", cityCode).find(NowDB.class);
                Log.d(TAG, "getCityWeather: " + nowDBList.isEmpty());
                if (!nowDBList.isEmpty()) {
                    NowDB nowDB = nowDBList.get(0);
                    String tmp = nowDB.getTmp();
                    cityInfo.setCityTmp(tmp);
                }
            }
        }
        return cityInfoList;
    }


    //下拉刷新
    public void refreshCity() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<CityInfo> cityInfoList = LitePal.findAll(CityInfo.class);
                for (CityInfo cityInfo : cityInfoList) {
                    requestWeather(cityInfo.getCityCode());
                    if (LitePal.where("cityCode = ?", cityInfo.getCityCode()).find(NowDB.class) != null) {
                        NowDB nowDB = LitePal.where("cityCode = ?", cityInfo.getCityCode()).find(NowDB.class).get(0);
                        String tmp = nowDB.getTmp();
                        cityInfo.setCityTmp(tmp);
                        cityInfo.save();
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        cityAdapter.notifyDataSetChanged();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        }).start();
    }
}
