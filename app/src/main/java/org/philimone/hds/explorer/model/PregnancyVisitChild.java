package org.philimone.hds.explorer.model;

import org.philimone.hds.explorer.model.converters.BreastFeedingStatusConverter;
import org.philimone.hds.explorer.model.converters.HeadRelationshipTypeConverter;
import org.philimone.hds.explorer.model.converters.IllnessSymptomsCollectionConverter;
import org.philimone.hds.explorer.model.converters.IllnessSymptomsConverter;
import org.philimone.hds.explorer.model.converters.ImmunizationStatusConverter;
import org.philimone.hds.explorer.model.converters.NewBornStatusConverter;
import org.philimone.hds.explorer.model.converters.PregnancyOutcomeTypeConverter;
import org.philimone.hds.explorer.model.enums.BreastFeedingStatus;
import org.philimone.hds.explorer.model.enums.HeadRelationshipType;
import org.philimone.hds.explorer.model.enums.IllnessSymptoms;
import org.philimone.hds.explorer.model.enums.ImmunizationStatus;
import org.philimone.hds.explorer.model.enums.NewBornStatus;
import org.philimone.hds.explorer.model.enums.PregnancyOutcomeType;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;
import io.objectbox.annotation.Unique;
import io.objectbox.relation.ToOne;

@Entity
public class PregnancyVisitChild implements CoreEntity {

    @Id
    public long id;

    public ToOne<PregnancyVisit> visit;

    @Index
    public String pregnancyCode;

    @Convert(converter = PregnancyOutcomeTypeConverter.class, dbType = String.class)
    public PregnancyOutcomeType outcomeType;

    @Index
    public String childCode;

    @Convert(converter = NewBornStatusConverter.class, dbType = String.class)
    public NewBornStatus childStatus;
    public Double childWeight;

    @Convert(converter = IllnessSymptomsCollectionConverter.class, dbType = String.class)
    public Set<IllnessSymptoms> childIllnessSymptoms;

    @Convert(converter = BreastFeedingStatusConverter.class, dbType = String.class)
    public BreastFeedingStatus childBreastfeedingStatus;

    @Convert(converter = ImmunizationStatusConverter.class, dbType = String.class)
    public ImmunizationStatus childImmunizationStatus;
    public String notes;

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

    public PregnancyVisitChild() {
        this.childIllnessSymptoms = new HashSet<>();
    }
}
