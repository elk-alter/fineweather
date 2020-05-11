package com.example.fineweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Weather {

    public Basic basic;

    public Update update;

    public String status;

    public Now now;

    @SerializedName("daily_forecast")
    public List<Daily_forecast> daily_forecastList;

    @SerializedName("hourly")
    public List<Hourly> hourlyList;

    @SerializedName("lifestyle")
    public List<Lifestyle> lifestyleList;
}
