package com.example.fineweather.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fineweather.R;
import com.example.fineweather.db.CityInfo;

import java.util.List;

public class CityAdapter extends RecyclerView.Adapter<CityAdapter.ViewHolder> {
    private Context mContext;

    private List<CityInfo> mCityList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView cityName;
        TextView cityTmp;

        public ViewHolder(View view) {
            super(view);
            cardView = (CardView) view;
            cityName = view.findViewById(R.id.city_name);
            cityTmp = view.findViewById(R.id.city_tmp);
        }
    }

    public CityAdapter(List<CityInfo> cityList) {
        mCityList = cityList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (mContext == null) {
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.city_item,
                parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CityInfo city = mCityList.get(position);
        holder.cityName.setText(city.getCityName());
        holder.cityTmp.setText(city.getCityTmp());
    }

    @Override
    public int getItemCount() {
        return mCityList.size();
    }
}
