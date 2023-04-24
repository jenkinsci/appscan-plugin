/**
 * @ Copyright IBM Corporation 2016.
 * @ Copyright HCL Technologies Ltd. 2017, 2019.
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

import hudson.ProxyConfiguration;
import hudson.model.ItemGroup;
import hudson.util.Secret;
import jenkins.model.Jenkins;

public final class JenkinsAuthenticationProvider implements IAuthenticationProvider, Serializable {

	private static final long serialVersionUID = 1L;
	
	private ASoCCredentials m_credentials;
	private Proxy m_proxy;
	
	public JenkinsAuthenticationProvider(String id, ItemGroup<?> context) {
		configureCredentials(id, context);
		configureProxy();
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
		return m_proxy;
	}
	
	private String getToken() {
		return Secret.toString(m_credentials.getToken());
	}
	
	private void configureCredentials(String id, ItemGroup<?> context) {
		List<ASoCCredentials> credentialsList = CredentialsProvider.lookupCredentials(ASoCCredentials.class, context,
				null, Collections.<DomainRequirement>emptyList());
		for(ASoCCredentials creds : credentialsList) {
			if(creds.getId().equals(id)) {
				m_credentials = creds;
				return;
			}
		}
		m_credentials = new ASoCCredentials("", "", "", "", "", false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}
	
	private void configureProxy() {
		final ProxyConfiguration proxy = Jenkins.getInstance().proxy;
		
		if(proxy == null) {
			m_proxy = Proxy.NO_PROXY;
			return;
		}
		
		//Set up the proxy host and port
		if(proxy.name != null && proxy.port > 0) {
			m_proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxy.name, proxy.port));
		}

		//If authentication is required
		if(proxy.getUserName() != null && proxy.getPassword() != null) {
			Authenticator.setDefault(new Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(proxy.getUserName(), proxy.getPassword().toCharArray());
				}
			});
		}
	}

	public boolean isAppScan360(){
		String URL = m_credentials.getUrl();
		if(!((URL == null || URL.equals("")))){
			return (!(URL.contains("appscan.com")));
		} else {
			return false;
		}
	}

    @Override
    public boolean getCertificates(){
        return m_credentials.getCertificates();
    }
}
