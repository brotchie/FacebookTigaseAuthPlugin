package com.mystylechat.roster;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import tigase.util.JIDUtils;
import tigase.xml.Element;
import tigase.xmpp.JID;
import tigase.xmpp.NotAuthorizedException;
import tigase.xmpp.XMPPResourceConnection;
import tigase.xmpp.impl.roster.DynamicRosterIfc;

import com.google.gson.Gson;
import com.mystylechat.auth.MyStyleChatCallbackHandler;

public class FriendsListRoster implements DynamicRosterIfc {
	private static final Logger log = Logger.getLogger(FriendsListRoster.class.getName());
	private static final HttpClient httpClient = new DefaultHttpClient();
	private static final FriendsListRosterCache rosterCache = new FriendsListRosterCache();
	
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
	
	private JID[] fetchFacebookFriendsList(XMPPResourceConnection session) throws NotAuthorizedException {
		JID[] roster = rosterCache.getRoster(session.getBareJID());
	
		// Fetch the user's friends list from Facebook if not in cache and we have a valid facebook auth token.
		String facebookToken = (String)session.getSessionData(MyStyleChatCallbackHandler.FACEBOOK_TOKEN_KEY);
		if (roster == null && facebookToken != null) {
			String url = MessageFormat.format("https://graph.facebook.com/{0}/friends?fields=name&access_token={1}",
					session.getJID().getLocalpart(),
					URLEncoder.encode(facebookToken));
			log.warning("Facebook getting url: " + url);
				HttpGet get = new HttpGet(url);
				
				ResponseHandler<String> responseHandler = new BasicResponseHandler();
				String responseBody;
			try {
				try {
					responseBody = httpClient.execute(get, responseHandler);
				} catch (IOException e) {
					log.warning("Facebook " + e.toString());
					return null;
				}
	
				Gson gson = new Gson();
				JsonFriendList friendList = gson.fromJson(responseBody, JsonFriendList.class);
				log.warning("Facebook friends " + friendList.data.size());
				roster = new JID[friendList.data.size()];
				int i = 0;
				String domain = session.getJID().getDomain();
				for (JsonFriend friend : friendList.data) {
					roster[i] = JID.jidInstanceNS(friend.id, domain, null);
					i++;
				}
				rosterCache.setRoster(session.getBareJID(), roster);
			} finally {
				get.releaseConnection();
			}
		}
		return roster;
	}
	
	@Override
	public JID[] getBuddies(XMPPResourceConnection session)
		throws NotAuthorizedException {
		return fetchFacebookFriendsList(session);
	}

	@Override
	public Element getBuddyItem(XMPPResourceConnection session, JID buddy)
			throws NotAuthorizedException {
		return new Element("item", new Element[] {
				new Element("group", "Facebook")
			},
			new String[] {"jid", "subscription", "name"},
			new String[] {buddy.toString(), "both", JIDUtils.getNodeNick(buddy.toString())
		});
	};
	
	@Override
	public List<Element> getRosterItems(XMPPResourceConnection session)
			throws NotAuthorizedException {
		JID[] roster = fetchFacebookFriendsList(session);
		if (roster != null) {
			ArrayList<Element> items = new ArrayList<Element>(roster.length);
			for (int i = 0; i < roster.length; i++) {
				items.add(getBuddyItem(session, roster[i]));
			}
			return items;
		} else {
			return null;
		}
	};
	
	@Override
	public void setItemExtraData(Element item) {};
	
	@Override
	public Element getItemExtraData(Element item) { return null; };
}
