package org.philimone.hds.explorer.model;

public interface CoreEntity {

    public long getId();

    public boolean isRecentlyCreated();

    public String getRecentlyCreatedUri();
}
