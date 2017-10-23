package net.manhica.dss.explorer.listeners;

import com.mapswithme.maps.api.MWMPoint;

import net.manhica.dss.explorer.model.Household;
import net.manhica.dss.explorer.model.Member;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 8/8/16.
 */
public interface MemberActionListener extends Serializable {
    void onMemberSelected(Household household, Member member);

    void onClosestMembersResult(Member member, MWMPoint[] points, MWMPoint[] originalPoints, ArrayList<Member> members);

    void onClosestHouseholdsResult(Household household, MWMPoint[] points, ArrayList<Household> households);
}
