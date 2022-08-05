package org.philimone.hds.explorer.model;

import org.philimone.hds.explorer.model.converters.StringCollectionConverter;
import org.philimone.hds.explorer.model.converters.SubjectEntityConverter;
import org.philimone.hds.explorer.model.enums.SubjectEntity;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import io.objectbox.annotation.Backlink;
import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;
import io.objectbox.annotation.Unique;
import io.objectbox.relation.ToMany;

@Entity
public class FormGroupInstance {

    @Id
    public long id;

    @Index
    public String groupFormId;

    public String groupFormName;

    @Index
    @Convert(converter = SubjectEntityConverter.class, dbType = String.class)
    public SubjectEntity subjectEntity;

    @Index
    public String subjectCode;

    @Unique
    public String instanceUuid;

    @Unique
    public String instanceCode;

    @Convert(converter = StringCollectionConverter.class, dbType = String.class)
    public Set<String> formsChilds;

    @Backlink(to = "groupInstance")
    public ToMany<FormGroupInstanceChild> instanceChilds;

    public Date createdDate;

    public FormGroupInstance() {
        formsChilds = new LinkedHashSet<>();
    }
}
