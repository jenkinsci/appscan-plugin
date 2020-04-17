package com.hcl.appscan.jenkins.plugin.scanners;

import com.hcl.appscan.sdk.CoreConstants;

public interface ScannerConstants {
	
	String EMPTY					= "";					//$NON-NLS-1$
	String ENABLE_MAIL_NOTIFICATION			= "EnableMailNotification";		//$NON-NLS-1$
	String EXTRA_FIELD				= "ExtraField";				//$NON-NLS-1$
	String INCLUDE_VERIFIED_DOMAINS			= "IncludeVerifiedDomains";		//$NON-NLS-1$
	String LOGIN_USER				= "LoginUser";				//$NON-NLS-1$
	String LOGIN_PASSWORD				= "LoginPassword";			//$NON-NLS-1$
	String PRESENCE_ID				= "PresenceId";				//$NON-NLS-1$
	String SCAN_FILE				= "ScanFile";				//$NON-NLS-1$
	String SCAN_TYPE				= "ScanType";				//$NON-NLS-1$
	String TEST_POLICY				= "TestPolicy";				//$NON-NLS-1$
	String TARGET					= CoreConstants.TARGET;			//$NON-NLS-1$
	String TEMPLATE_EXTENSION			= ".scant";				//$NON-NLS-1$
	String TEMPLATE_EXTENSION2			= ".scan";				//$NON-NLS-1$
	
	String DYNAMIC_ANALYZER				= "Dynamic Analyzer";			//$NON-NLS-1$
	String MOBILE_ANALYZER				= "Mobile Analyzer";			//$NON-NLS-1$
	String STATIC_ANALYZER				= "Static Analyzer";			//$NON-NLS-1$

	String CUSTOM					= "Custom";				//$NON-NLS-1$
	String PRODUCTION				= "Production";				//$NON-NLS-1$
	String STAGING					= "Staging";				//$NON-NLS-1$
	
	String NORMAL					= "Normal";				//$NON-NLS-1$
	String OPTIMIZED				= "Optimized";				//$NON-NLS-1$
	String FAST                     = "Fast";               //$NON-NLS-1$
	String FASTER                   = "Faster";               //$NON-NLS-1$
	String FASTEST                  = "Fastest";               //$NON-NLS-1$
	String NO_OPTIMIZATION          = "NoOptimization";               //$NON-NLS-1$
	String TEST_OPTIMIZATION_LEVEL  = "TestOptimizationLevel";  //$NON-NLS-1$
}
