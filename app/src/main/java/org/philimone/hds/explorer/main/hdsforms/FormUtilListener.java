package org.philimone.hds.explorer.main.hdsforms;

import java.util.Map;

public interface FormUtilListener<T> {
    void onNewEntityCreated(T entity, Map<String, Object> data);

    void onEntityEdited(T entity, Map<String, Object> data);

    void onFormCancelled();
}
