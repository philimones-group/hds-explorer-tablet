package org.philimone.hds.explorer.model;

import org.philimone.hds.explorer.model.converters.ExternalInMigrationTypeConverter;
import org.philimone.hds.explorer.model.converters.InMigrationTypeConverter;
import org.philimone.hds.explorer.model.enums.temporal.ExternalInMigrationType;
import org.philimone.hds.explorer.model.enums.temporal.InMigrationType;

import java.util.Date;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;
import io.objectbox.annotation.Unique;
import io.objectbox.relation.ToOne;

@Entity
public class Inmigration implements CoreEntity {

    @Id
    public long id;

    //public ToOne<Member> member;

    @Index
    public String memberCode;

    @Convert(converter = InMigrationTypeConverter.class, dbType = String.class)
    public InMigrationType type;
    //public ToOne<Household> origin;

    @Convert(converter = ExternalInMigrationTypeConverter.class, dbType = String.class)
    public ExternalInMigrationType extMigType;

    @Index
    public String originCode;

    public String originOther;
    //public ToOne<Household> destination;
    @Index
    public String destinationCode;

    //public ToOne<Residency> destinationResidency;

    public Date migrationDate;
    public String migrationReason;

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
