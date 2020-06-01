package com.example.fineweather.util;

import android.util.Log;

import com.example.fineweather.db.ForecastDB;
import com.example.fineweather.db.HourlyDB;
import com.example.fineweather.db.NowDB;
import com.google.gson.Gson;

import org.litepal.LitePal;

import java.util.List;

import interfaces.heweather.com.interfacesmodule.bean.Code;
import interfaces.heweather.com.interfacesmodule.bean.Lang;
import interfaces.heweather.com.interfacesmodule.bean.Unit;
import interfaces.heweather.com.interfacesmodule.bean.air.now.AirNow;
import interfaces.heweather.com.interfacesmodule.bean.weather.forecast.Forecast;
import interfaces.heweather.com.interfacesmodule.bean.weather.forecast.ForecastBase;
import interfaces.heweather.com.interfacesmodule.bean.weather.hourly.Hourly;
import interfaces.heweather.com.interfacesmodule.bean.weather.hourly.HourlyBase;
import interfaces.heweather.com.interfacesmodule.bean.weather.now.Now;
import interfaces.heweather.com.interfacesmodule.bean.weather.now.NowBase;
import interfaces.heweather.com.interfacesmodule.view.HeConfig;
import interfaces.heweather.com.interfacesmodule.view.HeWeather;

/**
 * 需要参数为 城市代码 cityCode
 * 向接口请求各种天气数据
 */
public class WeatherUtil {

    private static final String TAG = "WeatherUtil";

    public WeatherUtil () {

    }
    public void saveNowInfo (final String cityCode) {
        LitePal.getDatabase();
        HeConfig.init("HE2005102045471023", "a8cbacc0f792408f938372c5c0c4c1f2");
        HeConfig.switchToFreeServerNode();
        HeWeather.getWeatherNow(MyApplication.getContext(), cityCode, Lang.CHINESE_SIMPLIFIED, Unit.METRIC,
                new HeWeather.OnResultWeatherNowBeanListener() {
                    @Override
                    public void onError(Throwable throwable) {
                        Log.d(TAG, "Weather Now onError: ", throwable);
                    }

                    @Override
                    public void onSuccess(Now dataObject) {
                        Log.d(TAG, " Weather Now onSuccess: " + new Gson().toJson(dataObject));
                        //先判断返回的status是否正确，当status正确时获取数据，若status不正确，可查看status对应的Code值找到原因
                        if ( Code.OK.getCode().equalsIgnoreCase(dataObject.getStatus()) ){
                            //此时返回数据
                            NowBase nowBase = dataObject.getNow();

                            Log.d(TAG, "onSuccess: 1");
                            List<NowDB> nowDBList = LitePal.findAll(NowDB.class);
                            for (NowDB n : nowDBList) {
                                if (n.getCityCode().equals(cityCode)) {
                                    n.delete();
                                }
                            }

                            NowDB now = new NowDB();
                            now.setCityCode(cityCode);
                            now.setLocTime(dataObject.getUpdate().getLoc());
                            now.setFl(nowBase.getFl());
                            now.setTmp(nowBase.getTmp());
                            now.setCond_txt(nowBase.getCond_txt());
                            now.setWind_dir(nowBase.getWind_dir());
                            now.setWind_sc(nowBase.getWind_sc());
                            now.setHum(nowBase.getHum());
                            now.setPcpn(nowBase.getPcpn());
                            now.save();

                            Log.d(TAG, "onSuccess: 2");
                        } else {
                            //在此查看返回数据失败的原因
                            String status = dataObject.getStatus();
                            Code code = Code.toEnum(status);
                            Log.d(TAG, "failed code: " + code);
                        }
                    }
                });
    }

    public void saveForecastInfo (final String cityCode) {
        HeWeather.getWeatherForecast(MyApplication.getContext(), cityCode, Lang.CHINESE_SIMPLIFIED, Unit.METRIC,
                new HeWeather.OnResultWeatherForecastBeanListener() {
                    @Override
                    public void onError(Throwable throwable) {
                        Log.d(TAG, "Weather Forecast onError: ", throwable);
                    }

                    @Override
                    public void onSuccess(Forecast dataObject) {
                        Log.d(TAG, " Weather Forecast onSuccess: " + new Gson().toJson(dataObject));

                        if ( Code.OK.getCode().equalsIgnoreCase(dataObject.getStatus()) ){
                            //此时返回数据

                            List<ForecastDB> forecastDBList = LitePal.findAll(ForecastDB.class);
                            for (ForecastDB f : forecastDBList) {
                                if (f.getCityCode().equals(cityCode)) {
                                    f.delete();
                                }
                            }

                            List<ForecastBase> forecastBase = dataObject.getDaily_forecast();
                            for (ForecastBase f : forecastBase) {
                                ForecastDB forecastDB = new ForecastDB();
                                forecastDB.setCityCode(cityCode);
                                forecastDB.setLocTime(dataObject.getUpdate().getLoc());
                                forecastDB.setDate(f.getDate());
                                forecastDB.setTmp_max(f.getTmp_max());
                                forecastDB.setTmp_min(f.getTmp_min());
                                forecastDB.setCond_txt(f.getCond_txt_d());
                                forecastDB.setPop(f.getPop());
                                forecastDB.save();
                            }
                        } else {
                            //在此查看返回数据失败的原因
                            String status = dataObject.getStatus();
                            Code code = Code.toEnum(status);
                            Log.d(TAG, "failed code: " + code);
                        }
                    }
                });
    }

    public void saveHourlyInfo (final String cityCode) {
        HeWeather.getWeatherHourly(MyApplication.getContext(), cityCode, Lang.CHINESE_SIMPLIFIED, Unit.METRIC,
                new HeWeather.OnResultWeatherHourlyBeanListener() {
                    @Override
                    public void onError(Throwable throwable) {
                        Log.d(TAG, "Weather Hourly onError: ", throwable);
                    }

                    @Override
                    public void onSuccess(Hourly dataObject) {
                        Log.d(TAG, " Weather Hourly onSuccess: " + new Gson().toJson(dataObject));

                        if ( Code.OK.getCode().equalsIgnoreCase(dataObject.getStatus()) ){
                            //此时返回数据

                            List<HourlyDB> hourlyDBList = LitePal.findAll(HourlyDB.class);
                            for (HourlyDB h : hourlyDBList) {
                                if (h.getCityCode().equals(cityCode)) {
                                    h.delete();
                                }
                            }

                            List<HourlyBase> hourlyBase = dataObject.getHourly();
                            for (HourlyBase h : hourlyBase) {
                                HourlyDB hourlyDB = new HourlyDB();
                                hourlyDB.setCityCode(cityCode);
                                hourlyDB.setLocTime(dataObject.getUpdate().getLoc());
                                hourlyDB.setTime(h.getTime());
                                hourlyDB.setTmp(h.getTmp());
                                hourlyDB.setCond_txt(h.getCond_txt());
                                hourlyDB.save();
                            }
                        } else {
                            //在此查看返回数据失败的原因
                            String status = dataObject.getStatus();
                            Code code = Code.toEnum(status);
                            Log.d(TAG, "failed code: " + code);
                        }
                    }
                });
    }

    public void saveAirNowCity (final String cityCode) {
        HeWeather.getAirNow(MyApplication.getContext(), cityCode, Lang.CHINESE_SIMPLIFIED, Unit.METRIC,
                new HeWeather.OnResultAirNowBeansListener() {
                    @Override
                    public void onError(Throwable throwable) {
                        Log.d(TAG, "AirNow onError: ", throwable);
                    }

                    @Override
                    public void onSuccess(AirNow dataObject) {
                        Log.d(TAG, "AirNow onSuccess: " + new Gson().toJson(dataObject));

                        if ( Code.OK.getCode().equalsIgnoreCase(dataObject.getStatus()) ){
                            //此时返回数据

                            List<NowDB> nowDBList = LitePal.findAll(NowDB.class);
                            for (NowDB n : nowDBList) {
                                if (n.getCityCode().equals(cityCode)) {
                                    n.setAqi(dataObject.getAir_now_city().getAqi());
                                    n.setPm25(dataObject.getAir_now_city().getPm25());
                                    n.save();
                                }
                            }
                        } else {
                            //在此查看返回数据失败的原因
                            String status = dataObject.getStatus();
                            Code code = Code.toEnum(status);
                            Log.d(TAG, "failed code: " + code);
                        }
                    }
                });
    }
}
