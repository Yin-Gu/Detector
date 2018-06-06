package com.example.administrator.demo.ui.condition_monitoring.adapter;

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

    private final static String TAG = "DataMonitoringAdapter";

    private List<DataMonitoringItem> mItemList;

    private ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public DataMonitoringAdapter(List<DataMonitoringItem> mItemList) {
        this.mItemList = mItemList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_data_monitoring, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position){
        //奇数项为白色
        holder.view.setTag(position);
        if ((position & 0x1) == 0) {
            holder.view.setBackgroundColor(Color.WHITE);
        } else {
            holder.view.setBackgroundColor(Color.parseColor("#FFF5EE"));
        }

        DataMonitoringItem item = mItemList.get(position);
        holder.tv_motor_name_value.setText(item.getMotorName());
        if (item.getStatus()){
            holder.status.setColorFilter(Color.YELLOW);
        }else {
            holder.status.setColorFilter(Color.GREEN);
        }

        String[] attrs = mItemList.get(position).getAttrs();
        String[] values = mItemList.get(position).getValues();
        int shortStart = 0, longStart = 6;
        int shortEnd = 6, longEnd = 8;
        for (int i = 0; i < attrs.length; i++) {
            String text = attrs[i] + ": " + values[i];
            if (attrs[i].length() < 10) {
                holder.textViews[shortStart++].setText(text);
            } else {
                holder.textViews[longStart++].setText(text);
            }
        }

        while (shortStart < shortEnd) {
            holder.textViews[shortStart++].setVisibility(View.GONE);
        }
        while (longStart < longEnd) {
            holder.textViews[longStart++].setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount(){
        return mItemList.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_motor_name_value) TextView tv_motor_name_value;
        @BindView(R.id.iv_LED) ImageView status;
        @BindView(R.id.tv_item_1) TextView tv_item_1;
        @BindView(R.id.tv_item_2) TextView tv_item_2;
        @BindView(R.id.tv_item_3) TextView tv_item_3;
        @BindView(R.id.tv_item_4) TextView tv_item_4;
        @BindView(R.id.tv_item_5) TextView tv_item_5;
        @BindView(R.id.tv_item_6) TextView tv_item_6;
        @BindView(R.id.tv_item_7) TextView tv_item_7;
        @BindView(R.id.tv_item_8) TextView tv_item_8;

        private View view;
        private TextView[] textViews;

        ViewHolder(View view) {
            super(view);
            this.view = view;
            ButterKnife.bind(this, view);
            textViews = new TextView[]{tv_item_1, tv_item_2, tv_item_3, tv_item_4,
                    tv_item_5, tv_item_6, tv_item_7, tv_item_8};

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (itemClickListener != null)
                        itemClickListener.onItemClick(view, (int)view.getTag());
                }
            });
        }
    }

}
