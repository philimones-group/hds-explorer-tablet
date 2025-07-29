package org.philimone.hds.explorer.model;

import org.philimone.hds.explorer.model.converters.BirthPlaceConverter;
import org.philimone.hds.explorer.model.converters.BreastFeedingStatusConverter;
import org.philimone.hds.explorer.model.converters.HealthcareProviderTypeConverter;
import org.philimone.hds.explorer.model.converters.PregnancyStatusConverter;
import org.philimone.hds.explorer.model.converters.PregnancyVisitTypeConverter;
import org.philimone.hds.explorer.model.enums.BirthPlace;
import org.philimone.hds.explorer.model.enums.BreastFeedingStatus;
import org.philimone.hds.explorer.model.enums.HealthcareProviderType;
import org.philimone.hds.explorer.model.enums.PregnancyStatus;
import org.philimone.hds.explorer.model.enums.PregnancyVisitType;

import java.time.LocalDate;
import java.util.Date;

import io.objectbox.annotation.Backlink;
import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;
import io.objectbox.annotation.Unique;
import io.objectbox.relation.ToMany;

@Entity
public class PregnancyVisit implements CoreEntity {

    @Id
    public long id;

    @Index
    public String code;

    @Index
    public String visitCode;
    @Index
    public String motherCode;

    @Convert(converter = PregnancyStatusConverter.class, dbType = String.class)
    public PregnancyStatus status;
    public Integer visitNumber;
    
    @Convert(converter = PregnancyVisitTypeConverter.class, dbType = String.class)
    public PregnancyVisitType visitType;
    public Date visitDate;

    /* Antepartum questions */
    public Integer weeksGestation;
    public Boolean prenatalCareReceived;

    @Convert(converter = HealthcareProviderTypeConverter.class, dbType = String.class)
    public HealthcareProviderType prenatalCareProvider;
    public Boolean complicationsReported; 
    public String complicationDetails; 
    public Boolean hasBirthPlan; 
    @Convert(converter = BirthPlaceConverter.class, dbType = String.class)
    public BirthPlace expectedBirthPlace; 
    public String birthPlaceOther; 
    public Boolean transportationPlan; 
    public Boolean financialPreparedness; 

    /* Postpartum questions */
    public Boolean postpartumComplications; 
    public String postpartumComplicationDetails;

    @Convert(converter = BreastFeedingStatusConverter.class, dbType = String.class)
    public BreastFeedingStatus breastfeedingStatus;  
    public Boolean resumedDailyActivities; 
    public Boolean attendedPostpartumCheckup; 


    @Backlink
    public ToMany<PregnancyVisitChild> childs;

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
