package com.example.fineweather.gson;

import com.google.gson.annotations.SerializedName;

public class Basic {

    @SerializedName("location")
    public String cityName;// 地区／城市名称

    @SerializedName("cid")
    public String weatherId;//地区／城市ID

    public String parent_city;//该地区／城市的上级城市

    public String admin_area;//该地区／城市所属行政区域

    public String cnty;//该地区／城市所属国家名称

    public String tz;//该地区／城市所在时区
}
