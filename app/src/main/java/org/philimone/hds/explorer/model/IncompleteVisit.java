package org.philimone.hds.explorer.model;

import org.philimone.hds.explorer.model.converters.IncompleteVisitReasonConverter;
import org.philimone.hds.explorer.model.enums.IncompleteVisitReason;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;
import io.objectbox.annotation.Unique;
import io.objectbox.relation.ToOne;

@Entity
public class IncompleteVisit implements CoreEntity {
    @Id
    public long id;

    @Index
    public String visitCode;

    @Index
    public String memberCode;

    @Convert(converter = IncompleteVisitReasonConverter.class, dbType = String.class)
    public IncompleteVisitReason reason;

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
