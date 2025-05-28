package com.hcl.appscan.jenkins.plugin.scanModes;

public class ScanModeFactory {

    public static ScanMode getScanTypeImplementation(String scanType, String loginType, String userName, String password, String trafficFile, String exploreData) {
        switch (scanType) {
            case "1":
                return new FullScan(loginType, userName, password, trafficFile, exploreData);
            case "3":
                return new TestOnly(loginType, userName, password, trafficFile, exploreData);
            case "4":
                return new PostmanCollection();
            default:
                throw new IllegalArgumentException("Unsupported scan type: " + scanType);
        }
    }
}
