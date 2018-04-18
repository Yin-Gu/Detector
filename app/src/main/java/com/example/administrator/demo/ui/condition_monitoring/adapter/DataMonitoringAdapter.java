package com.example.administrator.demo.ui.condition_monitoring.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.administrator.demo.R;
import com.example.administrator.demo.model.condition_monitoring.DataMonitoringItem;
import com.example.administrator.demo.ui.common.ItemClickListener;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DataMonitoringAdapter extends RecyclerView.Adapter<DataMonitoringAdapter.ViewHolder> {

    private List<DataMonitoringItem> mItemList;

    public DataMonitoringAdapter(List<DataMonitoringItem> mItemList) {
        this.mItemList = mItemList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_data_monitoring,parent,false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    private ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder,int position){
        holder.view.setTag(position);
        DataMonitoringItem item = mItemList.get(position);
        holder.equipmentName.setText(item.getEquipmentName());
        if (item.getStatus()){
            holder.status.setColorFilter(Color.RED);
        }else {
            holder.status.setColorFilter(Color.GREEN);
        }
        holder.temperature.setText(item.getTemperature());
        holder.current.setText(item.getElectricCurrent());
        holder.voltage.setText(item.getVoltage());
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_equipment_name) TextView equipmentName;
        @BindView(R.id.iv_LED) ImageView status;
        @BindView(R.id.tv_temperature) TextView temperature;
        @BindView(R.id.tv_current) TextView current;
        @BindView(R.id.tv_voltage) TextView voltage;

        View view;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            ButterKnife.bind(this, view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (itemClickListener != null)
                        itemClickListener.onItemClick(view, (int)view.getTag());
                }
            });
        }
    }

    @Override
    public int getItemCount(){
        return mItemList.size();
    }
}
