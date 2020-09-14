package org.philimone.hds.explorer.widget.member_details;

import java.io.Serializable;

public class Distance implements Serializable {
    String label;
    double value;

    public Distance(String label, double value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return label;
    }
}
