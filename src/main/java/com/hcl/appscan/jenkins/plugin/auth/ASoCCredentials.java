/**
 * © Copyright IBM Corporation 2016.
 * @ Copyright HCL Technologies Ltd. 2019, 2024.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package com.hcl.appscan.jenkins.plugin.auth;

import hudson.Extension;
import hudson.util.FormValidation;
import hudson.util.Secret;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import com.cloudbees.plugins.credentials.CredentialsDescriptor;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import com.hcl.appscan.sdk.CoreConstants;
import com.hcl.appscan.sdk.utils.SystemUtil;
import com.hcl.appscan.jenkins.plugin.Messages;

public class ASoCCredentials extends UsernamePasswordCredentialsImpl {

	private static final long serialVersionUID = 1L;
	private Secret m_token;
	public String m_url;
        public boolean m_acceptInvalidCerts;

	@DataBoundConstructor
	public ASoCCredentials(String id, String description, String username, String password, String url, boolean acceptInvalidCerts) {
		this(CredentialsScope.GLOBAL, id, description, username, password, acceptInvalidCerts);
		m_url=url;
                m_acceptInvalidCerts=acceptInvalidCerts;
	}
	
	public ASoCCredentials(CredentialsScope scope, String id, String description, String username, String password, boolean acceptInvalidCerts) {
		super(scope, description, description, username, password);
	}
	
	@Override
	public CredentialsDescriptor getDescriptor() {
		return (DescriptorImpl)super.getDescriptor();
	}

	public String getUrl() {
		return m_url;
	}

        public boolean getacceptInvalidCerts() {return m_acceptInvalidCerts;}
	
	public String getServer() {
		String url = m_url;
		if(url == null || url.equals("")){
			url = SystemUtil.getServer(getUsername());
		}
		return url.endsWith("/") ? url.substring(0, url.length()-1) : url;
	}
	
	public Secret getToken() {
		return m_token;
	}

	public void setToken(String connection) {
		m_token = Secret.fromString(connection);
	}
	
	@Symbol("asoc-credentials") //$NON-NLS-1$
    @Extension
    public static class DescriptorImpl extends CredentialsDescriptor {
		
		@Override
		public String getDisplayName() {
			return Messages.label_asoc();
		}
		
		public String getApiKeyUrl() {
			return SystemUtil.getDefaultServer() + CoreConstants.API_KEY_PATH;
		}

		public FormValidation doCheckUsername(@QueryParameter String username) {
			if(username.trim().equals("")) //$NON-NLS-1$
				return FormValidation.errorWithMarkup(Messages.need_api_key(getApiKeyUrl())); //$NON-NLS-1$
			return FormValidation.ok();
		}
		
		public FormValidation doCheckPassword(@QueryParameter String password) {
			return FormValidation.validateRequired(password);
		}

                public FormValidation doCheckAcceptInvalidCerts(@QueryParameter Boolean acceptInvalidCerts,@QueryParameter String username){
            		if((!username.trim().startsWith("local")) && acceptInvalidCerts) {
                		return FormValidation.error(Messages.error_asoc_certificates_ui());
            		}
            		return FormValidation.ok();
        	}
    }
}
