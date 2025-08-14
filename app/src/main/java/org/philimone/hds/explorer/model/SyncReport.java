package org.philimone.hds.explorer.model;

import org.philimone.hds.explorer.model.converters.SyncEntityConverter;
import org.philimone.hds.explorer.model.converters.SyncStatusConverter;
import org.philimone.hds.explorer.model.enums.SyncEntity;
import org.philimone.hds.explorer.model.enums.SyncStatus;

import java.io.Serializable;
import java.util.Date;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Unique;
import mz.betainteractive.utilities.DateUtil;
import mz.betainteractive.utilities.StringUtil;

/**
 * Created by paul on 5/21/16.
 */
@Entity
public class SyncReport implements Serializable {

    @Id
    public long id;

    @Unique
    @Convert(converter = SyncEntityConverter.class, dbType = Integer.class)
    public SyncEntity reportId;

    public Date date;

    @Convert(converter = SyncStatusConverter.class, dbType = Integer.class)
    public SyncStatus status;

    public String description;

    public SyncReport(){

    }

    public SyncReport(SyncEntity reportId, Date date, SyncStatus status, String description) {
        this.reportId = reportId;
        this.date = date;
        this.status = status;
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public SyncEntity getReportId() {
        return reportId;
    }

    public void setReportId(SyncEntity reportId) {
        this.reportId = reportId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setDate(String date) {
        if (date != null && !date.isEmpty()){
            this.date = DateUtil.toDateYMDHMS(date);
            return;
        }

        this.date = null;
    }

    public SyncStatus getStatus() {
        return status;
    }

    public void setStatus(SyncStatus status) {
        this.status = status;
    }

}
