package com.example.fineweather.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.example.fineweather.R;
import com.example.fineweather.adapter.CityAdapter;
import com.example.fineweather.db.CityInfo;
import com.example.fineweather.db.NowDB;
import com.example.fineweather.util.WeatherUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
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

import interfaces.heweather.com.interfacesmodule.bean.Code;
import interfaces.heweather.com.interfacesmodule.bean.Lang;
import interfaces.heweather.com.interfacesmodule.bean.basic.Basic;
import interfaces.heweather.com.interfacesmodule.bean.search.Search;
import interfaces.heweather.com.interfacesmodule.view.HeConfig;
import interfaces.heweather.com.interfacesmodule.view.HeWeather;

public class CityPickerActivity extends AppCompatActivity {

    public LocationClient mLocationClient = null;
    private MyLocationListener myListener = new MyLocationListener();
    String locCity = null;
    String locCityCode = null;
    String locProvince = null;


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

        //声明LocationClient类
        mLocationClient = new LocationClient(getApplicationContext());
        //注册监听函数
        mLocationClient.registerLocationListener(myListener);

        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(CityPickerActivity.this, Manifest.
                permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(CityPickerActivity.this, Manifest.
                permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_NETWORK_STATE);
        }
        if (ContextCompat.checkSelfPermission(CityPickerActivity.this, Manifest.
                permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String [] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(CityPickerActivity.this, permissions, 1);
        } else {
            requestLocation();
        }


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


                mLocationClient.requestLocation();

                requestCityInfo(locCity);

                CityPicker.from(CityPickerActivity.this)
                        .enableAnimation(true)
                        .setHotCities(hotCities)
                        .setOnPickListener(new OnPickListener() {
                            @Override
                            public void onPick(int position, City data) {
                                Log.d(TAG, "onCreate: " + locCity + locProvince + locCityCode);
                                Toast.makeText(getApplicationContext(), data.getName(), Toast.LENGTH_SHORT).show();
                                String cityCode = "CN" + data.getCode();
                                Intent intent = new Intent(CityPickerActivity.this, WeatherActivity.class);
                                saveCityInfo(data.getName(), cityCode);
                                Log.d(TAG, "onPick: " + data.getName() + cityCode);
                                intent.putExtra("cityCode", cityCode);
                                startActivity(intent);
                                CityPickerActivity.this.finish();
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
                                        Log.d(TAG, "run: " + locCity + locProvince + locCityCode);
                                        //定位完成之后更新数据
                                        CityPicker.from(CityPickerActivity.this)
                                                .locateComplete(new LocatedCity(locCity, locProvince, locCityCode), LocateState.SUCCESS);
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
        weatherUtil.saveAirNowCity(cityCode);
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
                Log.d(TAG, "getCityWeather: " + !nowDBList.isEmpty());
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

    public LocationClientOption getOption() {
        LocationClientOption option = new LocationClientOption();

        option.setIsNeedAddress(true);
//可选，是否需要地址信息，默认为不需要，即参数为false
//如果开发者需要获得当前点的地址信息，此处必须为true

        option.setNeedNewVersionRgc(true);
//可选，设置是否需要最新版本的地址信息。默认需要，即参数为true

        mLocationClient.setLocOption(option);
//mLocationClient为第二步初始化过的LocationClient对象
//需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
//更多LocationClientOption的配置，请参照类参考中LocationClientOption类的详细说明
        return option;
    }

    public void requestLocation() {
        mLocationClient.setLocOption(getOption());
        mLocationClient.start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意所有权限才能使用本程序", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
    }

    public class MyLocationListener extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(final BDLocation location) {
            //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
            //以下只列举部分获取地址相关的结果信息
            //更多结果信息获取说明，请参照类参考中BDLocation类中的说明


            locCity = location.getCity();

/*            String addr = location.getAddrStr();    //获取详细地址信息
            String country = location.getCountry();    //获取国家
            String province = location.getProvince();    //获取省份
            String city = location.getCity();    //获取城市
            String district = location.getDistrict();    //获取区县
            String street = location.getStreet();    //获取街道信息
            String adcode = location.getAdCode();    //获取adcode
            String town = location.getTown();    //获取乡镇信息
 */
        }
    }

    public void requestCityInfo(String cityname) {
        HeConfig.init("HE2005102045471023", "a8cbacc0f792408f938372c5c0c4c1f2");
        HeConfig.switchToFreeServerNode();
        HeWeather.getSearch(getApplicationContext(), cityname, "world", 1, Lang.CHINESE_SIMPLIFIED, new HeWeather.OnResultSearchBeansListener() {

            @Override
            public void onError(Throwable throwable) {
                Log.d(TAG, "Search onError: "+"城市回调");
            }

            @Override
            public void onSuccess(Search search) {
                Log.d(TAG, "Search onSuccess: " + new Gson().toJson(search));
                //先判断返回的status是否正确，当status正确时获取数据，若status不正确，可查看status对应的Code值找到原因
                if (Code.OK.getCode().equalsIgnoreCase(search.getStatus())) {
                    Basic basic = search.getBasic().get(0);
                    locCityCode = basic.getCid().substring(2);
                    locCity = basic.getLocation();
                    locProvince = basic.getParent_city();
                } else {
                    String status = search.getStatus();
                    Code code = Code.toEnum(status);
                    Log.d(TAG, "failed code: " + code);
                }
            }
        });
    }
}
