package mz.betainteractive.odk.model;

import static mz.betainteractive.odk.model.RepeatGroupType.RESIDENT_MEMBERS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import org.philimone.hds.explorer.R;
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
	private Map<String, List<Map<String, String>>> mapRepeatGroup;

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

    	/*
    	if (value instanceof Map){
    		mapRepeatGroup.put(variable, (Map<String, String>) value);
		}*/
    }

	public void putAll(Map<String, Object> mapValues){
		//values.putAll(mapValues);

		for (String key : mapValues.keySet()){
			String value = mapValues.get(key)+"";

			//Log.d("map", "key="+key+", value="+value);

			if (key.contains(".")){ //mapped repeat inner values ("RepeatGroupName.variable")
				//The repeat inner columns dont go to "values" map but to mapRepeat
				//Log.d("testttt", key+":"+value);
				String[] spt = key.split("\\.");
				String repeatGroupName = spt[0];  //Repeat Group name
				String repeatGroupInnerColumn = spt[1]; //inside variable

				List<Map<String, String>> mapList = mapRepeatGroup.get(repeatGroupName); //map that contains inside_var and its value: Member.code
				mapList = (mapList==null) ? new ArrayList<>() : mapList; //create new if dont exists
				//if list of maps is empty create new map and add, or get the default map
				Map<String, String> map = (mapList.size()==0) ? new LinkedHashMap<>() : mapList.get(0); //default map - will only have one for repeat groups mapped from Members List

				if (mapList.size() == 0){
					mapList.add(map);
					mapRepeatGroup.put(repeatGroupName, mapList);
				}

				map.put(repeatGroupInnerColumn, value); //"innercolumn" - is odk repeat inside variable and "value" is the Subject.columnName

			}else {
				values.put(key, value);
			}
		}

	}

	public void putRepeatObjects(String repeatGroupName, List<Map<String, String>> mapObjectsList){
		values.put(repeatGroupName, RepeatGroupType.MAPPED_VALUES.code);

		mapRepeatGroup.put(repeatGroupName, mapObjectsList);
	}
    
    public Object get(String variable){
    	return values.get(variable);
    }

	public void updateUnknownMember(Context context) {
		for (String key : this.values.keySet()) {
			String value = this.values.get(key).toString();

			value = getParentName(context, value);

			this.values.put(key, value);
		}
	}

	private String getParentName(Context context, String name){
		if (name.equals("Unknown") || name.equals("member.unknown.label")){
			return context.getString(R.string.member_details_unknown_lbl);
		}else {
			return name;
		}
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

	public RepeatGroupType getRepeatGroupType(String variableName){
		if (isRepeatGroup(variableName)){
			String value = (String) values.get(variableName);
			return RepeatGroupType.getFrom(value);
		}

		return null;
	}

	public boolean isMemberRepeatGroup(String variableName){
		RepeatGroupType type = getRepeatGroupType(variableName);
		return type== RESIDENT_MEMBERS || type==RepeatGroupType.DEAD_MEMBERS || type==RepeatGroupType.OUTMIGRATED_MEMBERS ||  type==RepeatGroupType.ALL_MEMBERS;
	}

	public boolean isResidentMemberRepeatGroup(String variableName){
		RepeatGroupType type = getRepeatGroupType(variableName);
		return type== RESIDENT_MEMBERS;
	}

	public boolean isDeadMembersRepeatGroup(String variableName){
		RepeatGroupType type = getRepeatGroupType(variableName);
		return type==RepeatGroupType.DEAD_MEMBERS;
	}

	public boolean isOutMigMembersRepeatGroup(String variableName){
		RepeatGroupType type = getRepeatGroupType(variableName);
		return type==RepeatGroupType.OUTMIGRATED_MEMBERS;
	}

	public boolean isAllMembersRepeatGroup(String variableName){
		RepeatGroupType type = getRepeatGroupType(variableName);
		return type==RepeatGroupType.ALL_MEMBERS;
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

	public List<Map<String, String>> getRepeatGroupMapping(String repeatGroup){
		return this.mapRepeatGroup.get(repeatGroup);
	}

	public int getRepeatGroupCount(String variableName){
		final String value = values.get(variableName)+"";
		RepeatGroupType type = RepeatGroupType.getFrom(value);

		switch (type) {
			case RESIDENT_MEMBERS : return getResidentMembers().size();
			case DEAD_MEMBERS : return getDeadMembers().size();
			case OUTMIGRATED_MEMBERS : return getOutmigMembers().size();
			case ALL_MEMBERS : return getHouseholdMembers().size();
			case MAPPED_VALUES: return mapRepeatGroup.get(variableName).size();
			default: return 0;
		}
	}
}
