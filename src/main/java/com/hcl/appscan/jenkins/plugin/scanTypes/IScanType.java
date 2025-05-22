package com.hcl.appscan.jenkins.plugin.scanTypes;

import java.util.Map;

public interface IScanType {
    void configureScanProperties(Map<String, String> properties);
}
