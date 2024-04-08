package org.philimone.hds.explorer.model;

import org.philimone.hds.explorer.model.converters.NoVisitReasonConverter;
import org.philimone.hds.explorer.model.converters.StringCollectionConverter;
import org.philimone.hds.explorer.model.converters.VisitLocationItemConverter;
import org.philimone.hds.explorer.model.converters.VisitReasonConverter;
import org.philimone.hds.explorer.model.enums.NoVisitReason;
import org.philimone.hds.explorer.model.enums.SubjectEntity;
import org.philimone.hds.explorer.model.enums.VisitLocationItem;
import org.philimone.hds.explorer.model.enums.VisitReason;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;
import io.objectbox.annotation.Unique;
import io.objectbox.relation.ToOne;
import mz.betainteractive.utilities.ReflectionUtils;

@Entity
public class Visit implements CoreEntity, FormSubject {

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

    public Boolean visitPossible;

    @Convert(converter = NoVisitReasonConverter.class, dbType = String.class)
    public NoVisitReason visitNotPossibleReason;

    @Convert(converter = VisitLocationItemConverter.class, dbType = String.class)
    public VisitLocationItem visitLocation;

    public String visitLocationOther;

    public Integer roundNumber;

    //public ToOne<Member> respondent;
    public Boolean respondentResident;
    public String respondentRelationship;

    @Index
    public String respondentCode;
    public String respondentName;

    public Boolean hasInterpreter;

    public String interpreterName;

    public Double gpsAccuracy;
    public Double gpsAltitude;
    public Double gpsLatitude;
    public Double gpsLongitude;

    @Convert(converter = StringCollectionConverter.class, dbType = String.class)
    public Set<String> nonVisitedMembers;

    @Unique
    public String collectedId;

    public boolean recentlyCreated;

    public String recentlyCreatedUri;

    public Visit(){
        this.nonVisitedMembers = new LinkedHashSet<>();
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public SubjectEntity getTableName() {
        return SubjectEntity.VISIT;
    }

    @Override
    public String getValueByName(String fieldName) {
        return ReflectionUtils.getValueByName(this, fieldName);
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
