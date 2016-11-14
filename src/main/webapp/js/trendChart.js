/**
 * @ Copyright IBM Corporation 2016.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

google.charts.load('current', {'packages':['corechart']});
google.charts.setOnLoadCallback(drawChart);

function drawChart() {
	var dataTable;
	var numResultSets = 0;
	var cols = [];

	for(var scanner in builds) {
		var data = new google.visualization.DataTable();
		data.addColumn('number', 'Build');
		data.addColumn('number', scanner);
		var findings = builds[scanner];
		for (var key in findings) {
			if(findings[key] == -1) {
				continue;
			}
			var row = [parseInt(key),findings[key]];
			data.addRow(row);
		}
		
		if(!dataTable) {
			dataTable = data;
		}
		else {
			numResultSets = numResultSets + 1;
			cols.push(numResultSets);
			dataTable = google.visualization.data.join(dataTable, data, 'full', [[0,0]], cols, [1]);
		}
	}
	
	var numLines = 5;
	var numRows = dataTable.getNumberOfRows();
	if(numRows < numLines) {
		numLines = numRows;
		if(numRows % 2 == 0) {
			numlines = numLines + 1;
		}
	}
	
	var options = {
		title: title,
  		legend: {position: 'right', alignment: 'end'},
  		hAxis: {
  			title: 'Build Number',
  			format: '#',
  			gridlines: {count: numLines}
  		},
  		vAxis: {
  			title: 'Findings',
  			viewWindow: {min: 0},
  			format: '0'
      	}
    };
	
	var chart = new google.visualization.AreaChart(document.getElementById('trendChart'));
	chart.draw(dataTable, options);
}
