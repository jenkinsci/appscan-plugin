package com.hcl.appscan.jenkins.plugin.scanTypes;

import hudson.Extension;
import hudson.util.Secret;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.Map;

public class TestOnly implements IScanType {

    private String m_loginTypeTestScan;
    private String m_trafficFileTestScan;
    private String m_userNameTestScan;
    private Secret m_passwordTestScan;
    private String m_exploreDataTestScan;

    @DataBoundConstructor
    public TestOnly() {
        this("Manual", "", "", "", "");
    }

    public TestOnly(String loginTypeTestScan, String trafficFileTestScan, String userNameTestScan, String passwordTestScan, String exploreDataTestScan) {
        m_loginTypeTestScan = loginTypeTestScan;
        m_trafficFileTestScan = trafficFileTestScan;
        m_userNameTestScan = userNameTestScan;
        m_passwordTestScan = Secret.fromString(passwordTestScan);
        m_exploreDataTestScan = exploreDataTestScan;
    }



    @Override
    public void configureScanProperties(Map<String, String> properties) {

    }

    @Symbol("test_only") //$NON-NLS-1$
    @Extension
    public static final class DescriptorImpl extends ScanTypeDescriptor {

        @Override
        public String getDisplayName() {
            return "Test Only";
        }
    }
}
