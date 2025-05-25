package com.hcl.appscan.jenkins.plugin.scanModes;

public class ScanModeFactory {

    public static ScanMode getScanTypeImplementation(String scanType) {
        switch (scanType) {
            case "FullScan":
                return new FullScan();
            case "TestOnly":
                return new TestOnly();
            case "PostmanCollection":
                return new PostmanCollection();
            default:
                throw new IllegalArgumentException("Unsupported scan type: " + scanType);
        }
    }
}
