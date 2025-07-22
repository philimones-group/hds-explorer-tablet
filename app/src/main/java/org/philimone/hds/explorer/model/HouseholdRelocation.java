package org.philimone.hds.explorer.model;

import org.philimone.hds.explorer.model.converters.HouseholdRelocationReasonConverter;
import org.philimone.hds.explorer.model.converters.RegionHeadEndTypeConverter;
import org.philimone.hds.explorer.model.converters.RegionHeadStartTypeConverter;
import org.philimone.hds.explorer.model.enums.HouseholdRelocationReason;
import org.philimone.hds.explorer.model.enums.temporal.RegionHeadEndType;
import org.philimone.hds.explorer.model.enums.temporal.RegionHeadStartType;

import java.util.Date;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;
import io.objectbox.annotation.Unique;

@Entity
public class HouseholdRelocation implements CoreEntity {

    @Id
    public long id;

    //public ToOne<Region> region;

    //public ToOne<Member> head;

    @Index
    public String visitCode;

    @Index
    public String originCode;
    @Index
    public String destinationCode;
    @Index
    public String headCode;

    public String headName;

    public Date eventDate;

    @Index
    @Convert(converter = HouseholdRelocationReasonConverter.class, dbType = String.class)
    public HouseholdRelocationReason reason;

    public String reasonOther;

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
