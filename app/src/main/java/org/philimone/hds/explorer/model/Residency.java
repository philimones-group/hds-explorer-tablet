package org.philimone.hds.explorer.model;

import org.philimone.hds.explorer.model.converters.ResidencyEndTypeConverter;
import org.philimone.hds.explorer.model.converters.ResidencyStartTypeConverter;
import org.philimone.hds.explorer.model.enums.temporal.ResidencyEndType;
import org.philimone.hds.explorer.model.enums.temporal.ResidencyStartType;

import java.util.Date;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;
import io.objectbox.relation.ToOne;

/**
 * Residency stores the information about a Member/Individual thats lives
 */
@Entity
public class Residency {

    @Id
    public long id;

    //public ToOne<Household> household;

    //public ToOne<Member> member;

    @Index
    public String householdCode;

    @Index
    public String memberCode;

    @Index
    @Convert(converter = ResidencyStartTypeConverter.class, dbType = String.class)
    public ResidencyStartType startType;

    public Date startDate;

    @Index
    @Convert(converter = ResidencyEndTypeConverter.class, dbType = String.class)
    public ResidencyEndType endType;

    public Date endDate;

}
