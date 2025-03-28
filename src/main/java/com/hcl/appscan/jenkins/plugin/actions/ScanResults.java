/**
 * © Copyright IBM Corporation 2016.
 * @ Copyright HCL Technologies Ltd. 2019, 2024, 2025.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.hcl.appscan.jenkins.plugin.actions;

import com.hcl.appscan.sdk.utils.FileUtil;
import hudson.model.Action;
import hudson.model.Run;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashSet;

import javax.servlet.ServletException;

import jenkins.tasks.SimpleBuildStep;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import com.hcl.appscan.sdk.CoreConstants;
import com.hcl.appscan.sdk.results.IResultsProvider;
import com.hcl.appscan.jenkins.plugin.Messages;

public class ScanResults extends AppScanAction implements SimpleBuildStep.LastBuildAction {

	private static final String REPORT_SUFFIX = "_report"; //$NON-NLS-1$
	
	private final Run<?,?> m_build;	
	private IResultsProvider m_provider;
	private String m_name;
	private String m_status;
	private String m_scanServerUrl;
	private String m_label;
	private int m_totalFindings;
    	private int m_criticalCount;
	private int m_highCount;
	private int m_mediumCount;
	private int m_lowCount;
	private int m_infoCount;
	
	@DataBoundConstructor
	public ScanResults(Run<?,?> build, IResultsProvider provider, String name, String status,
			int totalFindings, int criticalCount, int highCount, int mediumCount, int lowCount, int infoCount, String scanServerUrl, String label) {
		super(build.getParent());
		m_build = build;
		m_provider = provider;
		m_name = name;
		m_status = status;
		m_totalFindings = totalFindings;
                m_criticalCount = criticalCount;
		m_highCount = highCount;
		m_mediumCount = mediumCount;
		m_lowCount = lowCount;
		m_infoCount = infoCount;
		m_label = label;
		m_scanServerUrl = scanServerUrl;
                getReport();
	}

	public ScanResults(Run<?,?> build, IResultsProvider provider, String name, String serverUrl, String label) {
		this(build, provider, name, provider.getStatus(), provider.getFindingsCount(), provider.getCriticalCount(), provider.getHighCount(), provider.getMediumCount(), provider.getLowCount(), provider.getInfoCount(), serverUrl, label);
	}
	
	@Override
	public String getUrlName() {
		return getReportName();
	}
	
	@Override
	public String getDisplayName() {
		return Messages.label_results(m_name);
	}
	
	@Override
	public Collection<? extends Action> getProjectActions() {
		HashSet<Action> actions = new HashSet<Action>();
		actions.add(new ScanResultsTrend(m_build, m_provider.getType(), m_name));
		return actions;
	}
	
	public Run<?,?> getBuild() {
		return m_build;
	}
	
	public String getName() {
		return m_name;
	}
	
	public String getScanType() {
		return m_provider.getType();
	}
	
	public int getHighCount() {
		return m_highCount;
	}
	
	public int getMediumCount() {
		return m_mediumCount;
	}
	
	public int getLowCount() {
		return m_lowCount;
	}

	public int getCriticalCount(){
                return m_criticalCount;
        }
	public int getInfoCount() {
		return m_infoCount;
	}
	
	public int getTotalFindings() {
		return m_totalFindings;
	}
	
	public boolean getHasResults() {
		return !m_status.equalsIgnoreCase(CoreConstants.FAILED);
	}
	
	public boolean getFailed() {
		String status = m_status == null ? m_provider.getStatus() : m_status;
		return status.equalsIgnoreCase(CoreConstants.FAILED);
	}

	public String getScanServerUrl()  {
		return m_scanServerUrl;
	}

	public String getLabel() {
		return m_label;
	}
	
	public boolean isBetterThanLast() {
		return getTotalFindings() < getLastFindingsCount();
	}
	
	public void doDynamic(StaplerRequest request, StaplerResponse response) throws MalformedURLException, ServletException, IOException {
		File report = getReport();
		if(report.isFile())
			response.serveFile(request, report.toURI().toURL());
	}
	
	public File getReport() {
		File report = new File(m_build.getRootDir(), getReportName());
		if(!report.isFile())
			m_provider.getResultsFile(report, null);
		return report;
	}
	
	private String getReportName() {
		String name = FileUtil.getValidFilename(getName()).replaceAll(" ", ""); //$NON-NLS-1$ //$NON-NLS-2$
		return name + REPORT_SUFFIX + "." + m_provider.getResultsFormat().toLowerCase(); //$NON-NLS-1$
	}
	
	private int getLastFindingsCount() {
		if(m_project.getLastSuccessfulBuild() != null && m_project.getLastSuccessfulBuild().getAction(ScanResults.class) != null)
			return m_project.getLastSuccessfulBuild().getAction(ScanResults.class).getTotalFindings();
		return Integer.MAX_VALUE;
	}
}
