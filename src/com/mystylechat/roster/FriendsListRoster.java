package com.mystylechat.roster;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import tigase.xmpp.JID;
import tigase.xmpp.NotAuthorizedException;
import tigase.xmpp.XMPPResourceConnection;
import tigase.xmpp.impl.roster.DynamicRosterIfc;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mystylechat.auth.MyStyleChatCallbackHandler;

public class FriendsListRoster implements DynamicRosterIfc {
	private static final Logger log = Logger.getLogger(FriendsListRoster.class.getName());
	private static final HttpClient httpClient = new DefaultHttpClient();
	
	@Override
	public void init(Map<String, Object> props) {}

	@Override
	public void init(String param) {}

	private class JsonFriendList {
		List<JsonFriend> data;
		JsonPaging paging;
	}
	
	private class JsonFriend {
		public String name;
		public String id;
	}
	
	private class JsonPaging {
		public String next;
	}
	
	@Override
	public JID[] getBuddies(XMPPResourceConnection session)
		throws NotAuthorizedException {
		String facebookToken = (String)session.getSessionData(MyStyleChatCallbackHandler.FACEBOOK_TOKEN_KEY);
		if (facebookToken != null) {
			String url = MessageFormat.format("https://graph.facebook.com/{0}/friends?fields=name&access_token={1}",
					session.getJID().getLocalpart(),
					URLEncoder.encode(facebookToken));
			log.warning("Facebook getting url: " + url);
			HttpGet get = new HttpGet(url);
			
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String responseBody;
			try {
				responseBody = httpClient.execute(get, responseHandler);
			} catch (IOException e) {
				log.warning("Facebook " + e.toString());
				return null;
			}

			Gson gson = new Gson();
			JsonFriendList friendList = gson.fromJson(responseBody, JsonFriendList.class);
			log.warning("Facebook friends " + friendList.data.size());
			JID[] result = new JID[friendList.data.size()];
			int i = 0;
			String domain = session.getJID().getDomain();
			for (JsonFriend friend : friendList.data) {
				result[i] = JID.jidInstanceNS(friend.id, domain, null);
				i++;
			}
			return result;
		}
		return null;
	}

	@Override
	public tigase.xml.Element getBuddyItem(XMPPResourceConnection session, tigase.xmpp.JID buddy)
			throws NotAuthorizedException {
		return null;
	};
	
	@Override
	public java.util.List<tigase.xml.Element> getRosterItems(XMPPResourceConnection session)
			throws NotAuthorizedException {
		return null;
	};
	
	@Override
	public void setItemExtraData(tigase.xml.Element item) {};
	
	@Override
	public tigase.xml.Element getItemExtraData(tigase.xml.Element item) { return null; };
}
