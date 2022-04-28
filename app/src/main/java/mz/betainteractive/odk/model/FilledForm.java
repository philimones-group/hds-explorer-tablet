package mz.betainteractive.odk.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.util.Log;

import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.enums.temporal.ResidencyEndType;

/**
 * A filled form represents an ODK form that has been prefilled with values from
 * the OpenHDS application.
 */
public class FilledForm {

	public static final String TYPE_RESIDENT_MEMBERS = "HouseholdResidentMembers";
	public static final String TYPE_DEAD_MEMBERS = "HouseholdDeadMembers";
	public static final String TYPE_OUTMIGRATED_MEMBERS = "HouseholdExtMembers";
	public static final String TYPE_ALL_MEMBERS = "HouseholdAllMembers";


    private String formName;
	private String formVersion;
    private Map<String,Object> values;
    private List<Member> householdMembers; /* Only has values if we selected an Household or a Member */
	private List<Member> residentMembers;
	private List<Member> deadMembers;
	private List<Member> outmigMembers;
	private Map<String, Map<String, String>> mapRepeatGroup;

	public FilledForm(String formName) {
        this.formName = formName;
        this.values = new HashMap<String, Object>();
        this.mapRepeatGroup = new HashMap<>();
        initMembers();
    }

	public FilledForm(String formName, String formVersion) {
		this.formName = formName;
		this.formVersion = formVersion;
		this.values = new HashMap<String, Object>();
		initMembers();
	}

	private void initMembers(){
		this.householdMembers = new ArrayList<>();
		this.residentMembers = new ArrayList<>();
		this.deadMembers = new ArrayList<>();
		this.outmigMembers = new ArrayList<>();
	}

    public void put(String variable, Object value){
    	values.put(variable, value);

    	if (value instanceof Map){
    		mapRepeatGroup.put(variable, (Map<String, String>) value);
		}
    }

	public void putAll(Map<String, Object> mapValues){
		//values.putAll(mapValues);

		for (String key : mapValues.keySet()){
			String value = mapValues.get(key)+"";

			if (key.contains(".")){
				Log.d("testttt", key+":"+value);
				String[] spt = key.split("\\.");
				key = spt[0];  //Repeat Group name
				String var = spt[1]; //inside variable

				Map<String, String> map = mapRepeatGroup.get(key); //map that contains inside_var and its value: Member.code
				if (map == null){
					map = new LinkedHashMap<>();
					mapRepeatGroup.put(key, map);
				}
				map.put(var, value); //"var" - is odk repeat inside variable and "value" is the Subject.columnName


			}else {
				values.put(key, value);
			}
		}

	}
    
    public Object get(String variable){
    	return values.get(variable);
    }

	public String getFormName() {
		return formName;
	}

	public void setFormName(String formName) {
		this.formName = formName;
	}

	public String getFormVersion() {
		return formVersion;
	}

	public void setFormVersion(String formVersion) {
		this.formVersion = formVersion;
	}

	public List<String> getVariables(){
		return new ArrayList<String>(this.values.keySet()); 
	}

	public void setValues(ContentValues contentValues) {
		for (String key : contentValues.keySet()){
			String value = contentValues.getAsString(key);
			values.put(key, value);
		}
	}

	public Map<String, Object> getValues() {
		return values;
	}

	public void setHouseholdMembers(List<Member> householdMembers){
		this.householdMembers.addAll(householdMembers);

		for (Member m : householdMembers){
			if (m.getEndType() == ResidencyEndType.NOT_APPLICABLE || m.getEndType()==null){
				residentMembers.add(m);
			} else if (m.getEndType() == ResidencyEndType.DEATH){
				deadMembers.add(m);
			} else if (m.getEndType() == ResidencyEndType.EXTERNAL_OUTMIGRATION){ //these setting must be checked and HDS-Explorer should have its types
				outmigMembers.add(m);
			}
		}
	}

	/*Repeat Groups*/
	public boolean isRepeatGroup(String variableName) {
		return mapRepeatGroup.containsKey(variableName);
	}

	public boolean isMemberRepeatGroup(String variableName){
		String value = values.get(variableName)+"";
		return TYPE_RESIDENT_MEMBERS.equals(value) || TYPE_DEAD_MEMBERS.equals(value) || TYPE_OUTMIGRATED_MEMBERS.equals(value) ||  TYPE_ALL_MEMBERS.equals(value);
	}

	public boolean isResidentMemberRepeatGroup(String variableName){
		String value = values.get(variableName)+"";
		return TYPE_RESIDENT_MEMBERS.equals(value);
	}

	public boolean isDeadMembersRepeatGroup(String variableName){
		String value = values.get(variableName)+"";
		return TYPE_DEAD_MEMBERS.equals(value);
	}

	public boolean isOutMigMembersRepeatGroup(String variableName){
		String value = values.get(variableName)+"";
		return TYPE_OUTMIGRATED_MEMBERS.equals(value);
	}

	public boolean isAllMembersRepeatGroup(String variableName){
		String value = values.get(variableName)+"";
		return TYPE_ALL_MEMBERS.equals(value);
	}

	public List<Member> getHouseholdMembers(){
		return this.householdMembers;
	}

	public List<Member> getResidentMembers() {
		return residentMembers;
	}

	public List<Member> getDeadMembers() {
		return deadMembers;
	}

	public List<Member> getOutmigMembers() {
		return outmigMembers;
	}

	public Map<String, String> getRepeatGroupMapping(String repeatGroup){
		return this.mapRepeatGroup.get(repeatGroup);
	}

	public int getMembersCount(String variableName){
		final String value = values.get(variableName)+"";

		switch (value) {
			case TYPE_RESIDENT_MEMBERS : return getResidentMembers().size();
			case TYPE_DEAD_MEMBERS : return getDeadMembers().size();
			case TYPE_OUTMIGRATED_MEMBERS : return getOutmigMembers().size();
			case TYPE_ALL_MEMBERS : return getHouseholdMembers().size();
		}

		return 0;
	}
}
