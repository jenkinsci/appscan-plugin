/**
 * @ Copyright IBM Corporation 2016.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

function getAncestorByType(elem, type) {
	while(elem) {
		elem = elem.parentNode;
		if(elem.tagName.toLowerCase() == type) {
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
	}
}

function waitClicked(e) {
	if(!e.checked) {
		var table = getAncestorByType(e, 'table');
		var failCheckbox = table.querySelector('input[name=failBuild]');
		failCheckbox.checked = false;
	}
}
