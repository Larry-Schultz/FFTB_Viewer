
function populateBotAccountChart() {
	var playerName = $('#accountName').text();
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
			
			new Chart(document.getElementById("accountChart"), {
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

function revealModal(modalId) {
	$('#'+modalId).foundation('open');
}

function populateBotCharts() {
	$('.botName').each(function() {
		populateBotChart($(this).text().trim());
	});
}

function populateBotChart(name) {
	let datasets = [];
	let coreLabels = [];
	let data = [];
	for(var i = 0; i <= 23; i++) {
		let hourlyDataEntryElement = $('#' + name + 'HourlyDataEntry' + i);
		var newData = hourlyDataEntryElement.attr('balance');
		if(typeof newData !== 'undefined') {
			newData = newData.trim();
			data.push(parseInt(newData));
			coreLabels.push(hourlyDataEntryElement.attr('value').trim());
		}
	}
	
	datasets.push({
		data: data,
		label: name,
		fill: false
		});
	
	console.log('creating chart for: ' + name);
	new Chart(document.getElementById(name + 'Chart'), {
		type: 'line',
		data: {
			labels: coreLabels,
			datasets: datasets,
		},
		options: {
		    title: {
		    	display: true,
		        text: 'Bot balance based on past 24 hours',
		        responsive: false,
		        maintainAspectRatio: false
		    }
		  }
		});
}

var stompClient = null;

function connect() {
	var url = window.location.href  + 'gs-guide-websocket';
	url = url.replace('botland', '');
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
