/**
 * @ Copyright HCL Technologies Ltd. 2019.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.hcl.appscan.jenkins.plugin.auth;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.hcl.appscan.sdk.auth.ASEAuthenticationHandler;
import com.hcl.appscan.sdk.auth.IASEAuthenticationProvider;
import hudson.model.ItemGroup;
import hudson.util.Secret;
import java.io.IOException;
import java.io.Serializable;
import java.net.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.wink.json4j.JSONException;

public class ASEJenkinsAuthenticationProvider implements IASEAuthenticationProvider, Serializable {

	private static final long serialVersionUID = 1L;
	
	private ASECredentials m_credentials;
	
	public ASEJenkinsAuthenticationProvider(String id, ItemGroup<?> context) {
		List<ASECredentials> credentialsList = CredentialsProvider.lookupCredentials(ASECredentials.class, context,
				null, Collections.<DomainRequirement>emptyList());
		for(ASECredentials creds : credentialsList) {
			if(creds.getId().equals(id)) {
				m_credentials = creds;
				return;
			}
		}
		m_credentials = new ASECredentials("", "", "", "",""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}
	
	@Override
	public boolean isTokenExpired() {
		boolean isExpired = false;
		ASEAuthenticationHandler handler = new ASEAuthenticationHandler(this);

		try {
			isExpired = handler.isTokenExpired() && !handler.login(m_credentials.getUsername(), Secret.toString(m_credentials.getPassword()), true,m_credentials.getServer());
		} catch (IOException | JSONException e) {
			isExpired = false;
		}
		return isExpired;
	}

	@Override
	public Map<String, String> getAuthorizationHeader(boolean persist) {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("asc_xsrf_token", getToken().trim()); //$NON-NLS-1$ //$NON-NLS-2$
                List<String> cookies=getCookies();
                if (cookies != null) {		       
		       headers.put("Cookie",cookies.get(0));
		    }
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

    public void setCookies(List<String> cookies) {
        m_credentials.setCookies(cookies);
    }

	private String getToken() {
		return Secret.toString(m_credentials.getToken());
	}

    private List<String> getCookies() {
        return m_credentials.getCookies();
    }

    @Override
    public Proxy getProxy() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}