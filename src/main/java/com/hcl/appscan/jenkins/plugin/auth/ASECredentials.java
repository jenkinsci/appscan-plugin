/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hcl.appscan.jenkins.plugin.auth;

import com.cloudbees.plugins.credentials.CredentialsDescriptor;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import com.hcl.appscan.sdk.CoreConstants;
import com.hcl.appscan.sdk.utils.SystemUtil;
import com.hcl.appscan.jenkins.plugin.Messages;
import hudson.Extension;
import hudson.util.FormValidation;
import hudson.util.Secret;
import java.util.List;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

/**
 *
 * @author anurag-s
 */
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
        
        public List<String> getCookies(){
            return m_cookies;
        }

        public void setToken(String connection) {
		m_token = Secret.fromString(connection);
	}
        
        public void setCookies(List<String> cookies){
            m_cookies=cookies;
        }
        @DataBoundSetter
        public void setUrl(String url){
            m_url=url;
        }
	
	@Symbol("ase-credentials") //$NON-NLS-1$
    @Extension
    public static final class DescriptorImpl extends CredentialsDescriptor {
    	
		@Override
		public String getDisplayName() {
			return Messages.label_ase();
		}
		
		/*public String getApiKeyUrl() {
			return SystemUtil.getDefaultServer() + CoreConstants.API_KEY_PATH;
		}*/

		public FormValidation doCheckUsername(@QueryParameter String username) {
			return FormValidation.validateRequired(username);
		}
		
		public FormValidation doCheckPassword(@QueryParameter String password) {
			return FormValidation.validateRequired(password);
		}
                /*
                public FormValidation doCheckUrl(@QueryParameter String url) {
			return FormValidation.validateRequired(url);
		}*/
    }
}

