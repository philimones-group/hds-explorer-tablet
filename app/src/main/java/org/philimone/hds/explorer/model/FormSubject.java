package org.philimone.hds.explorer.model;

public interface FormSubject {

    public long getId();

    public String getTableName();

    public String getValueByName(String fieldName);
}
