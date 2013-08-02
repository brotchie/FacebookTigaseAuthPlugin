package com.mystylechat.auth;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.logging.Logger;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import tigase.auth.SessionAware;
import tigase.xmpp.XMPPResourceConnection;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class MyStyleChatCallbackHandler implements CallbackHandler, SessionAware {
	private final static String FACEBOOK_API_ID = "1387573978127294";
	private final static String FACEBOOK_API_SECRET = "1e63d3e4f5a8cb97ef513989de52069c";
	private static final Logger log = Logger.getLogger(MyStyleChatCallbackHandler.class.getName());
	
	public static final String FACEBOOK_TOKEN_KEY = "Facebook-Token";
	private static String FACEBOOK_APP_TOKEN;
	private XMPPResourceConnection _session;
	
	@Override
	public void setSession(XMPPResourceConnection session) {
		_session = session;
	}
	
	@Override
	public void handle(Callback[] callbacks) throws IOException,
			UnsupportedCallbackException {
		for (Callback callback : callbacks) {
			if (callback.getClass().equals(ValidateFacebookTokenCallback.class)) {
				handleValidateFacebookTokenCallback((ValidateFacebookTokenCallback)callback);
			} else {
				throw new UnsupportedCallbackException(callback);
			}
		}
	}
	
	private String getFacebookAppToken() throws IOException {
		HttpClient httpclient = new DefaultHttpClient();
		synchronized(FACEBOOK_API_ID) {
			if (FACEBOOK_APP_TOKEN == null) {
				String url = MessageFormat.format("https://graph.facebook.com/oauth/access_token?client_id={0}&client_secret={1}&grant_type=client_credentials", FACEBOOK_API_ID, FACEBOOK_API_SECRET);
				HttpGet get = new HttpGet(url);
				
				ResponseHandler<String> responseHandler = new BasicResponseHandler();
				String responseBody = httpclient.execute(get, responseHandler);
				
				String[] parts = responseBody.split("=", 2);
				if (parts.length == 2 && parts[0].equals("access_token")) {
					FACEBOOK_APP_TOKEN = parts[1];
					log.warning("Fetched Facebook app token: " + FACEBOOK_APP_TOKEN);
				}
			}
		}
		return FACEBOOK_APP_TOKEN;
	}
	
	private void handleValidateFacebookTokenCallback(ValidateFacebookTokenCallback callback) throws IOException {
		HttpClient httpclient = new DefaultHttpClient();
		
		String appToken = getFacebookAppToken();
		
		if (appToken != null) {
			String url = MessageFormat.format("https://graph.facebook.com/debug_token?input_token={0}&access_token={1}",
					URLEncoder.encode(callback.getAuthToken()),
					URLEncoder.encode(appToken));
			HttpGet get = new HttpGet(url);
			
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String responseBody = httpclient.execute(get, responseHandler);
			
			log.warning("Received Facebook JSON: " + responseBody);
			
			JsonElement element = new JsonParser().parse(responseBody);
			if (element == null)
				return;
			
			JsonObject object = element.getAsJsonObject();
			if (object == null || !object.has("data"))
				return;
			
			object = object.getAsJsonObject("data");
			if (!object.has("user_id") || !object.has("is_valid"))
				return;
			
			String user_id = object.get("user_id").getAsString();
			boolean is_valid = object.get("is_valid").getAsBoolean();
			log.warning("Facebook is_valid: " + is_valid + " " + user_id + " == " + callback.getUserId());
			callback.setValid(is_valid && user_id.equals(callback.getUserId()));
			
			if (callback.isValid() && _session != null) {
				log.warning("Added Facebook token key: " + callback.getAuthToken());
				_session.putSessionData(FACEBOOK_TOKEN_KEY, callback.getAuthToken());
			}
		}
	}
}
