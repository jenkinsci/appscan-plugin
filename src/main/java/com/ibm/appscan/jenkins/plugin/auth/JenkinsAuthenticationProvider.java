/**
 * © Copyright IBM Corporation 2016.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.ibm.appscan.jenkins.plugin.auth;

import hudson.model.Item;
import hudson.util.Secret;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wink.json4j.JSONException;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.ibm.appscan.plugin.core.auth.AuthenticationHandler;
import com.ibm.appscan.plugin.core.auth.IAuthenticationProvider;

public final class JenkinsAuthenticationProvider implements IAuthenticationProvider {

	private ASoCCredentials m_credentials;
	
	public JenkinsAuthenticationProvider(String id) {
		List<ASoCCredentials> credentialsList = CredentialsProvider.lookupCredentials(ASoCCredentials.class, (Item)null,
				null, (List<DomainRequirement>)null);
		for(ASoCCredentials creds : credentialsList) {
			if(creds.getId().equals(id)) {
				m_credentials = creds;
				return;
			}
		}
		m_credentials = new ASoCCredentials("", "", "", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}
	
	@Override
	public boolean isTokenExpired() {
		AuthenticationHandler handler = new AuthenticationHandler(this);
		try {
			return handler.isTokenExpired() && !handler.login(m_credentials.getUsername(), Secret.toString(m_credentials.getPassword()), true);
		} catch (IOException | JSONException e) {
			return false;
		}
	}

	@Override
	public Map<String, String> getAuthorizationHeader(boolean persist) {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Authorization", "Bearer "+ getToken().trim()); //$NON-NLS-1$ //$NON-NLS-2$
		if(persist)
			headers.put("Connection", "Keep-Alive"); //$NON-NLS-1$ //$NON-NLS-2$
		return headers;
	}

	@Override
	public String getServer() {
		return m_credentials.getServer();
	}

	@Override
	public void saveConnection(String connection) {
		m_credentials.setToken(connection);
	}
	
	private String getToken() {
		return Secret.toString(m_credentials.getToken());
	}
}
