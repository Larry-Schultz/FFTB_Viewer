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
						labels.push(element.balanceHistory[j].create_timestamp);
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

populateChart();