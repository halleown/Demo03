package com.obdstar.x300dp.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.obdstar.common.ui.listener.OnItemClickListener;
import com.obdstar.x300dp.R;
import com.obdstar.x300dp.model.DiagHistoryBean;

import java.util.List;

/**
 * @author: Luuuzi
 * @Date: 2021-09-08
 * @description: 历史记录右侧列表
 */
public class DiagHistoryRvAdapter extends RecyclerView.Adapter<DiagHistoryRvAdapter.ViewHolder> {

    private final Context mContext;
    private List<DiagHistoryBean> mData;
    private OnItemClickListener itemClickListener;

    public DiagHistoryRvAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void setData(List<DiagHistoryBean> data) {
        this.mData = data;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_diag_hitsory_rv, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DiagHistoryBean item = mData.get(position);
        holder.mTv.setSelected(item.isSelected());
        holder.mTv.setText(item.getDate());

        if (TextUtils.isEmpty(mData.get(position).getDate())) {
            holder.line.setVisibility(View.INVISIBLE);
        }
        String text = mData.get(position).getDate() + "\n(" + mData.get(position).getSubData().size() + ")";
        holder.mTv.setText(text);
        if (mData.get(position).isSelected()) {
            holder.layout_bg.setBackgroundResource(com.obdstar.common.ui.R.drawable.vehicle_list_sel);
            holder.mTv.setSelected(true);
        } else {
            holder.layout_bg.setBackgroundColor(Color.TRANSPARENT);
            holder.mTv.setSelected(false);
        }
        holder.layout_bg.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(v, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layout_bg;
        TextView mTv;
        ImageView line;
        ViewHolder(View itemView) {
            super(itemView);
            layout_bg = itemView.findViewById(com.obdstar.common.ui.R.id.ll_item_name);
            mTv = itemView.findViewById(com.obdstar.common.ui.R.id.tv_item_name);
            line = itemView.findViewById(com.obdstar.common.ui.R.id.line);
        }
    }
}
