package org.philimone.hds.explorer.model;

import org.philimone.hds.explorer.model.enums.SubjectEntity;

public interface FormSubject {

    public long getId();

    public String getCode();

    public SubjectEntity getTableName();

    public String getValueByName(String fieldName);
}
