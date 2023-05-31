/* 
 * @ Copyright HCL Technologies Ltd. 2019, 2023.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
 
package com.ibm.appscan.jenkins.plugin.auth;

import com.cloudbees.plugins.credentials.CredentialsScope;

// Added for backward compatibility during HCL wash
public class ASoCCredentials extends com.hcl.appscan.jenkins.plugin.auth.ASoCCredentials {

	private static final long serialVersionUID = 1L;

	public ASoCCredentials(CredentialsScope scope, String id, String description, String username, String password, Boolean acceptInvalidCerts) {
		super(scope, id, description, username, password, acceptInvalidCerts);
	}
}