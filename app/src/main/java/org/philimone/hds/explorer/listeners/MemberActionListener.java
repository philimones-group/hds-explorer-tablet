package org.philimone.hds.explorer.listeners;

import com.mapswithme.maps.api.MWMPoint;

import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Member;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by paul on 8/8/16.
 */
public interface MemberActionListener extends Serializable {
    void onMemberSelected(Household household, Member member);

    void onMemberHouseholdSelected(Household household, Member member);

    void onClosestMembersResult(Member member, MWMPoint[] points, MWMPoint[] originalPoints, ArrayList<Member> members);

    void onClosestHouseholdsResult(Household household, MWMPoint[] points, ArrayList<Household> households);
}
