package org.philimone.hds.explorer.model;

import org.philimone.hds.explorer.model.converters.HouseholdInstitutionTypeConverter;
import org.philimone.hds.explorer.model.converters.ProxyHeadChangeReasonConverter;
import org.philimone.hds.explorer.model.converters.ProxyHeadRoleConverter;
import org.philimone.hds.explorer.model.converters.ProxyHeadTypeConverter;
import org.philimone.hds.explorer.model.enums.ProxyHeadChangeReason;
import org.philimone.hds.explorer.model.enums.ProxyHeadRole;
import org.philimone.hds.explorer.model.enums.ProxyHeadType;
import org.philimone.hds.explorer.model.enums.SubjectEntity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;
import io.objectbox.annotation.Unique;

@Entity
public class HouseholdProxyHead implements CoreEntity, Serializable {

    @Id
    public long id;
    @Index
    public String visitCode;
    @Index
    public String householdCode;
    @Index
    @Convert(converter = ProxyHeadTypeConverter.class, dbType = String.class)
    public ProxyHeadType proxyHeadType;
    @Index
    public String proxyHeadCode;
    public String proxyHeadName;
    @Index
    @Convert(converter = ProxyHeadRoleConverter.class, dbType = String.class)
    public ProxyHeadRole proxyHeadRole;

    @Index
    public Date startDate;
    @Index
    public Date endDate;

    @Convert(converter = ProxyHeadChangeReasonConverter.class, dbType = String.class)
    public ProxyHeadChangeReason reason;
    public String reasonOther;

    @Unique
    public String collectedId;
    public boolean recentlyCreated = false;
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
