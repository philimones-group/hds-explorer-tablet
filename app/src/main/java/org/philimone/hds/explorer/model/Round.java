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

    public static Round getEmptyRound(int roundNumber) {
        Round round = new Round();
        round.roundNumber = roundNumber;
        return round;
    }

}
