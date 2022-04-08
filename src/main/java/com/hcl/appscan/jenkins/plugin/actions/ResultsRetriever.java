/**
 * © Copyright IBM Corporation 2016.
 * @ Copyright HCL Technologies Ltd. 2019, 2020.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.hcl.appscan.jenkins.plugin.actions;

import com.hcl.appscan.jenkins.plugin.Messages;
import com.hcl.appscan.jenkins.plugin.util.ExecutorUtil;
import com.hcl.appscan.sdk.CoreConstants;
import com.hcl.appscan.sdk.scanners.ScanConstants;
import hudson.model.Action;
import hudson.model.Run;

import java.io.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Scanner;
import java.util.concurrent.*;

import jenkins.model.RunAction2;
import jenkins.tasks.SimpleBuildStep;

import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;

import com.hcl.appscan.sdk.results.IResultsProvider;

public class ResultsRetriever extends AppScanAction implements RunAction2, SimpleBuildStep.LastBuildAction {

	private final Run<?,?> m_build;
	private IResultsProvider m_provider;
	private String m_name;
	private String m_status;
	private String m_message;
	private String m_scanServerUrl;
	private String m_label;
	transient private Future<Boolean> futureTask = null;
	private Boolean m_resultsAvailable;
	private String m_application;
	private String m_reportsPath;

	private String m_jobId;
	private String m_jobStatus;
	private String m_netScanTime="";
	private String m_secEntitiesFound="";
	private String m_secEntitiesTested="";
	private String m_scanStartTime="";
	private String m_pagesFound="";
	private String m_pagesScanned="";
	private String m_scanningPhase="";
	private String m_requestsSent="";
	private String m_secIssueVariants="";
	private ScheduledExecutorService executorService;


	@DataBoundConstructor
	public ResultsRetriever(Run<?,?> build, IResultsProvider provider, String scanName, String scanServerUrl, String label, String applicationId, String reportsPath) {
		super(build.getParent());
		m_build = build;
		m_provider = provider;
		m_name = scanName;
		m_resultsAvailable = false;
		m_scanServerUrl = scanServerUrl;
		m_label = label;
		m_application = applicationId;
		m_reportsPath = reportsPath;
	}

	@Override
	public String getDisplayName() {
		return Messages.label_running(m_name);
	}

	@Override
	public String getUrlName() {
		return null;
	}

	@Override
	public void onAttached(Run<?, ?> r) {
	}

	@Override
	public void onLoad(Run<?, ?> r) {
		checkResults(r);
	}
	
	@Override
	public Collection<? extends Action> getProjectActions() {
		HashSet<Action> actions = new HashSet<Action>();
		actions.add(new ScanResultsTrend(m_build, m_provider.getType(), m_name));
		return actions;
	}
	
	public boolean getHasResults() {
		return checkResults(m_build);
	}

	public boolean getFailed() {
		return CoreConstants.FAILED.equalsIgnoreCase(m_status);
	}

	public String getJobId() {
		return m_jobId;
	}
	public String getStatus() {
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

	public String getMessage() {
		return m_message;
	}

	public String getScanType() {
		if (m_provider == null) return "";
		return m_provider.getType();
	}

	public File getReport() {
		File path = (m_reportsPath != null && m_reportsPath.length()>0) ? new File(m_reportsPath) : m_build.getRootDir();
		File report = new File(path, "statistics_"+m_build.getId()+".json");
		if(!report.isFile())
			m_provider.getResultsFile(report, "json");
		return report;
	}

	public JSONObject getStatistics()
	{
		JSONObject statsObject = m_provider.getStatistics();

		if (statsObject == null)
		{
			File path = (m_reportsPath != null && m_reportsPath.length()>0) ? new File(m_reportsPath) : m_build.getRootDir();
			File statReport = new File(path, "statistics_"+m_build.getId()+".json");
			if(statReport.exists())
			{
				try
				{
					String myJson = new Scanner(statReport).useDelimiter("\\Z").next();
					if (myJson != null && myJson.length()>0)
						statsObject = new JSONObject(myJson);
				}
				catch (IOException | JSONException e)
				{
					e.printStackTrace();
				}
			}
		}
		return statsObject;
	}

	private void populateStatsData()
	{
		JSONObject statsObject = getStatistics();
		if (statsObject != null)
		{
			try {
				m_jobId = statsObject.getString("baseJob");
				m_jobStatus = statsObject.getString("status");
				m_netScanTime = statsObject.getString("net-scan-time");
				m_secEntitiesFound = statsObject.getString("security-entities-found");
				m_secEntitiesTested = statsObject.getString("security-entities-tested");
				m_scanStartTime = statsObject.getString("run-start");
				m_pagesFound = statsObject.getString("pages-found");
				m_pagesScanned = statsObject.getString("pages-scanned");
				m_scanningPhase = statsObject.getString("scanning-phase");
				m_requestsSent = statsObject.getString("requests-sent");
				m_secIssueVariants = statsObject.getString("security-issue-variants");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean checkResults(Run<?,?> r)
	{
		if (m_resultsAvailable != null && m_resultsAvailable)
			return true;

		if (futureTask != null && futureTask.isDone())
		{
			try {
				m_resultsAvailable = futureTask.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				//e.printStackTrace();
			} catch (java.util.NoSuchElementException e){
				e.printStackTrace();
			}

		}
		else if (futureTask != null)
			return false;

		if (!m_resultsAvailable)
		{
			final Run<?,?> rTemp  = r;

			Callable<Boolean> callableTask = new Callable<Boolean>()
			{
				@Override
				public Boolean call() {

					populateStatsData();

					ResultsRetriever.this.m_status = m_provider.getStatus();
					ResultsRetriever.this.m_message = m_provider.getMessage();


					if (rTemp.getAllActions().contains(ResultsRetriever.this) && CoreConstants.FAILED.equalsIgnoreCase(ResultsRetriever.this.m_jobStatus)) {
						String message = com.hcl.appscan.sdk.Messages.getMessage(ScanConstants.SCAN_FAILED, " Scan Name: " + m_name);
						if (m_provider.getMessage() != null && m_provider.getMessage().trim().length() > 0)
							message += ", " + m_provider.getMessage();
						ResultsRetriever.this.m_message = message;
						return true;
					}
					else if (rTemp.getAllActions().contains(ResultsRetriever.this) && m_provider.hasResults()) {
						m_resultsAvailable = true;
						try
						{
							rTemp.getActions().remove(ResultsRetriever.this);
						}
						catch (Exception e)
						{
							System.out.println(e.getMessage());
						}

						rTemp.addAction(createResults());
						try
						{
							rTemp.save();
						}
						catch (Exception e)
						{
							System.out.println(e.getMessage());
						}
						return true;
					}
					return false;
				}
			};

			futureTask = (Future<Boolean>) ExecutorUtil.submitTask(callableTask);
		}

		return m_resultsAvailable;
	}

	private ScanResults createResults() {
		return new ScanResults(
			m_build,
			m_provider,
			m_name,
			m_provider.getStatus(),
			m_provider.getFindingsCount(),
			m_provider.getHighCount(),
			m_provider.getMediumCount(),
			m_provider.getLowCount(),
			m_provider.getInfoCount(),
			m_scanServerUrl,
			m_label,
			m_application,
			m_reportsPath,

			m_jobId,
			m_jobStatus,
			m_netScanTime,
			m_secEntitiesFound,
			m_secEntitiesTested,
			m_scanStartTime,
			m_pagesFound,
			m_pagesScanned,
			m_scanningPhase,
			m_requestsSent,
			m_secIssueVariants
			);
	}
}
