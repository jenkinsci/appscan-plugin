CHANGELOG
=========
1.4.1 (July, 2024)
------
- Support to execute SAST and SCA scans as a single pipeline job for HCL AppScan on Cloud.
- Updated Jenkins build console log to display the ASoC Scan Overview URL.
- Bug fixes.

1.4.0 (June, 2024)
------
- Support for executing DAST Scans via HCL AppScan 360° v1.3 and above.
- Support for Personal Scans in HCL AppScan on Cloud and HCL AppScan 360°.
- Bug fixes.

1.3.1 (March, 2024)
------
- Migration to version 4 of ASoC REST APIs.
- Bug fixes.

1.3.0 (November, 2023)
------
- Support for new scan type : Software Composition Analysis (SCA) in AppScan on Cloud.
- Support for adding Contact and Description for AppScan Enterprise jobs.
- Bug fixes.

1.2.0 (August, 2023)
------
- Added support for uploading files and folders in AppScan on Cloud/AppScan 360° for static analysis scans.
- Support static analysis scan speeds.
- Bug fixes.

1.1.0 (June, 2023)
------
- Support for executing SAST Scans via AppScan 360°.
- Support source code only scans in Static Analysis.
- Bug Fixes

1.0.14 (February, 2023)
------
* Support for critical severity.
* Bug Fixes
  
1.0.13 (November, 2022)
------
* Fix generated report to show missing information.

1.0.12 (July, 2022)
------
* Support additional login options for HCL AppScan on Cloud dynamic scans.
* Fix to support Jenkins Environment variables for ASE scan fields.
* Bug Fixes

1.0.11 (June, 2022)
------
* Bug Fixes

1.0.10 (April, 2022)
------
* Discontinued Mobile Application Security Testing (MAST) support from HCL AppScan on Cloud task.
* Included an option in AppScan on Cloud task to control intervention by scan enablement team
* Bug Fixes

1.0.9 (January, 2022)
------
* Fix to handle ASoC communication interruptions while Jenkins job is running.
* Fix to support Jenkins Environment variables for ASoC scan fields.
* Sort the presence list in the ASoC task.
* Modified the SAClient logs to display Jenkins.

1.0.8 (July, 2021)
------
* Fix to handle the network disconnection scenario while Jenkins job is awaiting scan results from ASoC.
* Fix to support Jenkins Environment variables for ASoC scan name field. Refer [JENKINS-60883](https://issues.jenkins-ci.org/browse/JENKINS-60883) for more details.
* Support for .login file in AppScan Enterprise scans.
* Display of new AppScan icon in scan results.
* Minimum Jenkins Version updated to 2.222.4.

1.0.7 (November, 2020)
------
* Support for additional data centers.

1.0.6 (September, 2020)
------
* Fix to accommodate a design change made in AppScan Enterprise 10.0.2 as regards polling frequency of report packs for ADAC jobs. If you are a consumer of AppScan Enterprise 10.0.2 release or above, it is mandatory to use this version or else "Fail Build Condition" might not work as expected.
* Inclusion of AppScan Enterprise Scan log URL in console output of a job configured for AppScan Enterprise Scan execution.
* Inclusion of AppScan Enterprise URL in Status section of a job configured for AppScan Enterprise Scan execution. The URL will be displayed only if a job completes successfully with display of scan results.
* Inclusion of AppScan On Cloud URL in Status section of a job configured for ASoC scans. The URL will be displayed only if a job completes successfully with display of scan results.

1.0.5 (August, 2020)
------
* Fixed bugs related to session validation and token reuse while configuring and executing ADAC jobs in AppScan Enterprise.

1.0.4 (June, 2020)
------
* Fix for [JENKINS-62314](https://issues.jenkins-ci.org/browse/JENKINS-62314)

1.0.3 (June, 2020)
------
* ASE Template, Folder, and Application fields now support type-ahead search.
* Enhanced error reporting for ASE jobs.
* Documentation and help updates.
* General bug fixes.

1.0.2 (May, 2020)
------
* ASE Test Optimization description updates.
* Fix for [JENKINS-62314](https://issues.jenkins-ci.org/browse/JENKINS-62314)

1.0.1 (April, 2020)
------
* Updated Test Optimization for ASoC DAST scans (“No Optimization”, “Fast”, “Faster” and “Fastest”).
* New Test Optimization for ASE scans (“No Optimization”, “Fast”, “Faster” and “Fastest”).
* Enhanced error reporting.
* Fix for [JENKINS-60958](https://issues.jenkins-ci.org/browse/JENKINS-60958)

1.0.0 (March, 2020)
------
* Initial release.
