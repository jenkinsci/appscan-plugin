/** 
 * @ Copyright HCL Technologies Ltd. 2019.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.hcl.appscan.jenkins.plugin.auth;

import com.cloudbees.plugins.credentials.CredentialsDescriptor;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;

import com.hcl.appscan.jenkins.plugin.Messages;
import hudson.Extension;
import hudson.util.FormValidation;
import hudson.util.Secret;
import java.util.List;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

public class ASECredentials extends UsernamePasswordCredentialsImpl{

	private static final long serialVersionUID = 1L;
	private Secret m_token;
	private String m_url;
	private List<String> m_cookies;

	@DataBoundConstructor
	public ASECredentials(String id, String description, String username, String password, String url) {
		this(CredentialsScope.GLOBAL, id, description, username, password);
		m_url=url;
	}
	
	public ASECredentials(CredentialsScope scope, String id, String description, String username, String password) {
		super(scope, description, description, username, password);
	}
	
	@Override
	public CredentialsDescriptor getDescriptor() {
		return (DescriptorImpl)super.getDescriptor();
	}
	
	public String getServer() {
		return m_url;
	}
	
	public Secret getToken() {
		return m_token;
	}

	public List<String> getCookies() {
		return m_cookies;
	}

	public void setToken(String connection) {
		m_token = Secret.fromString(connection);
	}

	public void setCookies(List<String> cookies) {
		m_cookies=cookies;
	}
	
	public String getUrl() {
		return m_url;
	}

	@Symbol("ase-credentials") //$NON-NLS-1$
	@Extension
	public static final class DescriptorImpl extends CredentialsDescriptor {
 
		@Override
		public String getDisplayName() {
			return Messages.label_ase();
		}
		
		public FormValidation doCheckUrl(@QueryParameter String url) {
			return FormValidation.validateRequired(url);
		}
		
		public String getApiDocUrl() {
			return "https://help.hcltechsw.com/appscan/Enterprise/9.0.3/topics/t_appscan_enterprise_rest_APIs_list.html";
		}

		public FormValidation doCheckUsername(@QueryParameter String username) {
			if(username.trim().equals("")) //$NON-NLS-1$
				return FormValidation.errorWithMarkup(Messages.need_ase_api_doc(getApiDocUrl())); //$NON-NLS-1$
			return FormValidation.ok();

		}
		
		public FormValidation doCheckPassword(@QueryParameter String password) {
			return FormValidation.validateRequired(password);
		}
	}
}