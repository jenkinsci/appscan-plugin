package com.hcl.appscan.jenkins.plugin.scanTypes;

import hudson.Extension;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.util.Map;

public class PostmanCollection implements IScanType{

    private String m_postmanCollectionFile;
    private String m_additionalDomains;
    private String m_environmentalVariablesFile;
    private String m_globalVariablesFile;
    private String m_additionalFiles;

    @DataBoundConstructor
    public PostmanCollection() {
        this("", "", "", "", "");
    }

    public PostmanCollection(String postmanCollectionFile, String additionalDomains, String environmentalVariablesFile, String globalVariablesFile, String additionaFiles) {
        m_postmanCollectionFile = postmanCollectionFile;
        m_additionalDomains = additionalDomains;
        m_environmentalVariablesFile = environmentalVariablesFile;
        m_globalVariablesFile = globalVariablesFile;
        m_additionalFiles = additionaFiles;
    }

    @DataBoundSetter
    public void setPostmanCollectionFile(String postmanCollectionFile) {
        m_postmanCollectionFile = postmanCollectionFile;
    }

    public String getPostmanCollectionFile() {
        return m_postmanCollectionFile;
    }

    @DataBoundSetter
    public void setAdditionalDomains(String additionalDomains) {
        m_additionalDomains = additionalDomains;
    }

    public String getAdditionalDomains() {
        return m_additionalDomains;
    }

    @DataBoundSetter
    public void setEnvironmentalVariablesFile(String environmentalVariablesFile) {
        m_environmentalVariablesFile = environmentalVariablesFile;
    }

    public String getEnvironmentalVariablesFile() {
        return m_environmentalVariablesFile;
    }

    @DataBoundSetter
    public void setGlobalVariablesFile(String globalVariablesFile) {
        m_globalVariablesFile = globalVariablesFile;
    }

    public String getGlobalVariablesFile() {
        return m_globalVariablesFile;
    }

    @DataBoundSetter
    public void setAdditionalFiles(String additionalFiles) {
        m_additionalFiles = additionalFiles;
    }

    public String getAdditionalFiles() {
        return m_additionalFiles;
    }

    public void configureScanProperties(Map<String, String> properties) {
        properties.put("scanMode", "postmanCollection");
        properties.put("postmanCollectionFile", m_postmanCollectionFile);
        properties.put("additionalDomains", m_additionalDomains);
        properties.put("environmentalVariablesFile", m_environmentalVariablesFile);
        properties.put("globalVariablesFile", m_globalVariablesFile);
        properties.put("additionalFiles", m_additionalFiles);
    }

    @Symbol("postman_collection") //$NON-NLS-1$
    @Extension
    public static final class DescriptorImpl extends ScanTypeDescriptor {

        @Override
        public String getDisplayName() {
            return "Postman Collection";
        }
    }
}
