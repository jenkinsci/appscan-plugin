CHANGELOG
=========
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
