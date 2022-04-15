package org.philimone.hds.explorer.model;

import org.philimone.hds.explorer.model.converters.VisitLocationItemConverter;
import org.philimone.hds.explorer.model.converters.VisitReasonConverter;
import org.philimone.hds.explorer.model.enums.VisitLocationItem;
import org.philimone.hds.explorer.model.enums.VisitReason;

import java.util.Date;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;
import io.objectbox.annotation.Unique;
import io.objectbox.relation.ToOne;

@Entity
public class Visit implements CoreEntity {

    @Id
    public long id;
    @Unique
    public String code;

    //public ToOne<Household> household;

    @Index
    public String householdCode;

    public Date visitDate;

    @Convert(converter = VisitReasonConverter.class, dbType = String.class)
    public VisitReason visitReason;

    @Convert(converter = VisitLocationItemConverter.class, dbType = String.class)
    public VisitLocationItem visitLocation;

    public String visitLocationOther;

    public Integer roundNumber;

    //public ToOne<Member> respondent;

    @Index
    public String respondentCode;

    public Boolean hasInterpreter;

    public String interpreterName;

    public Double gpsAccuracy;
    public Double gpsAltitude;
    public Double gpsLatitude;
    public Double gpsLongitude;

    @Unique
    public String collectedId;

    public boolean recentlyCreated;

    public String recentlyCreatedUri;

    @Override
    public long getId() {
        return this.id;
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
