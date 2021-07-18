package org.philimone.hds.explorer.model;

import org.philimone.hds.explorer.model.converters.VisitLocationItemConverter;
import org.philimone.hds.explorer.model.enums.VisitLocationItem;

import java.util.Date;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;
import io.objectbox.annotation.Unique;
import io.objectbox.relation.ToOne;

@Entity
public class Visit {

    @Id
    public long id;
    @Unique
    public String code;

    //public ToOne<Household> household;

    @Index
    public String householdCode;

    public Date visitDate;

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

    public boolean recentlyCreated;

    public String recentlyCreatedUri;
}
