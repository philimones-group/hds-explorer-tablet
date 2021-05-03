package org.philimone.hds.explorer.database;

import android.database.Cursor;

import org.philimone.hds.explorer.model.enums.SyncEntity;
import org.philimone.hds.explorer.model.enums.SyncStatus;
import org.philimone.hds.explorer.model.CollectedData;
import org.philimone.hds.explorer.model.DataSet;
import org.philimone.hds.explorer.model.Form;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Module;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.SyncReport;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.model.followup.TrackingList;


public class Converter {

	public static User cursorToUser(Cursor cursor){
		User user = new User();

		user.setCode(cursor.getString(cursor.getColumnIndex(DatabaseHelper.User.COLUMN_CODE)));
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
		form.setRegionLevel(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Form.COLUMN_REGION_LEVEL)));
        form.setGender(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Form.COLUMN_GENDER)));
        form.setMinAge(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.Form.COLUMN_MIN_AGE)));
        form.setMaxAge(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.Form.COLUMN_MAX_AGE)));
        form.setModules(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Form.COLUMN_MODULES)));
		form.setRegionForm(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.Form.COLUMN_IS_REGION))==1);
        form.setHouseholdForm(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.Form.COLUMN_IS_HOUSEHOLD))==1);
		form.setHouseholdHeadForm(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.Form.COLUMN_IS_HOUSEHOLD_HEAD))==1);
		form.setMemberForm(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.Form.COLUMN_IS_MEMBER))==1);
		form.setFollowUpOnly(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.Form.COLUMN_IS_FOLLOW_UP_ONLY))==1);
		form.setMultiCollPerSession(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.Form.COLUMN_MULTI_COL_PER_SESSION))==1);
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
		hh.setRecentlyCreated(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_RECENTLY_CREATED))==1);
		
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
		mb.setMaritalStatus(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_MARITAL_STATUS)));

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

		mb.setHeadRelationshipType(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_HEAD_RELATIONSHIP_TYPE)));

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
		mb.setRecentlyCreated(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_RECENTLY_CREATED))==1);
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

	public static org.philimone.hds.explorer.model.followup.TrackingSubjectList cursorToTrackingSubjectList(Cursor cursor){
		org.philimone.hds.explorer.model.followup.TrackingSubjectList tml = new org.philimone.hds.explorer.model.followup.TrackingSubjectList();

		tml.setId(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.TrackingSubjectList._ID)));
		tml.setListId(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.TrackingSubjectList.COLUMN_LIST_ID)));
		tml.setTrackingId(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.TrackingSubjectList.COLUMN_TRACKING_ID)));
		tml.setTitle(cursor.getString(cursor.getColumnIndex(DatabaseHelper.TrackingSubjectList.COLUMN_TITLE)));
		tml.setForms(cursor.getString(cursor.getColumnIndex(DatabaseHelper.TrackingSubjectList.COLUMN_FORMS)));

		tml.setSubjectCode(cursor.getString(cursor.getColumnIndex(DatabaseHelper.TrackingSubjectList.COLUMN_SUBJECT_CODE)));
		tml.setSubjectType(cursor.getString(cursor.getColumnIndex(DatabaseHelper.TrackingSubjectList.COLUMN_SUBJECT_TYPE)));
		tml.setSubjectVisit(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.TrackingSubjectList.COLUMN_SUBJECT_VISIT)));
		tml.setSubjectForms(cursor.getString(cursor.getColumnIndex(DatabaseHelper.TrackingSubjectList.COLUMN_SUBJECT_FORMS)));

		tml.setCompletionRate(cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.TrackingSubjectList.COLUMN_COMPLETION_RATE)));

		return tml;
	}

	public static DataSet cursorToDataSet(Cursor cursor){
		DataSet dataSet = new DataSet();

		dataSet.setId(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.DataSet._ID)));
		dataSet.setDatasetId(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.DataSet.COLUMN_DATASET_ID)));
		dataSet.setName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.DataSet.COLUMN_NAME)));
		dataSet.setTableNameField(cursor.getString(cursor.getColumnIndex(DatabaseHelper.DataSet.COLUMN_TABLE_NAME)));
		dataSet.setTableColumn(cursor.getString(cursor.getColumnIndex(DatabaseHelper.DataSet.COLUMN_TABLE_COLUMN)));
        dataSet.setKeyColumn(cursor.getString(cursor.getColumnIndex(DatabaseHelper.DataSet.COLUMN_KEYCOLUMN)));
		dataSet.setFilename(cursor.getString(cursor.getColumnIndex(DatabaseHelper.DataSet.COLUMN_FILENAME)));

		dataSet.setCreatedBy(cursor.getString(cursor.getColumnIndex(DatabaseHelper.DataSet.COLUMN_CREATED_BY)));
		dataSet.setCreationDate(cursor.getString(cursor.getColumnIndex(DatabaseHelper.DataSet.COLUMN_CREATION_DATE)));
		dataSet.setUpdatedBy(cursor.getString(cursor.getColumnIndex(DatabaseHelper.DataSet.COLUMN_UPDATED_BY)));
		dataSet.setUpdatedDate(cursor.getString(cursor.getColumnIndex(DatabaseHelper.DataSet.COLUMN_UPDATED_DATE)));

		dataSet.setLabels(cursor.getString(cursor.getColumnIndex(DatabaseHelper.DataSet.COLUMN_LABELS)));

		return dataSet;
	}
}
