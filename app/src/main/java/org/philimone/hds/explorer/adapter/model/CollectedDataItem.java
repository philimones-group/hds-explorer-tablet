package org.philimone.hds.explorer.adapter.model;

import org.philimone.hds.explorer.model.CollectedData;
import org.philimone.hds.explorer.model.Form;
import org.philimone.hds.explorer.model.Member;

import java.io.Serializable;

/**
 * Created by paul on 8/10/16.
 */
public class CollectedDataItem implements Serializable {

    private int id;
    private Member member;
    private Form form;
    private CollectedData collectedData;

    public CollectedDataItem(int id, Member member, Form form, CollectedData collectedData) {
        this.id = id;
        this.member = member;
        this.form = form;
        this.collectedData = collectedData;
    }

    public CollectedDataItem(Member member, Form form, CollectedData collectedData) {
        this.member = member;
        this.form = form;
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

    public Form getForm() {
        return form;
    }

    public void setForm(Form form) {
        this.form = form;
    }

    public CollectedData getCollectedData() {
        return collectedData;
    }

    public void setCollectedData(CollectedData collectedData) {
        this.collectedData = collectedData;
    }
}
