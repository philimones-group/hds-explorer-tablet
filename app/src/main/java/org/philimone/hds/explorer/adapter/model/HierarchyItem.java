package org.philimone.hds.explorer.adapter.model;

import org.philimone.hds.explorer.model.ApplicationParam;

public class HierarchyItem {
    private String name;
    private String level;

    public HierarchyItem(String name, String level) {
        this.name = name;
        this.level = level;
    }

    public HierarchyItem(ApplicationParam param) {
        this.name = param.getValue();
        this.level = param.getName();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    @Override
    public boolean equals(Object o) {

        HierarchyItem obj = (HierarchyItem) o;

        return this.level.equalsIgnoreCase(obj.level);
    }

    @Override
    public int hashCode() {
        return 1;
    }
}
