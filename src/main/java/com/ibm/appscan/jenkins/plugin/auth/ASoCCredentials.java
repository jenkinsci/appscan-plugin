/**
 * © Copyright IBM Corporation 2016.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.ibm.appscan.jenkins.plugin.auth;

import hudson.Extension;
import hudson.util.FormValidation;
import hudson.util.Secret;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import com.cloudbees.plugins.credentials.CredentialsDescriptor;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import com.ibm.appscan.jenkins.plugin.Messages;
import com.ibm.appscan.plugin.core.utils.SystemUtil;

public class ASoCCredentials extends UsernamePasswordCredentialsImpl {

	private static final long serialVersionUID = 1L;
	private Secret m_token;
	
	@DataBoundConstructor
	public ASoCCredentials(String id, String description, String username, String password) {
		this(CredentialsScope.GLOBAL, id, description, username, password);
	}
	
	public ASoCCredentials(CredentialsScope scope, String id, String description, String username, String password) {
		super(scope, id, description, username, password);
	}
	
	@Override
	public CredentialsDescriptor getDescriptor() {
		return (DescriptorImpl)super.getDescriptor();
	}
	
	public String getServer() {
		return SystemUtil.getDefaultServer();
	}
	
	public Secret getToken() {
		return m_token;
	}

	public void setToken(String connection) {
		m_token = Secret.fromString(connection);
	}
	
    @Extension
    public static final class DescriptorImpl extends CredentialsDescriptor {
    	
		@Override
		public String getDisplayName() {
			return Messages.label_asoc();
		}

		public FormValidation doCheckUsername(@QueryParameter String username) {
			return FormValidation.ok();
		}
		
		public FormValidation doCheckPassword(@QueryParameter String password) {
			return FormValidation.ok();
		}
    }
}
