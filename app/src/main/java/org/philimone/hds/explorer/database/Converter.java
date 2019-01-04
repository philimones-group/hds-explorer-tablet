package org.philimone.hds.explorer.database;

import android.database.Cursor;

import org.philimone.hds.explorer.model.ApplicationParam;
import org.philimone.hds.explorer.model.CollectedData;
import org.philimone.hds.explorer.model.Form;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Module;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.SyncReport;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.model.followup.TrackingList;
import org.philimone.hds.explorer.model.followup.TrackingMemberList;


public class Converter {

	public static ApplicationParam cursorToApplicationParam(Cursor cursor) {
		ApplicationParam param = new ApplicationParam();

		param.setId(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.ApplicationParam._ID)));
		param.setName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.ApplicationParam.COLUMN_NAME)));
		param.setType(cursor.getString(cursor.getColumnIndex(DatabaseHelper.ApplicationParam.COLUMN_TYPE)));
		param.setValue(cursor.getString(cursor.getColumnIndex(DatabaseHelper.ApplicationParam.COLUMN_VALUE)));

		return param;
	}

	public static SyncReport cursorToSyncReport(Cursor cursor){
		SyncReport syncReport = new SyncReport();

		syncReport.setReportId(Integer.parseInt(cursor.getString(cursor.getColumnIndex(DatabaseHelper.SyncReport.COLUMN_REPORT_ID))));
		syncReport.setDescription(cursor.getString(cursor.getColumnIndex(DatabaseHelper.SyncReport.COLUMN_DESCRIPTION)));
		syncReport.setDate(cursor.getString(cursor.getColumnIndex(DatabaseHelper.SyncReport.COLUMN_DATE)));
		syncReport.setStatus(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.SyncReport.COLUMN_STATUS)));

		return syncReport;
	}

	public static User cursorToUser(Cursor cursor){
		User user = new User();
				
		user.setFirstName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.User.COLUMN_FIRSTNAME)));
		user.setLastName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.User.COLUMN_LASTNAME)));
		user.setFullName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.User.COLUMN_FULLNAME)));
		user.setUsername(cursor.getString(cursor.getColumnIndex(DatabaseHelper.User.COLUMN_USERNAME)));
		user.setPassword(cursor.getString(cursor.getColumnIndex(DatabaseHelper.User.COLUMN_PASSWORD)));
		user.setModules(cursor.getString(cursor.getColumnIndex(DatabaseHelper.User.COLUMN_MODULES)));
		user.setEmail(cursor.getString(cursor.getColumnIndex(DatabaseHelper.User.COLUMN_EMAIL)));
		
		return user;
	}
	
	public static Form cursorToForm(Cursor cursor){
		Form form = new Form();

        form.setFormId(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Form.COLUMN_FORM_ID)));
        form.setFormName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Form.COLUMN_FORM_NAME)));
        form.setFormDescription(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Form.COLUMN_FORM_DESCRIPTION)));
		form.setFormDependencies(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Form.COLUMN_FORM_DEPENDENCIES)));
        form.setGender(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Form.COLUMN_GENDER)));
        form.setMinAge(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.Form.COLUMN_MIN_AGE)));
        form.setMaxAge(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.Form.COLUMN_MAX_AGE)));
        form.setModules(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Form.COLUMN_MODULES)));
		form.setHouseholdForm(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.Form.COLUMN_IS_HOUSEHOLD))==1);
		form.setHouseholdHeadForm(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.Form.COLUMN_IS_HOUSEHOLD_HEAD))==1);
		form.setMemberForm(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.Form.COLUMN_IS_MEMBER))==1);
		form.setFollowUpOnly(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.Form.COLUMN_IS_FOLLOW_UP_ONLY))==1);
		form.setFormMap(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Form.COLUMN_FORM_MAP)));
		form.setRedcapApi(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Form.COLUMN_REDCAP_API)));
		form.setRedcapMap(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Form.COLUMN_REDCAP_MAP)));
		return form;
	}

	public static Module cursorToModule(Cursor cursor){
		Module module = new Module();

		module.setCode(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Module.COLUMN_CODE)));
		module.setName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Module.COLUMN_NAME)));
		module.setDescription(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Module.COLUMN_DESCRIPTION)));

		return module;
	}

	public static Region cursorToRegion(Cursor cursor){
		Region region = new Region();

		region.setId(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.Region._ID)));
		region.setCode(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Region.COLUMN_CODE)));
		region.setName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Region.COLUMN_NAME)));
		region.setLevel(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Region.COLUMN_LEVEL)));
		region.setParent(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Region.COLUMN_PARENT)));

		return region;
	}

	public static Household cursorToHousehold(Cursor cursor){
		Household hh = new Household();

		hh.setId(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.Household._ID)));
		hh.setCode(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_CODE)));
		hh.setName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_NAME)));
		hh.setHeadCode(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_HEAD_CODE)));
		hh.setHeadName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_HEAD_NAME)));
		hh.setSecHeadCode(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_SECHEAD_CODE)));
		hh.setRegion(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_REGION)));
		hh.setHierarchy1(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_HIERARCHY_1)));
		hh.setHierarchy2(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_HIERARCHY_2)));
		hh.setHierarchy3(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_HIERARCHY_3)));
		hh.setHierarchy4(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_HIERARCHY_4)));
		hh.setHierarchy5(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_HIERARCHY_5)));
		hh.setHierarchy6(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_HIERARCHY_6)));
		hh.setHierarchy7(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_HIERARCHY_7)));
		hh.setHierarchy8(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_HIERARCHY_8)));
		hh.setGpsNull(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_GPS_NULL))==1);
		hh.setGpsAccuracy(cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_GPS_ACCURACY)));
		hh.setGpsAltitude(cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_GPS_ALTITUDE)));
		hh.setGpsLatitude(cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_GPS_LATITUDE)));
		hh.setGpsLongitude(cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_GPS_LONGITUDE)));
		hh.setCosLatitude(cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_COS_LATITUDE)));
		hh.setSinLatitude(cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_SIN_LATITUDE)));
		hh.setCosLongitude(cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_COS_LONGITUDE)));
		hh.setSinLongitude(cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_SIN_LONGITUDE)));
		
		return hh;
	}
	
	public static Member cursorToMember(Cursor cursor){
		Member mb = new Member();

		mb.setId(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.Member._ID)));
		mb.setCode(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_CODE)));
		mb.setName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_NAME)));
		mb.setGender(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_GENDER)));
		mb.setDob(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_DOB)));
		mb.setAge(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_AGE)));
		mb.setAgeAtDeath(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_AGE_AT_DEATH)));

        mb.setSpouseCode(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_SPOUSE_CODE)));
        mb.setSpouseName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_SPOUSE_NAME)));
		mb.setSpouseType(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_SPOUSE_TYPE)));

		mb.setMotherCode(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_MOTHER_CODE)));
		mb.setMotherName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_MOTHER_NAME)));
		mb.setFatherCode(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_FATHER_CODE)));
		mb.setFatherName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_FATHER_NAME)));

		mb.setHouseholdCode(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_HOUSE_CODE)));
		mb.setHouseholdName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_HOUSE_NAME)));
		mb.setStartType(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_START_TYPE)));
		mb.setStartDate(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_START_DATE)));
		mb.setEndType(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_END_TYPE)));
		mb.setEndDate(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_END_DATE)));

		mb.setEntryHousehold(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_ENTRY_HOUSEHOLD)));
		mb.setEntryType(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_ENTRY_TYPE)));
		mb.setEntryDate(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_ENTRY_DATE)));

		mb.setHouseholdHead(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_IS_HOUSEHOLD_HEAD))==1);
		mb.setSecHouseholdHead(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_IS_SEC_HOUSEHOLD_HEAD))==1);

		mb.setGpsNull(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_GPS_NULL))==1);
		mb.setGpsAccuracy(cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_GPS_ACCURACY)));
		mb.setGpsAltitude(cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_GPS_ALTITUDE)));
		mb.setGpsLatitude(cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_GPS_LATITUDE)));
		mb.setGpsLongitude(cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_GPS_LONGITUDE)));
		mb.setCosLatitude(cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_COS_LATITUDE)));
		mb.setSinLatitude(cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_SIN_LATITUDE)));
		mb.setCosLongitude(cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_COS_LONGITUDE)));
		mb.setSinLongitude(cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_SIN_LONGITUDE)));
		return mb;
	}

	public static CollectedData cursorToCollectedData(Cursor cursor){
		CollectedData cd = new CollectedData();

		cd.setId(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.CollectedData._ID)));
		cd.setFormId(cursor.getString(cursor.getColumnIndex(DatabaseHelper.CollectedData.COLUMN_FORM_ID)));
		cd.setFormUri(cursor.getString(cursor.getColumnIndex(DatabaseHelper.CollectedData.COLUMN_FORM_URI)));
		cd.setFormXmlPath(cursor.getString(cursor.getColumnIndex(DatabaseHelper.CollectedData.COLUMN_FORM_XML_PATH)));
		cd.setFormInstanceName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.CollectedData.COLUMN_FORM_INSTANCE_NAME)));
		cd.setFormLastUpdatedDate(cursor.getString(cursor.getColumnIndex(DatabaseHelper.CollectedData.COLUMN_FORM_LAST_UPDATED_DATE)));
		cd.setFormModule(cursor.getString(cursor.getColumnIndex(DatabaseHelper.CollectedData.COLUMN_FORM_MODULE)));
		cd.setCollectedBy(cursor.getString(cursor.getColumnIndex(DatabaseHelper.CollectedData.COLUMN_COLLECTED_BY)));
		cd.setUpdatedBy(cursor.getString(cursor.getColumnIndex(DatabaseHelper.CollectedData.COLUMN_UPDATED_BY)));
		cd.setSupervisedBy(cursor.getString(cursor.getColumnIndex(DatabaseHelper.CollectedData.COLUMN_SUPERVISED_BY)));
		cd.setRecordId(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.CollectedData.COLUMN_RECORD_ID)));
		cd.setTableName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.CollectedData.COLUMN_TABLE_NAME)));
		cd.setSupervised(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.CollectedData.COLUMN_SUPERVISED))==1);

		return cd;
	}

	public static TrackingList cursorToTrackingList(Cursor cursor){
		TrackingList tl = new TrackingList();

		tl.setId(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.TrackingList._ID)));
		tl.setName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.TrackingList.COLUMN_NAME)));
		tl.setCode(cursor.getString(cursor.getColumnIndex(DatabaseHelper.TrackingList.COLUMN_CODE)));
		tl.setDetails(cursor.getString(cursor.getColumnIndex(DatabaseHelper.TrackingList.COLUMN_DETAILS)));
		tl.setTitle(cursor.getString(cursor.getColumnIndex(DatabaseHelper.TrackingList.COLUMN_TITLE)));
		tl.setModule(cursor.getString(cursor.getColumnIndex(DatabaseHelper.TrackingList.COLUMN_MODULE)));
		tl.setCompletionRate(cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.TrackingList.COLUMN_COMPLETION_RATE)));

		return tl;
	}

	public static TrackingMemberList cursorToTrackingMembersList(Cursor cursor){
		TrackingMemberList tml = new TrackingMemberList();

		tml.setId(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.TrackingMemberList._ID)));
		tml.setListId(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.TrackingMemberList.COLUMN_LIST_ID)));
		tml.setTrackingId(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.TrackingMemberList.COLUMN_TRACKING_ID)));
		tml.setTitle(cursor.getString(cursor.getColumnIndex(DatabaseHelper.TrackingMemberList.COLUMN_TITLE)));
		tml.setForms(cursor.getString(cursor.getColumnIndex(DatabaseHelper.TrackingMemberList.COLUMN_FORMS)));

		tml.setMemberCode(cursor.getString(cursor.getColumnIndex(DatabaseHelper.TrackingMemberList.COLUMN_MEMBER_CODE)));
		tml.setMemberStudyCode(cursor.getString(cursor.getColumnIndex(DatabaseHelper.TrackingMemberList.COLUMN_MEMBER_STUDY_CODE)));
		tml.setMemberVisit(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.TrackingMemberList.COLUMN_MEMBER_VISIT)));
		tml.setMemberForms(cursor.getString(cursor.getColumnIndex(DatabaseHelper.TrackingMemberList.COLUMN_MEMBER_FORMS)));

		tml.setCompletionRate(cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.TrackingMemberList.COLUMN_COMPLETION_RATE)));

		return tml;
	}
}
