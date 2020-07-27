
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
	if(typeof playerName !== 'undefined' && playerName != null && playerName !== "") {
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
							let date = new Date(element.balanceHistory[j].create_timestamp);
							const year = new Intl.DateTimeFormat('en', { year: 'numeric' }).format(date)
							const month = new Intl.DateTimeFormat('en', { month: 'short' }).format(date)
							const day = new Intl.DateTimeFormat('en', { day: '2-digit' }).format(date)
							let dateString = `${day}-${month}-${year}`
							labels.push(dateString);
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
}

var stompClient = null;

function connect() {
	var url = window.location.href  + 'gs-guide-websocket';
	var playerName = $('#playerName').text();
	if(typeof playerName !== 'undefined' && playerName != null && playerName !== "") {
		url = url.replace('player/' + playerName, '');
		url = url.replace('refresh=true', '');
		url = url.replace('?', '');
	    var socket = new SockJS(url);
	    stompClient = Stomp.over(socket);
	    stompClient.connect({}, function (frame) {
	        stompClient.subscribe('/matches/events', function (chatMessages) {
	        	try {
	        		var jsonData = JSON.parse(chatMessages.body);
	        		var events = checkAndPossiblyResend(jsonData);
	        		if(events != null) {
		        		for(event of events) {
		        			parseEvents(event.element);
		        		}
	        		}
	        		var jsonData = JSON.parse(chatMessages.body);
	        		parseEvents(jsonData.element);
	        	} catch(error) {
	        		console.log("error found for chatMessages: " + chatMessages.body + " with error " + error);
	        		console.log(error.stack);
	        	}
	        });
	    });
	}
    
}

var eventIndex = null;
function checkAndPossiblyResend(jsonData) {
	var events = null;
	if(eventIndex == null) {
		eventIndex = jsonData.id;
	} else if(jsonData.id != (eventIndex + 1)) {
		var ids = [];
		for(var i = eventIndex + 1; i < jsonData.id; i++) {
			ids.push(i);
		}
		console.log("missing ids" + ids.join() + " getting remission from the server");
		events = manuallyCallToRetrieveEvent(ids);
		eventIndex = jsonData.id;
	} else {
		eventIndex = jsonData.id;
	}
	
	return events;
}

function manuallyCallToRetrieveEvent(indexes) {
	var events = null;
	$.ajax({
		url: "api/matches/currentData?ids=" + indexes.join(),
		async: false,
		success: function(result){
			events = result;
	  }});
	
	return events;
}

function parseEvents(event) {
	switch(event.eventType) {
		case 'BETTING_BEGINS':
			reload();
			break;
		default:
			break;
	}
	
}

function reload() {
	const queryString = window.location.search;
	const urlParams = new URLSearchParams(queryString);
	urlParams.set("refresh", "true");
	//stompClient.close();
	window.location.search = urlParams.toString();
}

pullPlayerList();
setColor('allegiance');
populateChart();
connect();