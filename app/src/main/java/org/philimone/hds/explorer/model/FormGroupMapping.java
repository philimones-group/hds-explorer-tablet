package org.philimone.hds.explorer.model;

import org.philimone.hds.explorer.model.converters.FormCollectTypeConverter;
import org.philimone.hds.explorer.model.enums.FormCollectType;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;
import io.objectbox.relation.ToOne;

@Entity
public class FormGroupMapping {

    @Id
    public long id;

    public ToOne<Form> groupForm;

    public int ordinal;

    @Index
    public String formId;

    public boolean formRequired;

    @Convert(converter = FormCollectTypeConverter.class, dbType = String.class)
    public FormCollectType formCollectType;

    public String formCollectCondition;
    public String formCollectLabel;

}
