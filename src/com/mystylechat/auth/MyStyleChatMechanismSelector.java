package com.mystylechat.auth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;

import javax.security.sasl.SaslServerFactory;

import tigase.auth.MechanismSelector;
import tigase.xmpp.XMPPResourceConnection;

public class MyStyleChatMechanismSelector implements MechanismSelector {

	@Override
	public Collection<String> filterMechanisms(
			Enumeration<SaslServerFactory> serverFactories,
			XMPPResourceConnection session) {
		final ArrayList<String> result = new ArrayList<String>();
		result.add("MYSTYLECHAT");
		return result;
	}

	@Override
	public void init(Map<String, Object> settings) {
	}

}
