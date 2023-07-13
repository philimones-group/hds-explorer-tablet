package org.philimone.hds.explorer.model;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class CoreFormColumnOptions {

    @Id
    public long id;

    public String formName;

    public String columnName;

    public String optionValue;

    public String optionLabel;

    public String optionLabelCode;

}
