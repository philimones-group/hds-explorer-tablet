package org.philimone.hds.explorer.main.hdsforms;

public interface FormUtilListener<T> {
    void onNewEntityCreated(T entity);

    void onEntityEdited(T entity);

    void onFormCancelled();
}
