package com.obdstar.x300dp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.obdstar.common.core.Constants;
import com.obdstar.common.ui.listener.OnItemClickListener;
import com.obdstar.common.ui.listener.OnItemLongClickListener;
import com.obdstar.module.data.manager.entity.DiagReportListBean;
import com.obdstar.x300dp.R;

import org.w3c.dom.Text;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 历史记录列表
 */
public class DiagHistorySubAdapter extends RecyclerView.Adapter<DiagHistorySubAdapter.ViewHolder> {

    private final Context mContext;
    private List<DiagReportListBean> mData;
    private boolean deleteMode = false;
    private OnItemClickListener itemClickListener;
    private EndOnClick endOnClick;
    private OnItemLongClickListener itemLongClickListener;
    private Map<String, Integer> faultNumberMap;
    public DiagHistorySubAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void setData(List<DiagReportListBean> data) {
        this.mData = data;
        notifyDataSetChanged();
    }

    public void setFaultData(Map<String, Integer> faultNumberMap) {
        this.faultNumberMap = faultNumberMap;
        notifyDataSetChanged();
    }

    public boolean isDeleteMode() {
        return deleteMode;
    }

    public void setDeleteMode(boolean deleteMode) {
        this.deleteMode = deleteMode;
    }

    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void setEndOnClick(EndOnClick endOnClick) {
        this.endOnClick = endOnClick;
    }
    public void setItemLongClickListener(OnItemLongClickListener itemLongClickListener) {
        this.itemLongClickListener = itemLongClickListener;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_diag_history_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DiagReportListBean sub = mData.get(position);
        if (!Constants.isDP83Device && !Constants.isMD75CANDODevice && !Constants.isF8SMDevice) {
            holder.tv_banner.setText(sub.bran);
        }
        Integer faultNumber = faultNumberMap.get(sub.name);
        if (faultNumber != null && faultNumber > 0) {
            holder.tv_fault_code.setTextColor(mContext.getResources().getColor(com.obdstar.common.ui.R.color.red));
        } else {
            holder.tv_fault_code.setTextColor(mContext.getResources().getColor(com.obdstar.module.data.manager.R.color.deep_gray));
        }
        holder.tv_fault_code.setText(mContext.getResources().getString(R.string.history_fault_codes) + ": " + (faultNumber == null || faultNumber < 0 ? 0 : faultNumber));
        holder.tv_name.setText(sub.name);
        File parentFile = sub.getFile().getParentFile();
        if (parentFile != null && parentFile.exists()) {
            String vin = parentFile.getName();
            if ("DATA".equalsIgnoreCase(vin)) {
                holder.tv_vin.setText(String.format("VIN：%s", ""));
            } else {
                holder.tv_vin.setText(String.format("VIN：%s", vin));
            }
        }
        holder.tvTime.setText(sub.time);
        holder.tv_state.setText(sub.state);
        // if (position % 2 == 0) {
        //     holder.rl_bg.setBackgroundResource(com.obdstar.common.ui.R.drawable.bg_menu_item_double);
        // } else {
        //     holder.rl_bg.setBackgroundResource(com.obdstar.common.ui.R.drawable.bg_menu_item_one);
        // }
//        if (position % 2 == 0) {
//            holder.rl_bg.setBackgroundColor(Color.parseColor("#d8ddde"));
//        } else {
//            holder.rl_bg.setBackgroundColor(Color.parseColor("#e6e6e6"));
//        }
        if (isDeleteMode()) {
            holder.iv_check.setVisibility(View.VISIBLE);
            holder.iv_check.setImageResource(com.obdstar.common.ui.R.drawable.checkbox_bg_svg);
        } else {
            holder.iv_check.setVisibility(View.INVISIBLE);
        }

        if (sub.isCheck()) {
            holder.iv_check.setImageResource(com.obdstar.common.ui.R.drawable.checkbox_sel_svg);
        } else {
            holder.iv_check.setImageResource(com.obdstar.common.ui.R.drawable.checkbox_bg_svg);
        }

        holder.rl_bg.setOnClickListener(v -> {
            if (isDeleteMode()) {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(v, position);
                }
            } else {
                if (endOnClick != null) {
                    endOnClick.endOnClick(v, position);
                }
            }
        });
        holder.rl_bg.setOnLongClickListener(v -> {
            if (itemLongClickListener != null) {
                itemLongClickListener.onItemlongClick(v,position);
            }
            return true;
        });

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView iv_check;
        private final TextView tv_banner;
        private final TextView tv_name;
        private final TextView tv_vin;
        private final TextView tvTime;
        private final TextView tv_state;
        private final TextView tv_fault_code;
        private final ConstraintLayout rl_bg;
        //   private final TextView tv_end;

        ViewHolder(View itemView) {
            super(itemView);
            rl_bg = itemView.findViewById(R.id.ll_bg);
            tv_banner = itemView.findViewById(R.id.tv_banner);
            tv_name = itemView.findViewById(R.id.tv_name);
            tv_vin = itemView.findViewById(R.id.tv_vin);
            tvTime = itemView.findViewById(R.id.tv_time);
            tv_state = itemView.findViewById(R.id.tv_state);
            //   tv_end = itemView.findViewById(R.id.tv_end);
            iv_check = itemView.findViewById(R.id.iv_check);
            tv_fault_code = itemView.findViewById(R.id.tv_fault_code);
        }
    }

    public interface EndOnClick {
        void endOnClick(View v, int position);
    }
}