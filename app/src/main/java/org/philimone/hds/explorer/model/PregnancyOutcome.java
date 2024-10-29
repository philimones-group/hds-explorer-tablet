package org.philimone.hds.explorer.model;

import org.philimone.hds.explorer.model.converters.BirthPlaceConverter;
import org.philimone.hds.explorer.model.enums.BirthPlace;

import java.util.Date;

import io.objectbox.annotation.Backlink;
import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;
import io.objectbox.annotation.Unique;
import io.objectbox.relation.ToMany;
import io.objectbox.relation.ToOne;

@Entity
public class PregnancyOutcome implements CoreEntity {

    @Id
    public long id;

    @Unique
    public String code;

    //public ToOne<Member> mother;

    //public ToOne<Member> father;

    @Index
    public String motherCode;

    @Index
    public String fatherCode;

    public Integer numberOfOutcomes;

    public Integer numberOfLivebirths;

    public Date outcomeDate;

    @Convert(converter = BirthPlaceConverter.class, dbType = String.class)
    public BirthPlace birthPlace;

    public String birthPlaceOther;

    @Index
    public String visitCode;

    @Backlink
    public ToMany<PregnancyChild> childs;

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
