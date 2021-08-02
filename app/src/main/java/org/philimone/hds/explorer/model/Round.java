package org.philimone.hds.explorer.model;

import java.util.Date;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Unique;

@Entity
public class Round {
    @Id
    public long id;

    @Unique
    public int roundNumber;

    public Date startDate;

    public Date endDate;

    public String description;

}
