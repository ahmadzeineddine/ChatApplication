package com.ahmed.chatapplication.response;

import org.json.JSONObject;

public interface VolleyJsonResponse {
	void processFinish(JSONObject output);
	void processError(String error);
}
