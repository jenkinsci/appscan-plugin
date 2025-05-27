package com.hcl.appscan.jenkins.plugin.scanModes;

public class ScanModeFactory {

    public static ScanMode getScanTypeImplementation(String scanType, String loginType, String username, String password, String trafficFile, String exploreData) {
        switch (scanType) {
            case "1":
                return new FullScan(scanType, loginType, username, password, trafficFile, exploreData);
            case "3":
                return new TestOnly(scanType, loginType, username, password, trafficFile, exploreData);
            case "4":
                return new PostmanCollection();
            default:
                throw new IllegalArgumentException("Unsupported scan type: " + scanType);
        }
    }
}
