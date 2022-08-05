package org.philimone.hds.explorer.model;

import java.util.Date;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;
import io.objectbox.relation.ToOne;

@Entity
public class FormGroupInstanceChild {
    @Id
    public long id;

    public ToOne<FormGroupInstance> groupInstance;

    @Index
    public String formId;

    public String formInstanceUri;

    public boolean collected;

    public Date collectedDate;

    public boolean uploaded;

    public Date uploadedDate;
}
