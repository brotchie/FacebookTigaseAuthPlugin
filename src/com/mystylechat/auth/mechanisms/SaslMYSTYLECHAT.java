package com.mystylechat.auth.mechanisms;

import java.util.Map;
import java.util.logging.Logger;

import javax.security.auth.callback.CallbackHandler;
import javax.security.sasl.SaslException;

import tigase.auth.XmppSaslException;
import tigase.auth.XmppSaslException.SaslError;
import tigase.auth.mechanisms.AbstractSasl;

import com.mystylechat.auth.ValidateFacebookTokenCallback;

public class SaslMYSTYLECHAT extends AbstractSasl {
	private static final String MECHANISM = "MYSTYLECHAT";
	private static final String STUB_FACEBOOK_AUTH_KEY = "stub-facebook-auth";
	
	private static final Logger log = Logger.getLogger(SaslMYSTYLECHAT.class.getName());
	
	public SaslMYSTYLECHAT(Map<? super String, ?> props, CallbackHandler handler) {
		super(props, handler);
	}
	
	@Override
	public byte[] evaluateResponse(byte[] response) throws SaslException {
		final String[] data = split(response, "");
		if (data.length != 2)
			throw new XmppSaslException(SaslError.malformed_request, "Invalid number of message parts");
		
		final String user_id = data[0];
		final String auth_token = data[1];
		
		// Early exit if we're asked to stub Facebook auth validation.
		final String stubFacebookAuth = (String)props.get(STUB_FACEBOOK_AUTH_KEY);
		if (stubFacebookAuth != null && stubFacebookAuth.equals("true")) {
			log.warning("Facebook: Stubbing Facebook Auth for " + user_id);
			authorizedId = user_id;
			complete = true;
			return null;
		}
		
		ValidateFacebookTokenCallback vfbtc = new ValidateFacebookTokenCallback(user_id, auth_token);
		
		handleCallbacks(vfbtc);
		
		if (vfbtc.isValid() == false) {
			throw new XmppSaslException(SaslError.not_authorized, "Invalid auth token");
		}
		
		authorizedId = user_id;
		complete = true;
		
		return null;
	}

	public String getAuthorizationID() {
		return authorizedId;
	}

	@Override
	public String getMechanismName() {
		return MECHANISM;
	}

	@Override
	public Object getNegotiatedProperty(String propName) {
		return null;
	}

	@Override
	public byte[] unwrap(byte[] incoming, int offset, int length) throws SaslException {
		return null;
	}

	@Override
	public byte[] wrap(byte[] outgoing, int offset, int length) throws SaslException {
		return null;
	}

}
