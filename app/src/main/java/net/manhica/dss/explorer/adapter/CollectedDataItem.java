package net.manhica.dss.explorer.adapter;

import android.content.ContentValues;

import net.manhica.dss.explorer.database.DatabaseHelper;
import net.manhica.dss.explorer.database.Table;
import net.manhica.dss.explorer.model.CollectedData;
import net.manhica.dss.explorer.model.Member;

import java.io.Serializable;

/**
 * Created by paul on 8/10/16.
 */
public class CollectedDataItem implements Serializable {

    private int id;
    private Member member;
    private CollectedData collectedData;

    public CollectedDataItem(int id, Member member, CollectedData collectedData) {
        this.id = id;
        this.member = member;
        this.collectedData = collectedData;
    }

    public CollectedDataItem(Member member, CollectedData collectedData) {
        this.member = member;
        this.collectedData = collectedData;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public CollectedData getCollectedData() {
        return collectedData;
    }

    public void setCollectedData(CollectedData collectedData) {
        this.collectedData = collectedData;
    }
}
