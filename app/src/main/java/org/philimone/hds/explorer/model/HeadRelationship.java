package org.philimone.hds.explorer.model;

import org.philimone.hds.explorer.model.converters.HeadRelationshipEndTypeConverter;
import org.philimone.hds.explorer.model.converters.HeadRelationshipStartTypeConverter;
import org.philimone.hds.explorer.model.converters.HeadRelationshipTypeConverter;
import org.philimone.hds.explorer.model.enums.HeadRelationshipType;
import org.philimone.hds.explorer.model.enums.temporal.HeadRelationshipEndType;
import org.philimone.hds.explorer.model.enums.temporal.HeadRelationshipStartType;

import java.util.Date;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;
import io.objectbox.annotation.Unique;
import io.objectbox.relation.ToOne;

@Entity
public class HeadRelationship implements CoreEntity {

    @Id
    public long id;

    //public ToOne<Household> household;

    //public ToOne<Member> member;

    //public ToOne<Member> head;

    @Index
    public String householdCode;
    @Index
    public String memberCode;
    @Index
    public String headCode;

    @Index
    @Convert(converter = HeadRelationshipTypeConverter.class, dbType = String.class)
    public HeadRelationshipType relationshipType;

    @Index
    @Convert(converter = HeadRelationshipStartTypeConverter.class, dbType = String.class)
    public HeadRelationshipStartType startType;

    public Date startDate;

    @Index
    @Convert(converter = HeadRelationshipEndTypeConverter.class, dbType = String.class)
    public HeadRelationshipEndType endType;

    public Date endDate;

    public boolean recentlyCreated;

    public String recentlyCreatedUri;

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public String getCollectedId() {
        return "NA";
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
