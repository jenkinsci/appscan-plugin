/**
 * @ Copyright IBM Corporation 2016.
 * @ Copyright HCL Technologies Ltd. 2017.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.ibm.appscan.jenkins.plugin.auth;

import hudson.model.ItemGroup;
import hudson.util.Secret;
import jenkins.model.Jenkins;

import java.io.IOException;
import java.io.Serializable;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.apache.wink.json4j.JSONException;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.hcl.appscan.sdk.auth.AuthenticationHandler;
import com.hcl.appscan.sdk.auth.IAuthenticationProvider;
import com.hcl.appscan.sdk.auth.LoginType;

public final class JenkinsAuthenticationProvider implements IAuthenticationProvider, Serializable {

	private static final long serialVersionUID = 1L;
	
	private ASoCCredentials m_credentials;
	
	public JenkinsAuthenticationProvider(String id, ItemGroup<?> context) {
		List<ASoCCredentials> credentialsList = CredentialsProvider.lookupCredentials(ASoCCredentials.class, context,
				null, Collections.<DomainRequirement>emptyList());
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
		boolean isExpired = false;
		AuthenticationHandler handler = new AuthenticationHandler(this);

		try {
			isExpired = handler.isTokenExpired() && !handler.login(m_credentials.getUsername(), Secret.toString(m_credentials.getPassword()), true, LoginType.ASoC_Federated);
		} catch (IOException | JSONException e) {
			isExpired = false;
		}
		return isExpired;
	}

	@Override
	public Map<String, String> getAuthorizationHeader(boolean persist) {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Authorization", "Bearer "+ getToken().trim()); //$NON-NLS-1$ //$NON-NLS-2$
		headers.put("Connection", "Keep-Alive");
		headers.putAll(Collections.singletonMap("range", "items=0-999999"));
		
		try {
		String proxyCreds = Jenkins.getInstance().proxy.getUserName() + ":" 
				+ Jenkins.getInstance().proxy.getPassword();
		//String encoded = new String(Base64.getEncoder().encode("quanzt:test".getBytes()));
		String encoded = DatatypeConverter.printBase64Binary(proxyCreds.getBytes());
		headers.put("Proxy-Authorization","Basic " + encoded);
		Authenticator.setDefault(new ProxyAuth(Jenkins.getInstance().proxy.getUserName(), Jenkins.getInstance().proxy.getPassword()));
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		if(persist)
			headers.put("Connection", "Keep-Alive"); //$NON-NLS-1$ //$NON-NLS-2$
		return headers;
	}

	public class ProxyAuth extends Authenticator {
		private PasswordAuthentication auth;
		private ProxyAuth(String user, String password) {
			auth = new PasswordAuthentication(user, password == null ? new char[] {}: password.toCharArray());
			
		}
		protected PasswordAuthentication getPasswordAuthentication() {
			return auth;
		}
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
