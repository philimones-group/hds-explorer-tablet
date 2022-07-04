package org.philimone.hds.explorer.model.oldstate;

import org.philimone.hds.explorer.model.converters.CoreFormEntityConverter;
import org.philimone.hds.explorer.model.enums.CoreFormEntity;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Unique;

@Entity
public class SavedEntityState {

    @Id
    public long id;

    @Convert(dbType = String.class, converter = CoreFormEntityConverter.class)
    public CoreFormEntity formEntity;

    public long collectedId;

    public String objectKey;

    public String objectGsonValue;

    public SavedEntityState() {
    }

    public SavedEntityState(CoreFormEntity coreFormEntity, long collectedId, String objectKey, String objectGsonValue) {
        this.formEntity = coreFormEntity;
        this.collectedId = collectedId;
        this.objectKey = objectKey;
        this.objectGsonValue = objectGsonValue;
    }
}
