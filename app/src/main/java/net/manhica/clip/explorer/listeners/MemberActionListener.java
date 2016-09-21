package net.manhica.clip.explorer.listeners;

import net.manhica.clip.explorer.model.Household;
import net.manhica.clip.explorer.model.Member;

/**
 * Created by paul on 8/8/16.
 */
public interface MemberActionListener {
    public void onMemberSelected(Household household, Member member);
}
