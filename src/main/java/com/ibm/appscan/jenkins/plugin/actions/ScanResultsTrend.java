/**
 * © Copyright IBM Corporation 2016.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.ibm.appscan.jenkins.plugin.actions;

import hudson.model.AbstractProject;
import hudson.model.Run;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.servlet.ServletException;

import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import com.ibm.appscan.jenkins.plugin.Messages;

public class ScanResultsTrend extends AppScanAction {

	private String m_type;
	private String m_name;
	
	@DataBoundConstructor
	public ScanResultsTrend(AbstractProject<?, ?> project, String type, String name) {
		super(project);
		m_type = type;
		m_name = name;
	}
	
	@Override
	public String getIconFileName() {
		if(getBuildCount() == 0 || getLatestResults() == null)
			return null;
		return super.getIconFileName();
	}
	
	@Override
	public String getUrlName() {
		ScanResults results = getLatestResults();
		if(results != null)
			return results.getUrlName();
		return null;
	}
	
	@Override
	public String getDisplayName() {
		return Messages.label_latest_report(m_name);
	}
	
	/**
	 * Gets the name to use for the trend chart.
	 * 
	 * @return
	 */
	public String getChartName() {
		return Messages.label_results_trend();		
	}
	
	/**
	 * Gets the number of builds that have {@link ScanResults}.
	 * 
	 * @return
	 */
	public int getBuildCount() {
		int count = 0;
		for(Run run : m_project.getBuilds()) {
			for(ResultsRetriever retriever : run.getActions(ResultsRetriever.class))
				retriever.checkResults(run);
	
			for(ScanResults results : run.getActions(ScanResults.class)) {
				if(results.getName().equals(m_name) && results.getScanType().equals(m_type))
					count++;
			}
		}
		return count;
	}
	
	/** Gets a JSONObject containing all scans and their results.
	 * 
	 * @return
	 */
	public JSONObject getBuildFindingCounts() {
		JSONObject builds = new JSONObject();
		
		//Loop through the builds to find those with ScanResults.
		for(Run run : m_project.getBuilds()) {
			
			//If there are any active ResultsRetriever's, get the ScanResults.
			for(ResultsRetriever retriever : run.getActions(ResultsRetriever.class)) {
				retriever.checkResults(run);
			}
			
			//Loop through the ScanResults to get each set.
			for(ScanResults results : run.getActions(ScanResults.class)) {
				String scanType = results.getScanType() + "-" + results.getName();
				
				try {
					if(builds.containsKey(scanType)) {
						JSONObject scan = (JSONObject)builds.get(scanType);
						scan.put(Integer.toString(run.number), results.getTotalFindings());
					}
					else {
						JSONObject build = new JSONObject();
						build.put(Integer.toString(run.number), results.getTotalFindings());
						builds.put(scanType, build);
					}
				}
				catch (JSONException e) {
					//Ignore and move on.
				}
			}
		}
		return builds;
	}
	
	public void doDynamic(StaplerRequest request, StaplerResponse response) throws MalformedURLException, ServletException, IOException {
		ScanResults results = getLatestResults();
		if(results == null)
			return;
		
		File report = results.getReport();
		if(report.isFile())
			response.serveFile(request, report.toURI().toURL());
	}
	
	private ScanResults getLatestResults() {
		for(Run run : m_project.getBuilds()) {
			for(ScanResults results : run.getActions(ScanResults.class)) {
				if(results.getScanType().equalsIgnoreCase(m_type) && results.getName().equalsIgnoreCase(m_name) && results.getHasResults()) {
					return results;
				}
			}
		}
		return null;
	}
}