/* 
 * @ Copyright HCL Technologies Ltd. 2019.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.ibm.appscan.jenkins.plugin.auth;

import hudson.model.ItemGroup;

@edu.umd.cs.findbugs.annotations.SuppressFBWarnings("NM_SAME_SIMPLE_NAME_AS_SUPERCLASS")
public class JenkinsAuthenticationProvider extends com.hcl.appscan.jenkins.plugin.auth.JenkinsAuthenticationProvider {

	private static final long serialVersionUID = 1L;

	public JenkinsAuthenticationProvider(String id, ItemGroup<?> context) {
		super(id, context);		
	}
}