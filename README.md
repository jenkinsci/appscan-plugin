# HCL AppScan Jenkins plugin

[![AppScan](img/AppScan.svg)](https://cloud.appscan.com)

Easily integrate security testing into your Jenkins builds using the HCL AppScan Jenkins plug-in. This plug-in enables you to execute SAST (Static Application Security Testing) scans and SCA (Software Composition Analysis) scans using HCL AppScan on Cloud and HCL AppScan 360°, and DAST (Dynamic Application Security Testing) scans using HCL AppScan on Cloud (ASoC), HCL AppScan 360° and HCL AppScan Enterprise (ASE).

## Prerequisites

The plugin supports scanning through following HCL AppScan products:
 - HCL AppScan on Cloud
 - HCL AppScan 360°
 - HCL AppScan Enterprise

**HCL AppScan on Cloud prerequisites**

An account at the [HCL AppScan on
Cloud](https://cloud.appscan.com/AsoCUI/serviceui/home)
service. [Create an
application](https://help.hcl-software.com/appscan/ASoC/ent_create_application.html?query=create)
on the service to associate with your scans.

**HCL AppScan 360° prerequisites**

To execute scans in HCL AppScan 360°, you must have access to an instance of AppScan 360°. To learn more about AppScan 360° features and installation, click [here](https://help.hcl-software.com/appscan/360/1.0/appseccloud_jenkins.html).

**HCL AppScan Enterprise prerequisites**

HCL AppScan Jenkins plug-in supports integration with HCL AppScan Enterprise for creation and execution of ADAC jobs. To use this integration, you must have access to a running instance of AppScan Enterprise Server  version 9.0.3.14 or later. Please note that Content Scan jobs are not supported through this integration.

## Usage
**Integration with HCL AppScan on Cloud/HCL AppScan 360°**

1.  Add your AppScan on Cloud/AppScan 360° credentials on the Jenkins
    **Credentials** page.
    -   From the main Jenkins dashboard, click **Credentials**.
    -   Add new global credentials.
    -   In the **Kind** drop-down list, select **HCL AppScan on Cloud/HCL AppScan 360° Credentials**.
    -   Enter your API key details.
    -   Check **Allow Untrusted Connections** to enable untrusted connection to AppScan 360° service.
2.  Add a **Run AppScan on Cloud/AppScan 360° Security Test** build step to your Jenkins project
    configuration and enter the following information:
    -   **Credentials:** Select the credentials you added to Jenkins in
        step 1 above.
    -   **Application:** Select the application to associate with the scan.<br>
        **Note**: At least one application must be created in HCL AppScan on Cloud or HCL AppScan 360°
        for the dropdown to show options. If it’s empty, check your server connection or ensure
        applications exist in the organization.
    -   **Test Name:** Specify a name to use for the scan. This value
        is used to distinguish this scan and its results from
        others.
    -   **Test Type:** Select the type of scan to run from the available
        options.
        -   **Dynamic Analysis (DAST):**
     	    - **Rescan**:  Select this option to rescan the same application, updating and overwriting the previous scan results with the latest findings.<br>
	      **Note**: If you are looking to use the Auto Close feature, ensure it has been enabled by your AppScan on Cloud/AppScan 360° organization administrator. Learn more about [rescanning](https://help.hcl-software.com/appscan/ASoC/appseccloud_scanning_rescan_cm.html).
              - **Scan ID**: Enter the Scan ID of the parent scan based on the application and technology you selected earlier. You can retrieve Scan ID from the AppScan on Cloud/AppScan 360° Server.<br>
              - **Incremental Scan**: An incremental scan saves time by examining only the changed parts of your application. It uses a base scan for comparison and scans only the new data. [ Learn more](https://help.hcl-software.com/appscan/ASoC/Incremental.html).<br>
	        	- **Base Scan**: Select a base scan from the dropdown. Scans are imported from ASoC and listed with their date and time.
            -   **Starting URL**: Enter the URL from where you want the
                scan to start exploring the site.
            -   **Additional Options**: If selected, the following
                options are available:
                -   **Test Optimization**: Following options are available:
                    - **Fast**: Select this option for approximately 97% issue coverage and twice as fast test stage speed.
                                  Recommended for security experts for more frequent scans.
                    - **Faster**: Select this option for approximately 85% issue coverage and five times as fast test stage speed.
                                  Recommended for DevSecOps during ongoing evaluation.
                    - **Fastest**: Select this option for approximately 70% issue coverage and ten times as fast test stage speed.
                                  Recommended for Dev and QA during initial evaluation.
                    - **No Optimization**: Select this option for maximum issue coverage and longest scan. Recommended for security
                      experts before major releases, compliance testing and benchmarks.								
                -   **Presence**: **Available for AppScan on Cloud only.** If your app is not on the internet,
                    select your AppScan Presence from the list.
                    Information about creating an AppScan Presence is
                    available
                    [here](https://help.hcl-software.com/appscan/ASoC/Presence_scanning.html).<br>
                    **Note**: Presence is available in AppScan on Cloud only.
                -   **Scan File**: If you have an AppScan Standard scan
                    file, enter its full path and file name in this
                    field. To learn more about AppScan Standard scan
                    files, see [this
                    topic](https://help.hcl-software.com/appscan/ASoC/asd_AppScanStandard.html).
				-   **Application login**: Select a Login method from the available options so 
											AppScan can scan pages that require authentication. 
					- **Login not required**: Leave this selected if no login is needed.
					- **Login required: Username and password**: Select this option to allow login to the application 
																	using a username and password.
						-   **Login User** and **Login Password**: If your app requires login, enter valid user credentials so that 
																	Application Security on Cloud can log in to the site.
						-   **Extra Field**: If your app requires a third credential, enter it in this field.
					- **Login required: Record login**: Select this option to allow login to the application using a recorded login sequence.
						-   **Login Sequence File**: Provide a path to the login sequence file data. Supported file type is .CONFIG.
        -   **Software Composition Analysis (SCA)**
            -	**Rescan**: Select this option to rescan the same application, updating and overwriting the previous scan results with the latest findings.<br>
              	**Note**: If you are looking to use the Auto Close feature, ensure it has been enabled by your AppScan on Cloud/AppScan 360° organization administrator. Learn more about [rescanning](https://help.hcl-software.com/appscan/ASoC/appseccloud_scanning_rescan_cm.html).
             	- **Scan ID**: Enter the Scan ID of the parent scan based on the application and technology you selected earlier. You can retrieve Scan ID from the AppScan on Cloud/AppScan 360° Server.
            -   **Target**: Enter the complete path to the directory containing the files to scan, or provide the full path to an existing .irx file. Leave this field empty to scan all supported files within the workspace directory.
        -   **Static Analysis (SAST)**
            -	**Rescan**: Select this option to rescan the same application, updating and overwriting the previous scan results with the latest findings.<br>
	    	**Note**: If you are looking to use the Auto Close feature, ensure it has been enabled by your AppScan on Cloud/AppScan 360° organization administrator. Learn more about [rescanning](https://help.hcl-software.com/appscan/ASoC/appseccloud_scanning_rescan_cm.html).
            	- **Scan ID**: Enter the Scan ID of the parent scan based on the application and technology you selected earlier. You can retrieve Scan ID from the AppScan on Cloud/AppScan 360° Server.		
            -   **Target**: Enter the complete path to the directory containing the files to scan, or provide the full path to an existing .irx, .war, .ear, .jar or .zip file. Leave this field empty to scan all supported files within the workspace directory.
            -   **Scan Method**
                -   **Generate IRX**: Generate an IRX archive locally from the specified files and folders.
                    -   **Additional Options**: If selected, the following options are available:
                        -   **Source Code Only**: Analyze source code only.
                        -   **Include SCA**: Include analysis of open source packages. Include SCA creates an SCA scan in addition to a SAST scan.
                        -   **Select Scan Speed**: Optimize scan speed and results according to development stage. Choose faster scans early in the development lifecycle to identify basic security issues; choose thorough scans later in the cycle to ensure complete coverage for your application.
                            -   **Normal**: Performs a complete analysis of the code, identifying vulnerabilities in detail and differentiating issues that could be reported as false positives. This scan takes the longest to complete.
                            -   **Fast**: Performs a comprehensive analysis of your files to identify vulnerabilities, taking longer to complete than “Faster” or “Fastest” scans.
                            -   **Faster**: Provides a medium level of detail of analysis and identification of security issues. This scan takes more time to complete than the “Fastest” option.
                            -   **Fastest**: Performs a surface-level analysis of your files to identify the most pressing issues for remediation, taking the least amount of time to complete.
                -   **Upload files and folders**: Upload files and folders directly to AppScan for immediate scanning preparation, resulting in faster processing.
                    -   **Additional Options**: If selected, the following options are available:
                        -   **Include SCA**:  Applicable only for IRX files.
                            -   Include analysis of open source packages. Include SCA creates an SCA scan in addition to a SAST scan.
    -   **Email notification:** Send the user an email when analysis is complete.
    -   **Run as a personal scan:** A personal scan does not affect the application data and compliance until it is promoted.
    -   **Allow intervention by scan enablement team:** Available for AppScan on Cloud only.
        -   When selected (default), our scan enablement team will step in if the scan fails, or if 
        no issues are found, and try to fix the configuration. This may delay 
        the scan result.            
    -   **Suspend job until security analysis completes:** If selected,
        the Jenkins job will pause until security analysis has completed
        and the results have been retrieved from the service. If
        unselected, the job will continue once the scan has been
        submitted to the analysis service.
    -	**Fail build for non-compliance with application policies:** Fail
      	the job if one or more issues are found which are non-compliant with
     	 respect to the selected application's policies.
    -   **Fail build if:** If selected, the Jenkins job will fail if the
        finding count(s) exceed the specified thresholds (see below).
    	-   **Add condition:** Allows you to add thresholds for the number
        of findings that will cause a build to fail. You can specify
        thresholds for total, critical, high, medium, and/or low finding counts.
        If multiple conditions are added, they will be treated as though
        they are separated by a logical OR.

**Notes:**
- AppScan on Cloud (ASoC) now performs SAST and SCA analysis as separate scans. To execute an open-source only scan, use the Software Composition Analysis (SCA) scan type.
- Scan logs are now automatically downloaded to the Jenkins job directory after completing dynamic or static scans for HCL AppScan on Cloud and HCL AppScan 360°.

 For more information on adding security analysis to Jenkins automation server, please visit this [link](https://help.hcl-software.com/appscan/ASoC/appseccloud_jenkins.html?query=jenkins).

**Integration with HCL AppScan Enterprise**

1.  Add your AppScan Enterprise credentials on the Jenkins
    **Credentials** page.
    -   From the main Jenkins dashboard, click the **Credentials** link.
    -   Add new global credentials.
    -   In the **Kind** drop-down list, select **HCL AppScan Enterprise Credentials**.
    -   Enter your AppScan Enterprise server URL. For example https:// ASE Server hostname:9443/ase.
    -   Enter your API key details.
    
2.  Add a **Run AppScan Enterprise Security Test** build step to your Jenkins project
    configuration and enter the following information:    
    -   **Credentials**: Select the credentials you added to Jenkins in
        step 1 above.        
       - **Job properties**        
            -   **Template**: Search the template for the scan.
            -   **Job name**: Specify a name to use for the scan. This value
                will be used to distinguish this scan and its results from
                others.
            -   **Scan folder**: Search for the destination folder to create the ADAC job. 
            -   **Application name**: Search for the application with which the scan will be associated.
                Please note this is not a required parameter for an AppScan Enterprise 
                ADAC job.
            -   **Test policy**: Select the test policy for your scan.
            -   **Description**: Description of the job.
            -   **Contact**: Contact user of the job.
            -   **Starting URL**: Enter the URL from where you want the
                scan to start exploring the site.           
       - **Scan**: The type of scan to run.
            - **Full Automatic Scan**: Select this option to automatically explore
     	    	- **Which Login method you want to use**:      
            		- **Recorded login**: Select this option to allow login to the application
              	  	  using a recorded login sequence. Once selected, you would be prompted
              	  	  to enter the path to recorded login sequence. Supported file formats are EXD, HAR, DAST.CONFIG and LOGIN.
              	  	  Please note that .login file is supported from AppScan Enterprise 10.0.4 release onwards.
            		- **Automatic login**: Select this option to allow Login to the application
              	  	  using a username and password. Once selected, you would be prompted to enter the username and password.
                	- **Login not required**: Selecting this option will result in AppScan not scanning pages that require a login.
                 - **Manual Explore (externally recorded)**:
                 	- **Path**: Enter the local path to the Manual explore data, to use as part of a new scan. For example: "C:\samplefileName.dast.config". 
			  Supported Manual explore file formats are: EXD, HAR, DAST.CONFIG, and CONFIG.
            - **Postman Collection**: Test the APIs in the Postman collection file. Add the Postman collection file & supported files.
     	    	- **Postman Collection File**: Export a .json file from Postman containing the APIs to be tested. Supported File type: .json.
     	    	- **Additional Domains**: Specify the domain details for the APIs in the collection. Multiple domains can be listed, separated by commas.
                - **Environmental Variables File**: Upload an optional .json file containing environmental variables required for the collection.
                - **Global Variables File**: Upload an optional .json file containing global variables required for the collection.
                - **Additional Files**:  If the collection required additional files for API execution, add them to a .zip archive.
            - **Test Only**: Select this option to run the test only on external recorded manual explore data.            
     	    	- **Which Login method you want to use**:     
            		- **Recorded login**: Select this option to allow login to the application
              	  	  using a recorded login sequence. Once selected, you would be prompted
              	  	  to enter the path to recorded login sequence. Supported file formats are EXD, HAR, DAST.CONFIG and LOGIN.
              	  	  Please note that .login file is supported from AppScan Enterprise 10.0.4 release onwards.
            		- **Automatic login**: Select this option to allow Login to the application
              	  	  using a username and password. Once selected, you would be prompted to enter the username and password.
                	- **Login not required**: Selecting this option will result in AppScan not scanning pages that require a login.
                 - **Manual Explore (externally recorded)**: **Required for Test Only scan type**
                 	- **Path**: Enter the local path to the Manual explore data, to use as part of a new scan. For example: "C:\samplefileName.dast.config". 
			  Supported Manual explore file formats are: EXD, HAR, DAST.CONFIG, and CONFIG.
       - **Test Optimization**
            - **Fast**: Select this option for approximately 97% issue coverage and twice as fast test stage speed.
			  Recommended for security experts for more frequent scans.
            - **Faster**: Select this option for approximately 85% issue coverage and five times as fast test stage speed.
			  Recommended for DevSecOps during ongoing evaluation.
            - **Fastest**: Select this option for approximately 70% issue coverage and ten times as fast test stage speed.
			  Recommended for Dev and QA during initial evaluation.
            - **No Optimization**: Select this option for maximum issue coverage and longest scan. Recommended for security
              experts before major releases, compliance testing and benchmarks.            
            **Note**: The options of “Faster” and “Fastest” are supported only for AppScan Enterprise version 10.0.0 and above.		
       - **More Options**: If selected, the following option is available.
            - **Designated Agent Server**: Select the agent server to execute the job.
              Default will be considered in absence of any selection.
      -  **Do not execute the next steps in the Jenkins job sequence, until AppScan security analysis completes.:** If selected,
        the Jenkins job will pause until security analysis has completed
        and the results have been retrieved. If unselected, the job will
        continue once the scan has been submitted to the analysis service.
      - **Fail the build if one of the following conditions apply:** If selected, the Jenkins job will fail if the
        finding count(s) exceed the specified thresholds (see below).
        -   **Add Condition:** Allows you to add thresholds for the number
        of findings that will cause a build to fail. You can specify
        thresholds for total, critical, high, medium, and/or low finding counts.
        If multiple conditions are added, they will be treated as though
        they are separated by a logical OR.

**Known Issues**
- ASoC is no longer providing Mobile Scan support. Refer to 
[this page](https://support.hcltechsw.com/community?id=community_blog&sys_id=a537a63adbb6f054a45ad9fcd396191f) for more information. 
The Mobile Scan functionality
has been removed from Jenkins since version 1.0.10. The existing jobs that 
have Mobile Scan configurations must therefore be updated accordingly.
- If you are unable to view the scan report on the Jenkins, first verify that the "Test Name" field. Test Name can't contain any of the following characters: \/:*?"<>|

## Additional Resources
- [HCL AppScan Enterprise: Jenkins Integration](https://www.youtube.com/watch?v=XctRBAd0HQc)
- [Blog: HCL AppScan Integrates Security Scanning Easily into the Jenkins Pipeline](https://blog.hcltechsw.com/appscan/hcl-appscan-integrates-security-scanning-easily-into-the-jenkins-pipeline/) 
- [Blog: Leveraging HCL AppScan on Cloud for More Secure Coding in Jenkins](https://blog.hcltechsw.com/appscan/leveraging-hcl-appscan-on-cloud-for-more-secure-coding-in-jenkins/)
