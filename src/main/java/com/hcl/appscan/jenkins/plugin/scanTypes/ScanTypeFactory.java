package com.hcl.appscan.jenkins.plugin.scanTypes;
import com.hcl.appscan.jenkins.plugin.scanTypes.ScanTypeConstants;

public class ScanTypeFactory {

    public static IScanType getScanTypeImplementation(String scanType) {
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
