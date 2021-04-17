
function setColor(elementId) {
	var element = document.getElementById(elementId);
	var teamName = $('#'+elementId).text();
	switch(teamName) {
		case 'BLUE':
			element.style.color = "blue";
			break;
		case 'WHITE':
			element.style.color = 'grey';
			break;
		case 'BLACK':
			element.style.color = 'black';
			break;
		case 'RED':
			element.style.color = 'red';
			break;
		case 'PURPLE':
			element.style.color = 'purple';
			break;
		case 'GREEN':
			element.style.color = 'green';
			break;
		case 'YELLOW':
			$('#'+elementId).css('color', 'rgb(204,204,0)'); //default yellow looks hideous
			break;
		case 'BROWN':
			element.style.color = 'brown';
			break;
		case 'CHAMPION': case 'CHAMP':
			element.style.color = 'orange';
			break;
		case 'NONE':
			break;
	}
}

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