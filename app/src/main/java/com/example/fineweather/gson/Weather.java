package com.example.fineweather.gson;

import java.util.List;

public class Weather {

    public Basic basic;

    public Update update;

    public String status;

    public Now now;

    public List<Daily_forecast> daily_forecastList;

    public List<Hourly> hourlyList;

    public List<Lifestyle> lifestyleList;
}
