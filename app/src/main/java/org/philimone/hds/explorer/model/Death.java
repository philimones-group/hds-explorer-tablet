package org.philimone.hds.explorer.model;


import java.util.Date;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;
import io.objectbox.annotation.Unique;
import io.objectbox.relation.ToOne;

@Entity
public class Death implements CoreEntity {

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
