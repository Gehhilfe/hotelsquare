package tk.internet.praktikum.foursquare.history;


import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.converter.PropertyConverter;

import java.util.Date;

/**
 * Created by truongtud on 04.07.2017.
 */
@Entity(nameInDb = "history")
public class HistoryEntry {

    @NotNull
    @Id
    private String uid;

    @Convert(converter = HistoryConverter.class, columnType = Integer.class)
    private HistoryType historyType;

    @NotNull
    private String venueName;

    @NotNull
    private String referenceVenueId;

    @NotNull
    private Date date;

    @Generated(hash = 22492394)
    public HistoryEntry(@NotNull String uid, HistoryType historyType, @NotNull String venueName, @NotNull String referenceVenueId,
            @NotNull Date date) {
        this.uid = uid;
        this.historyType = historyType;
        this.venueName = venueName;
        this.referenceVenueId = referenceVenueId;
        this.date = date;
    }

    public HistoryEntry() {
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @NotNull
    public HistoryType getHistoryType() {
        return historyType;
    }

    public void setHistoryType(@NotNull HistoryType historyType) {
        this.historyType = historyType;
    }

    public String getVenueName() {
        return venueName;
    }

    public void setVenueName(String venueName) {
        this.venueName = venueName;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }


    public String getReferenceVenueId() {
        return this.referenceVenueId;
    }


    public void setReferenceVenueId(String referenceVenueId) {
        this.referenceVenueId = referenceVenueId;
    }

    static class HistoryConverter implements PropertyConverter<HistoryType, Integer> {
        @Override
        public HistoryType convertToEntityProperty(Integer databaseValue) {
            if(databaseValue == null) {
                return null;
            } else {
                for(HistoryType historyType : HistoryType.values()) {
                    if(historyType.getValue()== databaseValue) {
                        return historyType;
                    }
                }
                throw new DaoException("Can not convert HistoryType from database value: " + databaseValue.toString());
            }
        }

        @Override
        public Integer convertToDatabaseValue(HistoryType entityProperty) {
            if(entityProperty == null) {
                return null;
            } else {
                return entityProperty.getValue();
            }
        }
    }
}
