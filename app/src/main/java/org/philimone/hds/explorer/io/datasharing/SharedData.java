package org.philimone.hds.explorer.io.datasharing;

import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Region;

import java.util.ArrayList;
import java.util.List;

public class SharedData {
    private List<Region> regions = new ArrayList<>();

    private List<Household> households = new ArrayList<>();

    private List<Member> members = new ArrayList<>();

    public SharedData(List<Region> regions, List<Household> households, List<Member> members) {
        this.regions.addAll(regions);
        this.households.addAll(households);
        this.members.addAll(members);
    }

    public List<Region> getRegions() {
        return regions;
    }

    public List<Household> getHouseholds() {
        return households;
    }

    public List<Member> getMembers() {
        return members;
    }

    public int countSharedData() {
        return this.households.size() + this.regions.size() + this.members.size();
    }
}
