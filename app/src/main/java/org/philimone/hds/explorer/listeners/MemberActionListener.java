package org.philimone.hds.explorer.listeners;

import com.mapswithme.maps.api.MWMPoint;

import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.widget.member_details.Distance;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by paul on 8/8/16.
 */
public interface MemberActionListener extends Serializable {
    void onMemberSelected(Household household, Member member, Region region);

    void onShowHouseholdClicked(Household household, Member member, Region region);
}
