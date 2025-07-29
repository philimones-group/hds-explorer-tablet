package org.philimone.hds.explorer.model;

import org.philimone.hds.explorer.model.converters.EstimatedDateOfDeliveryTypeConverter;
import org.philimone.hds.explorer.model.converters.PregnancyStatusConverter;
import org.philimone.hds.explorer.model.converters.PregnancyVisitTypeConverter;
import org.philimone.hds.explorer.model.enums.EstimatedDateOfDeliveryType;
import org.philimone.hds.explorer.model.enums.PregnancyStatus;
import org.philimone.hds.explorer.model.enums.PregnancyVisitType;

import java.time.LocalDate;
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

    public Integer   summary_antepartum_count;
    public Integer   summary_postpartum_count;

    @Convert(converter = PregnancyStatusConverter.class, dbType = String.class)
    public PregnancyStatus summary_last_visit_status;

    @Convert(converter = PregnancyVisitTypeConverter.class, dbType = String.class)
    public PregnancyVisitType summary_last_visit_type;

    public Date summary_last_visit_date;
    public Date summary_first_visit_date;
    public Boolean   summary_has_pregnancy_outcome;
    public Integer   summary_nr_outcomes;
    public Boolean   summary_followup_completed;

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
