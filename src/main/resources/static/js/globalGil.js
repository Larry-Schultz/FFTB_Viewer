
function populateChart(unit, chartName) {
	$.ajax({
		url: '/api/players/globalGilHistoryGraphData?timeUnit='+unit,
		success: function(result) {
			var datasets = [];
			var coreLabels = [];
			var element = result.data;
			if(element.length > 0) {
				var playerName = unit;
				var labels = [];
				var dataArray = [];
				for(var j = 0; j < element.length; j++) {
					
					let date = new Date(element[j].date);
					const year = new Intl.DateTimeFormat('en', { year: 'numeric' }).format(date)
					const month = new Intl.DateTimeFormat('en', { month: 'short' }).format(date)
					const day = new Intl.DateTimeFormat('en', { day: '2-digit' }).format(date)
					let dateString = `${day}-${month}-${year}`;
					labels.push(dateString);
					dataArray.push(element[j].globalGilCount);
				}
				coreLabels = labels;
				datasets.push({
					data: dataArray,
					label: playerName,
					fill: false
					});
			}
			
			new Chart(document.getElementById(chartName), {
				type: 'line',
				data: {
					labels: coreLabels,
					datasets: datasets,
				},
				options: {
				    title: {
				    	display: true,
				        text: 'Global Gil Count Based on historical data (where available)',
				        responsive: false,
				    },
				    plugins: {
			            colorschemes: {
			                scheme: 'brewer.Paired12'
			          }
			        }
				  }
				});
		}
	});
}

populateChart('day', 'countByDayChart');
populateChart('week', 'countByWeekChart');
populateChart('month', 'countByMonthChart');