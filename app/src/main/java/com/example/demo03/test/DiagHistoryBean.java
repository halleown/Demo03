package com.obdstar.x300dp.model;

import com.obdstar.module.data.manager.entity.DiagReportListBean;

import java.io.File;
import java.util.List;

public class DiagHistoryBean {
    private String date;
    private List<DiagReportListBean> subData;
    private boolean selected;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<DiagReportListBean> getSubData() {
        return subData;
    }

    public void setSubData(List<DiagReportListBean> subData) {
        this.subData = subData;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public static class Sub{
        private File file;
        private boolean isCheck;

        public File getFile() {
            return file;
        }

        public void setFile(File file) {
            this.file = file;
        }

        public boolean isCheck() {
            return isCheck;
        }

        public void setCheck(boolean check) {
            isCheck = check;
        }
    }
}
