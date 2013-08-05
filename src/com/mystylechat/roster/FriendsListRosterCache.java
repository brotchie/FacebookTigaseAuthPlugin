package com.mystylechat.roster;

import java.io.IOException;
import java.net.InetSocketAddress;

import tigase.xmpp.BareJID;
import tigase.xmpp.JID;

import net.spy.memcached.MemcachedClient;

public class FriendsListRosterCache {
	private static final int DEFAULT_EXPIRY = 3600;
	private static final InetSocketAddress memcacheAddress = new InetSocketAddress("localhost", 11211);
	private static MemcachedClient client;
	
	public FriendsListRosterCache() {
		try { 
			client = new MemcachedClient(memcacheAddress);
		} catch (IOException e) {};
	}
	
	public void setRoster(BareJID jid, JID[] roster) {
		String[] values = new String[roster.length];
		for (int i = 0; i < roster.length; i++) { values[i] = roster[i].toString(); };
		client.set(jid.toString(), DEFAULT_EXPIRY, values);
	}
	
	public JID[] getRoster(BareJID jid) {
		String[] values = (String[])client.get(jid.toString());
		if (values != null) {
			JID[] roster = new JID[values.length];
			for (int i = 0; i < values.length; i++) { roster[i] = JID.jidInstanceNS(values[i]); };
			return roster;
		} else {
			return null;
		}
	}
}