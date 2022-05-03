package org.philimone.hds.explorer.model;

import org.philimone.hds.explorer.model.converters.HeadRelationshipTypeConverter;
import org.philimone.hds.explorer.model.converters.PregnancyOutcomeTypeConverter;
import org.philimone.hds.explorer.model.enums.HeadRelationshipType;
import org.philimone.hds.explorer.model.enums.PregnancyOutcomeType;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;
import io.objectbox.annotation.Unique;
import io.objectbox.relation.ToOne;

@Entity
public class PregnancyChild implements CoreEntity {

    @Id
    public long id;

    public ToOne<PregnancyOutcome> outcome;

    @Index
    public String outcomeCode;

    @Convert(converter = PregnancyOutcomeTypeConverter.class, dbType = String.class)
    public PregnancyOutcomeType outcomeType;

    //public ToOne<Member> child;

    @Index
    public String childCode;

    public Integer childOrdinalPosition;

    @Convert(converter = HeadRelationshipTypeConverter.class, dbType = String.class)
    public HeadRelationshipType childHeadRelationshipType;

    public ToOne<HeadRelationship> childHeadRelationship;

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
