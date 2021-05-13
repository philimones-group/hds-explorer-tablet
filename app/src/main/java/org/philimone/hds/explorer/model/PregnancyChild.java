package org.philimone.hds.explorer.model;

import org.philimone.hds.explorer.model.converters.HeadRelationshipTypeConverter;
import org.philimone.hds.explorer.model.converters.PregnancyOutcomeTypeConverter;
import org.philimone.hds.explorer.model.enums.PregnancyOutcomeType;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;
import io.objectbox.relation.ToOne;

@Entity
public class PregnancyChild {

    @Id
    public long id;

    public ToOne<PregnancyOutcome> outcome;

    @Index
    public String outcomeCode;

    @Convert(converter = PregnancyOutcomeTypeConverter.class, dbType = String.class)
    public PregnancyOutcomeType outcomeType;

    public ToOne<Member> child;

    @Index
    public String childCode;

    public Integer childOrdinalPosition;

    public ToOne<HeadRelationship> childHeadRelationship;

}
