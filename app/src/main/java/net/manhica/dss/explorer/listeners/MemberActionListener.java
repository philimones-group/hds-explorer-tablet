package net.manhica.dss.explorer.listeners;

import net.manhica.dss.explorer.model.Household;
import net.manhica.dss.explorer.model.Member;

/**
 * Created by paul on 8/8/16.
 */
public interface MemberActionListener {
    public void onMemberSelected(Household household, Member member);
}
