package com.foxykeep.cpcodegenerator.model;

import org.json.JSONException;
import org.json.JSONObject;

public class UpdateData {

	   public String name;
	   public String dbCommand;

    public int version;

    public UpdateData(final JSONObject json) throws JSONException {
        version = json.optInt("version", 1);
    	name = json.getString("name");
    	dbCommand = json.getString("command");
	}

}
