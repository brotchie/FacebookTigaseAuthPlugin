package com.mystylechat.auth;

import javax.security.auth.callback.Callback;

public class ValidateFacebookTokenCallback implements Callback {
	private boolean _valid = false;
	private String _userId;
	private String _authToken;
	
	public ValidateFacebookTokenCallback(String userId, String authToken) {
		_userId = userId;
		_authToken = authToken;
	}
	
	public String getAuthToken() {
		return _authToken;
	}
	
	public String getUserId() {
		return _userId;
	}
	
	public boolean isValid() {
		return _valid;
	}
	
	public void setValid(boolean valid) {
		_valid = valid;
	}
}
