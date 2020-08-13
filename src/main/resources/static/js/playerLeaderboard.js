function populateChart() {
	var playerNames = $('#playerNames').val();
	$.ajax({
		url: '/api/players/playerLeaderboardBalanceHistory?players=' + playerNames + '&count=' + '10',
		success: function(result) {
			var datasets = [];
			var coreLabels = [];
			for(var i = 0; i < result.data.leaderboardBalanceHistories.length; i++) {
				var element = result.data.leaderboardBalanceHistories[i];
				if(element.balanceHistory.length == 10) {
					var playerName = element.playerName;
					var labels = [];
					var dataArray = [];
					for(var j = 0; j < element.balanceHistory.length; j++) {
						labels.push(createDateString(element.balanceHistory[j].create_timestamp));
						dataArray.push(element.balanceHistory[j].balance);
					}
					coreLabels = labels;
					datasets.push({
						data: dataArray,
						label: playerName,
						fill: false
						});
				}
			}
			
			new Chart(document.getElementById("line-chart"), {
				type: 'line',
				data: {
					labels: coreLabels,
					datasets: datasets,
				},
				options: {
				    title: {
				    	display: true,
				        text: 'Top Player balance based on past participated tournaments (data where available)',
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

function createDateString(dateValue) {
	let date = new Date(dateValue);
	const year = new Intl.DateTimeFormat('en', { year: 'numeric' }).format(date)
	const month = new Intl.DateTimeFormat('en', { month: 'short' }).format(date)
	const day = new Intl.DateTimeFormat('en', { day: '2-digit' }).format(date)
	let dateString = `${day}-${month}-${year}`;
	return dateString;
}

populateChart();