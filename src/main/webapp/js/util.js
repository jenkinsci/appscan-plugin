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

function getComponent(elem, eleName) {
	while(elem) {
		elem = elem.parentNode;
		var v = elem.getElementsByTagName('INPUT');
		for (i = 0; i < v.length; i++) {
			if (isTargetComponent(v[i], eleName)) {
				return v[i];
			}
		}
	}
	return null;
}

function isTargetComponent(elem, eleName) {
	return (elem != null && elem.nodeType === Node.ELEMENT_NODE && elem.hasAttribute('name') && elem.getAttribute('name') === eleName);
}

function failBuildClicked(e) {
	if(e.checked) {
		var waitCheckbox = getComponent(e, 'wait');
		waitCheckbox.checked = true;
		var failNonCompliantIssuesCheckbox = getComponent(e, 'failBuildNonCompliance');
		failNonCompliantIssuesCheckbox.checked = false;
	}
}

function failBuildNonComplianceIssuesClicked(e){
	if (e.checked) {
		var waitCheckbox = getComponent(e, 'wait');
		waitCheckbox.checked = true;
		var failCheckbox = getComponent(e, 'failBuild');
		failCheckbox.checked = false;
	}
}

function waitClicked(e) {
	if(!e.checked) {
		var failCheckbox = getComponent(e, 'failBuild');
		var failNonCompliantIssuesCheckbox = getComponent(e, 'failBuildNonCompliance');
		failCheckbox.checked = false;
		failNonCompliantIssuesCheckbox.checked = false;
	}
}

function aseWaitClicked(e) {
	if(!e.checked) {
		var failCheckbox = getComponent(e, 'failBuild');
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
