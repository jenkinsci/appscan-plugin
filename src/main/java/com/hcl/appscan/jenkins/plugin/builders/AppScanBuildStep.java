/**
 * @ Copyright IBM Corporation 2016.
 * @ Copyright HCL Technologies Ltd. 2017,2019.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.hcl.appscan.jenkins.plugin.builders;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import com.hcl.appscan.jenkins.plugin.Messages;
import com.hcl.appscan.jenkins.plugin.results.FailureCondition;
import com.hcl.appscan.jenkins.plugin.scanners.Scanner;
import com.hcl.appscan.jenkins.plugin.scanners.ScannerFactory;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Items;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Builder;
import jenkins.tasks.SimpleBuildStep;

public class AppScanBuildStep extends Builder implements SimpleBuildStep, Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Scanner m_scanner;
	private String m_name;
	private String m_type;
	private String m_target;
	private String m_application;
	private String m_credentials;
	private List<FailureCondition> m_failureConditions;
	private boolean m_emailNotification;
	private boolean m_wait;
    private boolean m_failBuildNonCompliance;
	private boolean m_failBuild;
	
	@Deprecated
	public AppScanBuildStep(Scanner scanner, String name, String type, String target, String application, String credentials, List<FailureCondition> failureConditions, boolean failBuildNonCompliance, boolean failBuild, boolean wait, boolean email) {
		m_scanner = scanner;
		m_name = (name == null || name.trim().equals("")) ? application.replaceAll(" ", "") + ThreadLocalRandom.current().nextInt(0, 10000) : name; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		m_type = scanner.getType();
		m_target = target;
		m_application = application;
		m_credentials = credentials;
		m_failureConditions = failureConditions;
		m_emailNotification = email;
		m_wait = wait;
        m_failBuildNonCompliance=failBuildNonCompliance;
		m_failBuild = failBuild;
        }
	
	@DataBoundConstructor
	public AppScanBuildStep(Scanner scanner, String name, String type, String application, String credentials) {
		m_scanner = scanner;
		m_name = (name == null || name.trim().equals("")) ? application.replaceAll(" ", "") + ThreadLocalRandom.current().nextInt(0, 10000) : name; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		m_type = scanner.getType();
		m_target = "";
		m_application = application;
		m_credentials = credentials;
		m_emailNotification = false;
		m_wait = false;
        m_failBuildNonCompliance=false;
		m_failBuild = false;
	}
	
	public Scanner getScanner() {
		return m_scanner;
	}
	
	public String getName() {
		return m_name;
	}
	
	public String getType() {
		return m_type;
	}
	
	@DataBoundSetter
	public void setTarget(String target) {
		m_target = target;
	}
	
	public String getTarget() {
		return m_target;
	}
	
	public String getApplication() {
		return m_application;
	}
	
	public String getCredentials() {
		return m_credentials;
	}

	@DataBoundSetter
	public void setFailureConditions(List<FailureCondition> failureConditions) {
		m_failureConditions = failureConditions;
	}
	
	public List<FailureCondition> getFailureConditions() {
		if(m_failureConditions == null)
			return new ArrayList<FailureCondition>();
		return m_failureConditions;
	}
	
	@DataBoundSetter
	public void setFailBuild(boolean failBuild) {
		m_failBuild = failBuild;
	}
	
	public boolean getFailBuild() {
		return m_failBuild;
	}
	
	@DataBoundSetter
	public void setWait(boolean wait) {
		m_wait = wait;
	}
	
	public boolean getWait() {
		return m_wait;
	}
        
    @DataBoundSetter
    public void setFailBuildNonCompliance(boolean failBuildNonCompliance){
            m_failBuildNonCompliance=failBuildNonCompliance;
    }
        
    public boolean getFailBuildNonCompliance(){
        return m_failBuildNonCompliance;
    }

	@DataBoundSetter
	public void setEmail(boolean emailNotification) {
		m_emailNotification = emailNotification;
	}
	
	public boolean getEmail() {
		return m_emailNotification;
	}
	
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }
    
    @Override
    public boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
		return true;
    }
    
	@Override
	public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath workspace, @Nonnull Launcher launcher, @Nonnull TaskListener listener) throws InterruptedException, IOException {
	}
    
    @Override
    public BuildStepMonitor getRequiredMonitorService() {
    	return BuildStepMonitor.NONE;
    }
    
    //To retain backward compatibility
    protected Object readResolve() {
    	if(m_scanner == null && m_type != null)
    		m_scanner = ScannerFactory.getScanner(m_type, m_target);
    	return this;
    }
    
    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Builder> {
    	
	    	//Retain backward compatibility
	    	@Initializer(before = InitMilestone.PLUGINS_STARTED)
	    	public static void createAliases() {
			Items.XSTREAM2.addCompatibilityAlias("com.ibm.appscan.jenkins.plugin.builders.AppScanBuildStep", com.hcl.appscan.jenkins.plugin.builders.AppScanBuildStep.class);
	    		Items.XSTREAM2.addCompatibilityAlias("com.hcl.appscan.plugin.core.results.CloudResultsProvider", com.hcl.appscan.sdk.results.CloudResultsProvider.class);
			Items.XSTREAM2.addCompatibilityAlias("com.hcl.appscan.plugin.core.scan.CloudScanServiceProvider", com.hcl.appscan.sdk.scan.CloudScanServiceProvider.class);
	    	}
    	
    		@Override
        public boolean isApplicable(Class<? extends AbstractProject> projectType) {
    			return false;
        }

    		@Override
        public String getDisplayName() {
            	return "Deprecated: " + Messages.label_build_step();
        }
    }
}

