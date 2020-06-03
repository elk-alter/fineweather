package com.example.fineweather.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fineweather.R;
import com.example.fineweather.activity.WeatherActivity;
import com.example.fineweather.db.CityInfo;

import org.litepal.LitePal;

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

        final ViewHolder holder = new ViewHolder(view);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                CityInfo cityInfo = mCityList.get(position);
                Intent intent = new Intent(mContext, WeatherActivity.class);
                intent.putExtra("cityCode", cityInfo.getCityCode());
                intent.putExtra("step", 1);
                mContext.startActivity(intent);
            }
        });
        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final int position = holder.getAdapterPosition();
                final CityInfo cityInfo = mCityList.get(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext).setIcon(R.mipmap.ic_launcher)
                        .setTitle("注意").setMessage("是否要删除" + cityInfo.getCityName() + "的天气信息?");
                builder.setPositiveButton("是的", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(mContext, "已删除", Toast.LENGTH_SHORT).show();
                        mCityList.remove(position);
                        LitePal.delete(CityInfo.class, cityInfo.getId());
                        notifyDataSetChanged();
                    }
                });
                builder.setNegativeButton("算了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(mContext, "取消", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
                builder.create().show();
                return true;
            }
        });
        return holder;
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
