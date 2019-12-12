/* 
 * @ Copyright HCL Technologies Ltd. 2019.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.ibm.appscan.jenkins.plugin.auth;

import hudson.model.ItemGroup;

public class JenkinsAuthenticationProvider extends com.hcl.appscan.jenkins.plugin.auth.JenkinsAuthenticationProvider {

	private static final long serialVersionUID = 1L;

	public JenkinsAuthenticationProvider(String id, ItemGroup<?> context) {
		super(id, context);		
	}
}