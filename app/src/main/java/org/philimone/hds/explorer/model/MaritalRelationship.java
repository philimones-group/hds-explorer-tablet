package org.philimone.hds.explorer.model;

import org.philimone.hds.explorer.model.converters.MaritalEndStatusConverter;
import org.philimone.hds.explorer.model.converters.MaritalStartStatusConverter;
import org.philimone.hds.explorer.model.enums.MaritalEndStatus;
import org.philimone.hds.explorer.model.enums.MaritalStartStatus;

import java.util.Date;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;
import io.objectbox.relation.ToOne;

@Entity
public class MaritalRelationship {

    @Id
    public long id;

    //public ToOne<Member> memberA;

    //public ToOne<Member> memberB;

    @Index
    public String memberA_code;

    @Index
    public String memberB_code;

    @Convert(converter = MaritalStartStatusConverter.class, dbType = String.class)
    public MaritalStartStatus startStatus;

    public Date startDate;

    @Convert(converter = MaritalEndStatusConverter.class, dbType = String.class)
    public MaritalEndStatus endStatus;

    public Date endDate;

    public boolean recentlyCreated;

}
