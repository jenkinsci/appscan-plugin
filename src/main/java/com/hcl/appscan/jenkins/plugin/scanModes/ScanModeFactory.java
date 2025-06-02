/**
 * @ Copyright HCL Technologies Ltd. 2025.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */


package com.hcl.appscan.jenkins.plugin.scanModes;

import com.hcl.appscan.sdk.scan.ASEScanType;

public class ScanModeFactory {

    public static ScanMode getScanTypeMode(String scanType, String loginType, String userName, String password, String trafficFile, String exploreData) {
        String scanTypeMode = ASEScanType.scanTypeName(scanType);
        switch (scanTypeMode) {
            case ScanModeConstants.FULL_SCAN:
                return new FullScan(loginType, userName, password, trafficFile, exploreData);
            case ScanModeConstants.TEST_ONLY:
                return new TestOnly(loginType, userName, password, trafficFile, exploreData);
            case ScanModeConstants.POSTMAN_COLLECTION:
                return new PostmanCollection();
            default:
                throw new IllegalArgumentException("Unsupported scan type: " + scanType);
        }
    }
}
