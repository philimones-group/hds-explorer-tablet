package org.philimone.hds.explorer.model.enums;

import java.io.Serializable;

public interface EnumCode extends Serializable {

    public int getCode();

    public EnumCode fromCode(Integer value);

    public Integer getDefault();

}
