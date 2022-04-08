/**
 * © Copyright IBM Corporation 2016.
 * @ Copyright HCL Technologies Ltd. 2019, 2020.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.hcl.appscan.jenkins.plugin.actions;

import com.hcl.appscan.jenkins.plugin.Messages;
import hudson.model.*;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashSet;

import javax.servlet.ServletException;

import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import com.hcl.appscan.sdk.CoreConstants;
import com.hcl.appscan.sdk.results.IResultsProvider;

public class ScanResults extends AppScanAction implements SimpleBuildStep.LastBuildAction {
	private final Run<?,?> m_build;
	private IResultsProvider m_provider;
	private String m_name;
	private String m_status;
	private String m_scanServerUrl;
	private String m_label;
	private int m_totalFindings;
	private int m_highCount;
	private int m_mediumCount;
	private int m_lowCount;
	private int m_infoCount;
	private String m_application;
	private String m_reportsPath;


	private String m_jobId;
	private String m_jobStatus;
	private String m_netScanTime;
	private String m_secEntitiesFound;
	private String m_secEntitiesTested;
	private String m_scanStartTime;
	private String m_pagesFound;
	private String m_pagesScanned;
	private String m_scanningPhase;
	private String m_requestsSent;
	private String m_secIssueVariants;

	public ScanResults(Run<?,?> build, IResultsProvider provider, String name, String status,
					   int totalFindings, int highCount, int mediumCount, int lowCount, int infoCount, String scanServerUrl, String label,String applicationId, String reportsPath,
					   String jobId, String jobStatus, String netScanTime, String secEntitiesFound, String secEntitiesTested, String scanStartTime, String pagesFound,
					   String pagesScanned, String scanningPhase, String requestsSent, String secIssueVariants) {
		super(build.getParent());
		m_build = build;
		m_provider = provider;
		m_name = name;
		m_status = status;
		m_totalFindings = totalFindings;
		m_highCount = highCount;
		m_mediumCount = mediumCount;
		m_lowCount = lowCount;
		m_infoCount = infoCount;
		m_label = label;
		m_scanServerUrl = scanServerUrl;
		m_application = applicationId;
		m_reportsPath = reportsPath;

		m_jobId = jobId;
		m_jobStatus = jobStatus;
		m_netScanTime = netScanTime;
		m_secEntitiesFound = secEntitiesFound;
		m_secEntitiesTested = secEntitiesTested;
		m_scanStartTime = scanStartTime;
		m_pagesFound = pagesFound;
		m_pagesScanned = pagesScanned;
		m_scanningPhase = scanningPhase;
		m_requestsSent = requestsSent;
		m_secIssueVariants = secIssueVariants;
	}

	@DataBoundConstructor
	public ScanResults(Run<?,?> build, IResultsProvider provider, String name, String status,
					   int totalFindings, int highCount, int mediumCount, int lowCount, int infoCount, String scanServerUrl, String label,String applicationId) {
		super(build.getParent());
		m_build = build;
		m_provider = provider;
		m_name = name;
		m_status = status;
		m_totalFindings = totalFindings;
		m_highCount = highCount;
		m_mediumCount = mediumCount;
		m_lowCount = lowCount;
		m_infoCount = infoCount;
		m_label = label;
		m_scanServerUrl = scanServerUrl;
		m_application = applicationId;
	}

	@Override
	public String getUrlName() {
		return getPDFReport();
	}

	public String getXMLReportName(){ return getXMLReport();}
	
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
	public boolean getRunning() {
		String status = m_status == null ? m_provider.getStatus() : m_status;
		return status.equalsIgnoreCase(CoreConstants.RUNNING);
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
		File report = null;
		File path = (m_reportsPath != null && m_reportsPath.length()>0) ? new File(m_reportsPath) : m_build.getRootDir();

		if (request.getParameter("type").equalsIgnoreCase("pdf")) {
			report = new File(path, getPDFReport());
		}
		if (request.getParameter("type").equalsIgnoreCase("xml")) {
			report = new File(path, getXMLReport());
		}

		if(report!= null && report.isFile())
			response.serveFile(request, report.toURI().toURL());
	}

	public File getReport() {
		File path = (m_reportsPath != null && m_reportsPath.length()>0) ? new File(m_reportsPath) : m_build.getRootDir();
		File report = new File(path, getReportName());
		if(!report.isFile())
			m_provider.getResultsFile(report, null);
		return report;
	}

	private String getPDFReport()
	{
		return "Report_"+m_build.getId()+".pdf";
	}

	private String getXMLReport() {
		return "Report_"+m_build.getId()+".xml";
	}

	private String getReportName() {
		return "Report_"+m_build.getId()+".pdf";
	}
	
	private int getLastFindingsCount() {
		if(m_project.getLastSuccessfulBuild() != null && m_project.getLastSuccessfulBuild().getAction(ScanResults.class) != null)
			return m_project.getLastSuccessfulBuild().getAction(ScanResults.class).getTotalFindings();
		return Integer.MAX_VALUE;
	}

	public String getJobId() {
		return m_jobId;
	}

	public String getStatus() {
		if (m_jobStatus.equalsIgnoreCase(CoreConstants.READY))
			return "Completed";
		else
			return m_jobStatus;
	}

	public String getNetScanTime() {
		return m_netScanTime;
	}
	public String getSecEntitiesFound() {
		return m_secEntitiesFound;
	}
	public String getSecEntitiesTested() {
		return m_secEntitiesTested;
	}
	public String getScanStartTime() {
		return m_scanStartTime;
	}
	public String getPagesFound() {
		return m_pagesFound;
	}
	public String getPagesTested() {
		return m_pagesScanned;
	}
	public String getScanningPhase() {
		return m_scanningPhase;
	}
	public String getReqSent() {
		return m_requestsSent;
	}
	public String getSecIssueVariants() {
		return m_secIssueVariants;
	}
}
