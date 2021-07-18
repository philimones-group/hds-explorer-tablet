package org.philimone.hds.explorer.model;


import java.util.Date;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;
import io.objectbox.annotation.Unique;
import io.objectbox.relation.ToOne;

@Entity
public class Death {

    @Id
    public long id;

    //public ToOne<Member> member;

    @Unique
    public String memberCode;

    public Date deathDate;

    public Integer ageAtDeath;

    public String deathCause;

    public String deathPlace;

    //public ToOne<Visit> visit;

    @Index
    public String visitCode;

    public boolean recentlyCreated;

    public String recentlyCreatedUri;

}
