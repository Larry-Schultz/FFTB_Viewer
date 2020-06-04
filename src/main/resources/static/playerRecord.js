
function playerSearch() {
	var playerName = $('#playerNamesInput').val();
	var locationUrl = window.location.toString();
	if(locationUrl.includes('/player/')) {
		window.location.replace(playerName);
	} else {
		window.location.replace('/player/' + playerName);
	}
}

function pullPlayerList() {
	$.ajax({
		url: '/api/players/playerList',
		success: function(result) {
		    var availableTags = result.data;
		    $("#playerNamesInput").autocomplete({
		      source: availableTags,
		      minLength: 2
	    	});
		}
	});
}


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
	var playerName = $('#playerName').text();
	$.ajax({
		url: '/api/players/balanceHistory?player='+playerName+'&count=10',
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
				        text: 'Player balance based on past participated tournaments (data where available)',
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

pullPlayerList();
setColor('allegiance');
populateChart();