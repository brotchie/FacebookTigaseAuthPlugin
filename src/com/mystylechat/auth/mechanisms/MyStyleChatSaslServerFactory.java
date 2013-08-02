package com.mystylechat.auth.mechanisms;

import java.util.Map;
import java.util.logging.Logger;

import javax.security.auth.callback.CallbackHandler;
import javax.security.sasl.SaslException;
import javax.security.sasl.SaslServer;
import javax.security.sasl.SaslServerFactory;

public class MyStyleChatSaslServerFactory implements SaslServerFactory {
	private static final Logger log = Logger.getLogger(MyStyleChatSaslServerFactory.class.getName());
	
	public MyStyleChatSaslServerFactory() {
	}

	@Override
	public SaslServer createSaslServer(String mechanism, String protocol, String serverName,
			Map<String, ?> props, CallbackHandler callbackHandler) throws SaslException {
		log.warning("Matching mechanism " + mechanism);
		
		if (mechanism.equals("MYSTYLECHAT")) {
			return new SaslMYSTYLECHAT(props, callbackHandler);
		} else {
			throw new SaslException("Mechanism not supported.");
		}
	}

	@Override
	public String[] getMechanismNames(Map<String, ?> props) {
		return new String[] {"MYSTYLECHAT"};
	}

}
