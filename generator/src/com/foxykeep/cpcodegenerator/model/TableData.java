package com.foxykeep.cpcodegenerator.model;

import com.foxykeep.cpcodegenerator.util.NameUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableData {

	public String dbClassName = null;
	public String dbTableName = null;
	public String dbConstantName = null;

	public int version;

	public List<FieldData> fieldList = new ArrayList<FieldData>();

	public Map<Integer, List<FieldData>> upgradeFieldMap = new HashMap<Integer, List<FieldData>>();

	public List<UpdateData> updateList = new ArrayList<UpdateData>();

	public Map<Integer, List<UpdateData>> updateDataMap = new HashMap<Integer, List<UpdateData>>();
	
	public List<String> initialDataList = new ArrayList<String>();

	public TableData(final JSONObject json, final String contentClassesPrefix,
			final int dbVersion) throws JSONException {
		dbClassName = contentClassesPrefix + json.getString("table_name");
		dbTableName = NameUtils.createLowerCamelCaseName(dbClassName);
		dbConstantName = NameUtils.createConstantName(dbTableName);

		version = json.optInt("version", 1);
		if (version > dbVersion) {
			throw new IllegalArgumentException("The table " + dbClassName
					+ " has a version (" + version
					+ ") higher than the database version (" + dbVersion + ")");
		}

		final JSONArray jsonFieldArray = json.getJSONArray("fields");
		for (int i = 0, n = jsonFieldArray.length(); i < n; i++) {
			fieldList.add(new FieldData(jsonFieldArray.getJSONObject(i)));
		}

		for (FieldData fieldData : fieldList) {
			if (fieldData.version > dbVersion) {
				throw new IllegalArgumentException("The field "
						+ fieldData.name + " has a version ("
						+ fieldData.version + ") higher than the database "
						+ "version (" + dbVersion + ")");
			}
			List<FieldData> upgradeList = upgradeFieldMap
					.get(fieldData.version);
			if (upgradeList == null) {
				upgradeList = new ArrayList<FieldData>();
				upgradeFieldMap.put(fieldData.version, upgradeList);
			}

			upgradeList.add(fieldData);
		}
		
		if (json.has("initial_data")) {
			final JSONArray jsonInitialDataArray = json.getJSONArray("initial_data");
			for (int i = 0, n = jsonInitialDataArray.length(); i < n; i++) {
				String command = jsonInitialDataArray.getJSONObject(i).getString("command");
				initialDataList.add( command);
			}
			
		}

		if (json.has("updates")) {
			final JSONArray jsonUpdateArray = json.getJSONArray("updates");
			for (int i = 0, n = jsonUpdateArray.length(); i < n; i++) {
				updateList
						.add(new UpdateData(jsonUpdateArray.getJSONObject(i)));
			}

			for (UpdateData updateData : updateList) {
				if (updateData.version > dbVersion) {
					throw new IllegalArgumentException("The command "
							+ updateData.name + " has a version ("
							+ updateData.version
							+ ") higher than the database " + "version ("
							+ dbVersion + ")");
				}
				List<UpdateData> upgradeList = updateDataMap
						.get(updateData.version);
				if (upgradeList == null) {
					upgradeList = new ArrayList<UpdateData>();
					updateDataMap.put(updateData.version, upgradeList);
				}

				upgradeList.add(updateData);
			}
		}

	}

	public static ArrayList<TableData> getClassesData(
			final JSONArray jsonClassArray, final String contentClassesPrefix,
			final int dbVersion) throws JSONException {
		final ArrayList<TableData> classDataList = new ArrayList<TableData>();

		final int jsonClassArrayLength = jsonClassArray.length();
		for (int i = 0; i < jsonClassArrayLength; i++) {
			classDataList.add(new TableData(jsonClassArray.getJSONObject(i),
					contentClassesPrefix, dbVersion));
		}

		return classDataList;
	}
}
