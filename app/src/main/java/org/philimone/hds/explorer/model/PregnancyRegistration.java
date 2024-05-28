package org.philimone.hds.explorer.model;

import org.philimone.hds.explorer.model.converters.EstimatedDateOfDeliveryTypeConverter;
import org.philimone.hds.explorer.model.converters.PregnancyStatusConverter;
import org.philimone.hds.explorer.model.enums.EstimatedDateOfDeliveryType;
import org.philimone.hds.explorer.model.enums.PregnancyStatus;

import java.util.Date;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;
import io.objectbox.annotation.Unique;
import io.objectbox.relation.ToOne;

@Entity
public class PregnancyRegistration implements CoreEntity {

    @Id
    public long id;

    @Unique
    public String code;

    //public ToOne<Member> mother;

    @Index
    public String motherCode;

    public Date recordedDate;

    public Integer pregMonths;

    public Boolean eddKnown;

    public Boolean hasPrenatalRecord;

    public Date eddDate;

    @Convert(converter = EstimatedDateOfDeliveryTypeConverter.class, dbType = String.class)
    public EstimatedDateOfDeliveryType eddType;

    public Boolean lmpKnown;

    public Date lmpDate;

    public Date expectedDeliveryDate;

    @Convert(converter = PregnancyStatusConverter.class, dbType = String.class)
    public PregnancyStatus status;

    //public ToOne<Visit> visit;

    @Index
    public String visitCode;

    @Unique
    public String collectedId;

    public boolean recentlyCreated;

    public String recentlyCreatedUri;

    @Override
    public long getId() {
        return this.id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String getCollectedId() {
        return collectedId;
    }

    @Override
    public boolean isRecentlyCreated() {
        return this.recentlyCreated;
    }

    @Override
    public String getRecentlyCreatedUri() {
        return this.recentlyCreatedUri;
    }

}
