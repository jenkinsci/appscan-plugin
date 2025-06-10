/**
 * @ Copyright HCL Technologies Ltd. 2025.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */


package com.hcl.appscan.jenkins.plugin.scanModes;

public interface ScanModeConstants {


    String FULL_SCAN                        = "Full Scan";                      //$NON-NLS-1$
    String TEST_ONLY                        = "Test Only";                      //$NON-NLS-1$
    String POSTMAN_COLLECTION               = "Postman Collection";             //$NON-NLS-1$
    String SCAN_TYPE                        = "scanType";                       //$NON-NLS-1$
    String LOGIN_TYPE                       = "loginType";                      //$NON-NLS-1$
    String MANUAL                           = "Manual";                         //$NON-NLS-1$
    String AUTOMATIC                        = "Automatic";                      //$NON-NLS-1$
    String USER_NAME                        = "userName";                       //$NON-NLS-1$
    String PASSWORD                         = "password";                       //$NON-NLS-1$
    String TRAFFIC_FILE                     = "trafficFile";                    //$NON-NLS-1$
    String EXPLORE_DATA                     = "exploreData";                    //$NON-NLS-1$
    String POSTMAN_COLLECTION_FILE          = "postmanCollectionFile";          //$NON-NLS-1$
    String ADDITIONAL_DOMAINS               = "additionalDomains";              //$NON-NLS-1$
    String ENVIRONMENTAL_VARIABLES_FILE     = "environmentalVariablesFile";     //$NON-NLS-1$
    String GLOBAL_VARIABLES_FILE            = "globalVariablesFile";            //$NON-NLS-1$
    String ADDITIONAL_FILES                 = "additionalFiles";                //$NON-NLS-1$
}
