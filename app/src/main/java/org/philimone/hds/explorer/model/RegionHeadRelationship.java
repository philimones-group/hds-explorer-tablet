package org.philimone.hds.explorer.model;

import org.philimone.hds.explorer.model.converters.RegionHeadEndTypeConverter;
import org.philimone.hds.explorer.model.converters.RegionHeadStartTypeConverter;
import org.philimone.hds.explorer.model.enums.temporal.RegionHeadEndType;
import org.philimone.hds.explorer.model.enums.temporal.RegionHeadStartType;

import java.util.Date;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;
import io.objectbox.annotation.Unique;

@Entity
public class RegionHeadRelationship implements CoreEntity {

    @Id
    public long id;

    //public ToOne<Region> region;

    //public ToOne<Member> head;

    @Index
    public String regionCode;
    @Index
    public String headCode;
    @Index
    public long oldHeadId;
    public long oldHeadRelationshipId;

    @Index
    @Convert(converter = RegionHeadStartTypeConverter.class, dbType = String.class)
    public RegionHeadStartType startType;

    @Index
    public Date startDate;

    @Index
    @Convert(converter = RegionHeadEndTypeConverter.class, dbType = String.class)
    public RegionHeadEndType endType;

    @Index
    public Date endDate;

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
        return this.collectedId;
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
