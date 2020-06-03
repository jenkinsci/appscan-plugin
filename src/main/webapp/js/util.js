/**
 * @ Copyright IBM Corporation 2016.
 * @ Copyright HCL Technologies Ltd. 2020.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

function getAncestorByType(elem, type) {
	while(elem) {
		elem = elem.parentNode;
		if(elem.tagName.toLowerCase() === type) {
			return elem;
		}
	}
	return null;
}

function failBuildClicked(e) {
	if(e.checked) {
		var table = getAncestorByType(e, 'table');
		var waitCheckbox = table.querySelector('input[name=wait]');
		waitCheckbox.checked = true;
        var failNonCompliantIssuesCheckbox=table.querySelector('input[name=failBuildNonCompliance]');
        failNonCompliantIssuesCheckbox.checked=false;                
	}
}

function failBuildNonComplianceIssuesClicked(e){
    if (e.checked){
        var table = getAncestorByType(e, 'table');
		var waitCheckbox = table.querySelector('input[name=wait]');
		waitCheckbox.checked = true;
        var failCheckbox = table.querySelector('input[name=failBuild]');
        failCheckbox.checked=false;                
    }
}

function waitClicked(e) {
	if(!e.checked) {
		var table = getAncestorByType(e, 'table');
		var failCheckbox = table.querySelector('input[name=failBuild]');
        var failNonCompliantIssuesCheckbox=table.querySelector('input[name=failBuildNonCompliance]');
		failCheckbox.checked = false;
        failNonCompliantIssuesCheckbox.checked=false;
	}
}

function aseWaitClicked(e) {
	if(!e.checked) {
		var table = getAncestorByType(e, 'table');
		var failCheckbox = table.querySelector('input[name=failBuild]');        
		failCheckbox.checked = false;        
	}
}

/*
   * Overridable method called before autocomplete container is loaded with result data.
   * This method is overridden to dynamically change the autocomplete result list size
   * @param sQuery {String} Original request.
   * @param oResponse {Object} Response object.
   * @param oPayload {MIXED} (optional) Additional argument(s)
   * @return {Boolean} Return true to continue loading data, false to cancel.

*/
YAHOO.widget.AutoComplete.prototype.doBeforeLoadData = function (sQuery, oResponse, oPayload) {
     if (oResponse.results.length != 0) {
          YAHOO.widget.AutoComplete.prototype.maxResultsDisplayed = oResponse.results.length;
     }
     return true;
}

function resetFields(credentialElement) {
     var credentialNodeList = document.getElementsByName('_.credentials');
     var templateNodeList = document.getElementsByName('_.template');
     var folderNodeList = document.getElementsByName('_.folder');
     var applicationNodeList = document.getElementsByName('_.application');
     for ( i=0; i < credentialNodeList.length; i++) {
         if(credentialNodeList[i] === credentialElement) {
             templateNodeList[i].value = "";
             folderNodeList[i].value = "";
             applicationNodeList[i].value = "";
             break;
         }
     }
}
