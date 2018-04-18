package com.example.administrator.demo.ui.homepage.adpater;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.administrator.demo.R;
import com.example.administrator.demo.model.hompage.HomepageItem;

import java.util.List;
import com.example.administrator.demo.ui.common.ItemClickListener;

import butterknife.BindView;
import butterknife.ButterKnife;


public class HomepageAdapter extends RecyclerView.Adapter<HomepageAdapter.ViewHolder>{
    private List<HomepageItem> mItemList;

    public HomepageAdapter(List<HomepageItem> itemList){
        mItemList = itemList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_homepage,parent,false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    private ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder,int position){
        HomepageItem item = mItemList.get(position);
        holder.itemImage.setImageResource(item.getItemImg());
        holder.itemName.setText(item.getItemName());
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_Image) ImageView itemImage;
        @BindView(R.id.item_Name) TextView itemName;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (itemClickListener != null)
                        itemClickListener.onItemClick(view, getLayoutPosition());
                }
            });
        }
    }

    @Override
    public int getItemCount(){
        return mItemList.size();
    }

}