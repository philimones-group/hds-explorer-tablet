package org.philimone.hds.explorer.listeners;

import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Region;

import java.io.Serializable;

/**
 * Created by paul on 8/8/16.
 */
public interface MemberActionListener extends Serializable {
    void onMemberSelected(Household household, Member member, Region region);

    void onShowHouseholdClicked(Household household, Member member, Region region);
}
