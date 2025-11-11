/**
 * @ Copyright IBM Corporation 2016.
 * @ Copyright HCL Technologies Ltd. 2017, 2024, 2025.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.hcl.appscan.jenkins.plugin.auth;

import java.io.IOException;
import java.io.Serializable;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wink.json4j.JSONException;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.hcl.appscan.sdk.auth.AuthenticationHandler;
import com.hcl.appscan.sdk.auth.IAuthenticationProvider;
import com.hcl.appscan.sdk.auth.LoginType;
import com.hcl.appscan.jenkins.plugin.util.JenkinsUtil;

import hudson.ProxyConfiguration;
import hudson.model.ItemGroup;
import hudson.util.Secret;
import jenkins.model.Jenkins;

public class JenkinsAuthenticationProvider implements IAuthenticationProvider, Serializable {

	private static final long serialVersionUID = 1L;
	
	private ASoCCredentials m_credentials;
        
	public JenkinsAuthenticationProvider(String id, ItemGroup<?> context) {
		configureCredentials(id, context);
	}
	
	@Override
	public boolean isTokenExpired() {
		boolean isExpired = false;
		AuthenticationHandler handler = new AuthenticationHandler(this);

		// If token is not expired, return false
		if (!handler.isTokenExpired()) {
			return false;
		}

		// Try logging in again if token is expired
		try {
			String username = m_credentials.getUsername();
			String password = Secret.toString(m_credentials.getPassword());

			// Check login based on a connection type
			if (isAppScan360()) {
				isExpired = handler.login(username, password, true, LoginType.ASoC_Federated, JenkinsUtil.getClientTypeUpdated());
			} else {
				isExpired = handler.login(username, password, true, LoginType.ASoC_Federated, JenkinsUtil.getClientType());
			}
			return isExpired;
		} catch (IOException | JSONException e) {
			// If an error occurs, treat token as expired
			return true;
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
	
	@Override
	public Proxy getProxy() {
		Jenkins jenkins = Jenkins.getInstanceOrNull();
		if(jenkins == null)
			return Proxy.NO_PROXY;
		
		final ProxyConfiguration proxy = jenkins.proxy;
		
		if(proxy != null && proxy.name != null && proxy.port > 0) {
			//If authentication is required
			if(proxy.getUserName() != null && proxy.getPassword() != null) {
				Authenticator.setDefault(new Authenticator() {
					@Override
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(proxy.getUserName(), proxy.getPassword().toCharArray());
					}
				});
			}
			
			return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxy.name, proxy.port));
		}
		
		return Proxy.NO_PROXY;
	}
	
	private String getToken() {
		return Secret.toString(m_credentials.getToken());
	}

	public boolean isAppScan360(){
		String keyId = m_credentials.getUsername();
		return  keyId.trim().startsWith("local");
	}
	
	public void configureCredentials(String id, ItemGroup<?> context) {
		List<ASoCCredentials> credentialsList = CredentialsProvider.lookupCredentials(ASoCCredentials.class, context,
				null, Collections.<DomainRequirement>emptyList());
		for(ASoCCredentials creds : credentialsList) {
			if(creds.getId().equals(id)) {
				m_credentials = creds;
				return;
			}
		}
		m_credentials = new ASoCCredentials("", "", "", "", "",false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}

    	@Override
    	public boolean getacceptInvalidCerts() {
        	return m_credentials.getacceptInvalidCerts();
    	}
}
