package com.example.fineweather.db;

import org.litepal.crud.LitePalSupport;

public class CityInfo extends LitePalSupport {

    private int id;

    private String cityName;

    private String cityCode;

    private String cityTmp;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getCityTmp() {
        return cityTmp;
    }

    public void setCityTmp(String cityTmp) {
        this.cityTmp = cityTmp;
    }
}
