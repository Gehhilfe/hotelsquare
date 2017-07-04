package tk.internet.praktikum.storage;

import android.support.annotation.NonNull;

import org.greenrobot.greendao.annotation.NotNull;

import java.util.Date;

/**
 * Created by truongtud on 04.07.2017.
 */

public abstract  class HistoryEntryBase {
    @NotNull
    private  String uid;
    @NonNull
    private String historyName;
    private Date date;

    public HistoryEntryBase(String uid, String historyName, Date date) {
        this.uid = uid;
        this.historyName = historyName;
        this.date = date;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getHistoryName() {
        return historyName;
    }

    public void setHistoryName(String historyName) {
        this.historyName = historyName;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
