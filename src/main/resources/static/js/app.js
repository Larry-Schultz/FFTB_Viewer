
var stompClient = null;
var loading = true; //track if the page is in loading state.

function connect() {
    var socket = new SockJS(window.location.href  + 'gs-guide-websocket');
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

function pullCurrentData() {
	$.ajax({url: "api/matches/currentData", success: function(result){
		console.log(result);
		result.data.sort((a, b) => (a.id > b.id) ? 1 : -1)
	    result.data.map(function (event) {
	    	eventIndex=event.id;
	    	parseEvents(event.element);
	    });
	  }});
}

var team1Name;
var team2Name;
function parseEvents(event) {
	//console.log(JSON.stringify(event));
	
	switch(event.eventType) {
		case 'BET':
			handleBet(event);
			break;
		case 'BET_INFO':
			handleBetInfo(event);
			break;
		case 'BETTING_BEGINS':
			team1Name = event.team1;
			team2Name = event.team2;
			handleBetBegins(event);
			break;
		case 'BETTING_ENDS':
			$('.notice').hide();
			if(loading) {
				$('#loadingNotice').show();
			} else {
				$('#matchNotice').show();
			}
			break;
		case 'FIGHT_BEGINS':
			$('.fightData').hide();
			$('.fightLoading').show();
			$('.notice').hide();
			$('#fightNotice').show();
			loading = false;
			break;
		case 'RESULT':
			$('.notice').hide();
			if(loading) {
				$('#loadingNotice').show();
			} else {
				$('#resultsNotice').show();
			}
			break;
		case 'TEAM_INFO':
			handleTeamInfo(event)
			break;
		case 'UNIT_INFO':
			handleUnitInfo(event);
			break;
		case 'MATCH_INFO':
			populateMatchInfo(event);
			break;
		case 'SKILL_DROP':
			populateSkillDrop(event);
			break;
		case 'BAD_BET':
			handleBadBet(event);
			break;
		case 'MUSIC':
			handleMusicEvent(event);
			break;
	}
	
	//tippy('[data-tippy-content]');
}

function handleBetBegins(event) {
	loading = false;
	resetMatchBlock();
	$('#team1').find('.player').each(function() { destroyTippyIfPresent($(this).attr('id'))});
	$('#team1').find('.example').each(function() { destroyTippyIfPresent($(this).attr('id'))});
	$("#team1").children().remove();
	
	$('#team2').find('.player').each(function() { destroyTippyIfPresent($(this).attr('id'))});
	$('#team2').find('.example').each(function() { destroyTippyIfPresent($(this).attr('id'))});
	$("#team2").children().remove();

	$('#team1Name').text(capitalize(team1Name.toLowerCase() + ' team'));
	$('#team2Name').text(capitalize(team2Name.toLowerCase() + ' team'));
	setColor(event.team1, 'team1'); 
	setColor(event.team1, 'team1grid'); 
	setColor(event.team1, 'team1Name');
	setColor(event.team1, 'team1BetCount');
	setColor(event.team1, 'team1BetCountIndicator');
	setColor(event.team1, 'team1Amount');
	setColor(event.team1, 'team1AmountIndicator');
	setColor(event.team1, 'team1Odds');
	setColor(event.team1, 'team1OddsIndicator');
	setColor(event.team1, 'team1Percentage');
	setColor(event.team1, 'team1PercentageSign');
	
	setColor(event.team2, 'team2'); 
	setColor(event.team2, 'team2grid'); 
	setColor(event.team2, 'team2Name');
	setColor(event.team2, 'team2BetCount');
	setColor(event.team2, 'team2BetCountIndicator');
	setColor(event.team2, 'team2Amount');
	setColor(event.team2, 'team2AmountIndicator');
	setColor(event.team2, 'team2Odds');
	setColor(event.team2, 'team2OddsIndicator');
	setColor(event.team2, 'team2Percentage');
	setColor(event.team2, 'team2PercentageSign');
}

function handleBet(event) {
	if(event.team == team1Name || event.team == 'LEFT') {
		$("#team1").prepend(generatePlayerRecord(event.metadata, event.player, event.betText, event.betAmount, event.betType, 'team1', event.allinbutFlag));
		sortList('team1');
		attachTabindexToGridElements(1000, 'team1');
		if(!isNaN(event.betAmount)) {
			var newAmount = countValuesforGrid('team1');
			$('#team1BetCount').text(countBetsForGrid('team1'));
			$('#team1Amount').attr('data-gil', newAmount);
			$('#team1Amount').text(newAmount.toLocaleString());
			$('#team1Odds').text(calculateOddsForTeam($('#team1Amount').text(), $('#team2Amount').text()));
			$('#team1Percentage').text(calculatePercentageForTeam($('#team1Amount').text(), $('#team2Amount').text()));
			$('#team2Percentage').text(calculatePercentageForTeam($('#team2Amount').text(), $('#team1Amount').text()));
		}
	} else if(event.team == team2Name || event.team == 'RIGHT') {
		$("#team2").prepend(generatePlayerRecord(event.metadata, event.player, event.betText, event.betAmount, event.betType, 'team2', event.allinbutFlag));
		sortList('team2');
		attachTabindexToGridElements(2000, 'team2');
		if(!isNaN(event.betAmount)) {
			var newAmount = countValuesforGrid('team2');
			$('#team2BetCount').text(countBetsForGrid('team2'));
			$('#team2Amount').attr('data-gil', newAmount);
			$('#team2Amount').text(newAmount.toLocaleString());
			$('#team2Odds').text(calculateOddsForTeam($('#team2Amount').attr('data-gil'), $('#team1Amount').attr('data-gil')));
			$('#team1Percentage').text(calculatePercentageForTeam($('#team1Amount').attr('data-gil'), $('#team2Amount').attr('data-gil')));
			$('#team2Percentage').text(calculatePercentageForTeam($('#team2Amount').attr('data-gil'), $('#team1Amount').attr('data-gil')));
		}
	}
	
}

function handleBadBet(event) {
	for(var i = 0; i < event.players.length; i++) {
		var id = event.players[i].toLowerCase() + 'Record';
		if(document.getElementById(id) != null) {
			document.getElementById(id).remove();
		}
	}
	
	$('#team1BetCount').text(countBetsForGrid('team1'));
	$('#team2BetCount').text(countBetsForGrid('team2'));
	
	var newAmountTeam1 = countValuesforGrid('team1');
	$('#team1Amount').attr('data-gil', newAmountTeam1);
	$('#team1Amount').text(newAmountTeam1.toLocaleString());
	
	var newAmountTeam2 = countValuesforGrid('team2');
	$('#team2Amount').attr('data-gil', newAmountTeam2);
	$('#team2Amount').text(newAmountTeam2.toLocaleString());
	
	var team1Odds = calculateOddsForTeam(newAmountTeam1, newAmountTeam2);
	var team2Odds = calculateOddsForTeam(newAmountTeam2, newAmountTeam1);
	
	$('#team1Odds').text(team1Odds);
	$('#team2Odds').text(team2Odds);
	
	$('#team1Percentage').text(calculatePercentageForTeam(newAmountTeam1, newAmountTeam2));
	$('#team2Percentage').text(calculatePercentageForTeam(newAmountTeam2, newAmountTeam1));
}

function handleMusicEvent(event) {
	$('#trackImage').hide();
	$('#trackName').text(event.songName);
}

function generatePlayerRecord(metadata, player, betText, betAmount, betType, team, allinbutFlag) {
	var possiblePlayerElement = document.getElementById(player + 'Record');
	if(possiblePlayerElement != null) {
		$('#' + player + 'Record').remove();
	}
	
	
	var possibleStar = betType =='ALLIN' || betType == 'PERCENTAGE' || betType == 'HALF' || betType == 'FLOOR' || allinbutFlag ? '<span class="example" id="' + player + 'Example">*</span>' : '';
	var possibleStarTooltip = 'Based on current data';
	
	var playerTooltip = "";
	if(metadata != null) {
		var wins = (metadata.wins != null && metadata.wins > 0 ? metadata.wins : 1);
		var losses = (metadata.losses != null && metadata.losses > 0 ? metadata.losses : 1);
		var playerWinLossRatio = wins / (wins+losses);
		playerWinLossRatio = parseInt(playerWinLossRatio.toFixed(2) * 100);
		playerTooltip = 'Betting: W:' + metadata.wins + ' L:' + metadata.losses + ' R:' + playerWinLossRatio + '%';
	}
	var playerRecord = '<span id="'+ player + 'Name" class="player" tabindex="" ><a id="' + player + 'DataLink" href="/player/'+ player + '" target="_blank" style="color: inherit;" >' + player + '</a></span>';
	
	var betTip = betText;
	if(allinbutFlag) {
		betTip = "allinbut " + betTip;
	}
	var betRecord = '<span id="' + player + 'BetAmount" class="betAmount">' + betAmount + '</span>';
	
	var record = '<li id="' + player + 'Record">' + playerRecord + '-' + betRecord + possibleStar + '</li>';

	$('#'+team).prepend(record);
	if(metadata != null) {
		attachTippy(player+'Name', playerTooltip);
	}
	if(possibleStar != '') {
		attachTippy(player+'Example', possibleStarTooltip);
	}
	
	attachTippy(player+'BetAmount', betTip);
	
}

function handleBetInfo(event) {
	if(event.team == team1Name || event.team == 'LEFT') {
		$("#team1").prepend(generatePlayerRecord(event.metadata, event.player, event.betAmount, event.betAmount, 'VALUE', 'team1', false));
		sortList('team1');
		attachTabindexToGridElements(1000, 'team1');
		if(!isNaN(event.betAmount)) {
			var newAmount = countValuesforGrid('team1');
			$('#team1BetCount').text(countBetsForGrid('team1'));
			$('#team1Amount').attr('data-gil', newAmount);
			$('#team1Amount').text(newAmount.toLocaleString());
			$('#team1Odds').text(calculateOddsForTeam($('#team1Amount').attr('data-gil'), $('#team2Amount').attr('data-gil')));
			$('#team2Odds').text(calculateOddsForTeam($('#team2Amount').attr('data-gil'), $('#team1Amount').attr('data-gil')));
			$('#team1Percentage').text(calculatePercentageForTeam($('#team1Amount').attr('data-gil'), $('#team2Amount').attr('data-gil')));
			$('#team2Percentage').text(calculatePercentageForTeam($('#team2Amount').attr('data-gil'), $('#team1Amount').attr('data-gil')));
		}
	} else if(event.team == team2Name || event.team == 'RIGHT') {
		$("#team2").prepend(generatePlayerRecord(event.metadata, event.player, event.betAmount, event.betAmount, 'VALUE', 'team2', false));
		sortList('team2');
		attachTabindexToGridElements(2000, 'team2');
		if(!isNaN(event.betAmount)) {
			var newAmount = countValuesforGrid('team2');
			$('#team2BetCount').text(countBetsForGrid('team2'));
			$('#team2Amount').attr('data-gil', newAmount);
			$('#team2Amount').text(newAmount.toLocaleString());
			$('#team1Odds').text(calculateOddsForTeam($('#team1Amount').attr('data-gil'), $('#team2Amount').attr('data-gil')));
			$('#team2Odds').text(calculateOddsForTeam($('#team2Amount').attr('data-gil'), $('#team1Amount').attr('data-gil')));
			$('#team1Percentage').text(calculatePercentageForTeam($('#team1Amount').attr('data-gil'), $('#team2Amount').attr('data-gil')));
			$('#team2Percentage').text(calculatePercentageForTeam($('#team2Amount').attr('data-gil'), $('#team1Amount').attr('data-gil')));
		}
	}
}

function populateMatchInfo(event) {
	$('#mapNumber').text(event.mapNumber);
	$('#mapNumber').show();
	$('#mapName').text(event.mapName);
	$('#mapName').show();
	$('.mapLoading').hide();
	$('#mapIcon').attr('src', 'img/maps/' + event.mapNumber + '.PNG');
	$('#mapIconLink').attr('href', 'https://ffhacktics.com/maps.php?id=' + event.mapNumber);
	$('#mapIconSpan').show();
}

function populateSkillDrop(event) {
	$('#skillImage').hide();
	$('#skilldropName').text(event.skill)
	//$('#skilldropName').attr('title', event.skillDescription);
	attachTippy('skilldropName', event.skillDescription);
}

function handleTeamInfo(event) {
	if(event.team == team1Name) {
		parseAndFillTeamInfo(event, 'Left');
		$('.leftPlayerNameLoading').hide();
	} else if(event.team == team2Name) {
		parseAndFillTeamInfo(event, 'Right');
		$('.rightPlayerNameLoading').hide();
	}
}

function handleUnitInfo(event) {
	for(var i of [1,2,3,4]) {
		for(direction of ['Left', 'Right']) {
			var base = 'fight' + direction + 'Player' + i;
			var tagPlayerName = $('#' + base + 'Name').text()
			if(tagPlayerName == event.player) {
				attachTippy(base + 'Class', event.unitInfoString);
				//$(base + 'Class').attr('title', event.unitInfoString);
				$('#' + base + 'ImageTag').attr('src', generateUnitImageString(event, direction, i));
				$('#' + base + 'Image').show();
				attachTippy(base + 'ImageTag', generateUnitImageTitle(event, direction, i));
				//$(base + 'ImageTag').attr('data-tippy-content', generateUnitImageTitle(event, direction, i));
				$('#' + base + 'LoadingImage').hide();
				if(event.isRaidBoss) {
					$('#fight'+ direction + 'Player' + i + 'RaidBossIndicator').show();
				}
				break;
			}
		}
	}
}

function parseAndFillTeamInfo(event, team) {
	var k=0;
	for(var i in event.playerUnitPairs){
	    k++;
		var key = i;
	    var val = event.playerUnitPairs[i];
	    for(var j in val){
	        var fighterName = j;
	        var fighterClass = val[j];
	        populateTeamInfo(fighterName, fighterClass, k, team, event.metaData[i])
	    }
	}
}

function populateTeamInfo(fighterName, fighterClass, position, team, metaData) {
	var baseTag = 'fight'+team + 'Player' + position;
	var fighterNameTag = '#' + baseTag + 'Name';
	var fighterClassTag = '#' + baseTag + 'Class';
	var fighterLinkTag = '#' + baseTag + 'Link';
	$('#' + baseTag).show();
	$(fighterNameTag).text(fighterName);
	$(fighterLinkTag).attr('href', '/player/'+fighterName);
	//$(fighterNameTag).attr('data-tooltip');
	
	
	if(metaData) {
		var wins = (metaData.fightWins != null && metaData.fightWins > 0 ? metaData.fightWins : 1);
		var losses = (metaData.fightLosses != null && metaData.fightLosses > 0 ? metaData.fightLosses : 1);
		var playerWinLossRatio = wins / (wins+losses);
		playerWinLossRatio = parseInt(playerWinLossRatio.toFixed(2) * 100);
		var tooltip = 'Fighting: W:' + metaData.fightWins + ' L:' + metaData.fightLosses + ' R:' + playerWinLossRatio + '%"';
		//$(fighterNameTag).attr('title', tooltip);
		attachTippy(baseTag + 'Name', tooltip);
	}
	
	$(fighterClassTag).text(fighterClass);
	if(team == 'Left') {
		setColor(team1Name, baseTag);
	} else if(team == 'Right') {
		setColor(team2Name, baseTag);
	}
}

function generateUnitImageString(event, direction, position) {
	var baseUrl = 'images/characters/';
	var classTagName = '#fight' + direction + 'Player' + position + 'Class';
	var className = $(classTagName).text();
	if(className.startsWith('Calculator')) {
		className = className.split('(')[0].trim();
	}
	var gender = event.unit.gender;
	
	var characterImageString;
	if(gender != 'Monster') {
		if(className == 'Time Mage') {
			className = 'TimeMage';
		}
		characterImageString = baseUrl + className + ' ' + gender;
	} else {
		characterImageString = baseUrl + className.replace(' ', '');
	}
	
	return characterImageString;
}

function generateUnitImageTitle(event, direction, position) {
	var classTagName = '#fight' + direction + 'Player' + position + 'Class';
	var className = $(classTagName).text();
	var gender = event.unit.gender;
	
	var characterImageTitle;
	if(gender != 'Monster') {
		characterImageTitle = className + ' - ' + gender;
	} else {
		characterImageTitle = className;
	}
	
	return characterImageTitle;
}

function sortList(id) {
  var list, i, switching, b, shouldSwitch;
  list = document.getElementById(id);
  switching = true;
  /* Make a loop that will continue until
  no switching has been done: */
  while (switching) {
    // Start by saying: no switching is done:
    switching = false;
    b = list.getElementsByTagName("LI");
    // Loop through all list items:
    for (i = 0; i < (b.length - 1); i++) {
      // Start by saying there should be no switching:
      shouldSwitch = false;
      /* Check if the next item should
      switch place with the current item: */
      if (parseInt(b[i].querySelector('.betAmount').innerHTML.toLowerCase()) < parseInt(b[i + 1].querySelector('.betAmount').innerHTML.toLowerCase())) {
        /* If next item is alphabetically lower than current item,
        mark as a switch and break the loop: */
        shouldSwitch = true;
        break;
      }
    }
    if (shouldSwitch) {
      /* If a switch has been marked, make the switch
      and mark the switch as done: */
      b[i].parentNode.insertBefore(b[i + 1], b[i]);
      switching = true;
    }
  }
}

function attachTabindexToGridElements(indexStart, gridName) {
	var i = indexStart
	$('#'+gridName).find('.player').each(function(){
		$(this).attr('tabindex', i);
		i++;
	});
	
	$('#'+gridName).find('.example').each(function() {
		$(this).attr('tabindex', i);
		i++;
	});
	
	$('#'+gridName).find('.betAmount').each(function() {
		$(this).attr('tabindex', i);
		i++;
	});
}

function countBetsForGrid(gridName) {
	var count = 0;
	$('#'+gridName).find('.betAmount').each(function(index) {
		count++;
	});
	
	return count;
}

function countValuesforGrid(gridName) {
	var count = 0;
	$('#'+gridName).find('.betAmount').each(function(index) {
		count = count + parseInt($(this).text());
	});
	
	return count;
}

function calculateOddsForTeam(yourTeamTotalValue, theirTeamTotalValue) {
	if(yourTeamTotalValue == 0 || theirTeamTotalValue == 0) {
		return 0;
	} else {
		var result =  (parseInt(theirTeamTotalValue) / parseInt(yourTeamTotalValue));
		result =  result.toFixed(3);
		return result;
	}
}

function calculatePercentageForTeam(yourTeamTotalValue, theirTeamTotalValue) {
	if(yourTeamTotalValue == 0 || theirTeamTotalValue == 0) {
		return 0;
	} else {
		var result = parseInt(yourTeamTotalValue) / (parseInt(yourTeamTotalValue) + parseInt(theirTeamTotalValue));
		result = result * 100;
		result = result.toFixed(1);
		return result;
	}
}

function attachTippy(elementName, content) {
	destroyTippyIfPresent(elementName.trim());
	$('#' + elementName.trim()).attr('title', content);
}

function destroyTippyIfPresent(elementName) {
	$('#' + elementName.trim()).attr('title', null);
}


function setColor(teamName, elementId) {
	var teamNameUppercase = teamName.toUpperCase();
	var element = document.getElementById(elementId);
	switch(teamNameUppercase) {
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
		default:
			break;
	}
}

function capitalize(string) {
	return string.charAt(0).toUpperCase() + string.slice(1);
}

function resetMatchBlock() {
	$('.fightData').hide();
	$('.fightLoading').show();
	
	$('#team1Name').show();
	$('#team2Name').show();
	$('#team1BetCount').show();
	$('#team1BetCountIndicator').show();
	$('#team1Amount').show();
	$('#team1AmountIndicator').show();
	$('#team1Odds').show();
	$('#team1OddsIndicator').show();
	$('#team1Percentage').show();
	$('#team1PercentageSign').show();
	$('#team2BetCount').show();
	$('#team2BetCountIndicator').show();
	$('#team2Amount').show();
	$('#team2AmountIndicator').show();
	$('#team2Odds').show();
	$('#team2OddsIndicator').show();
	$('#team2Percentage').show();
	$('#team2PercentageSign').show();
	$('#team1BetCount').text(0);
	$('#team1Amount').text(0);
	$('#team1Odds').text(0);
	$('#team1Percentage').text(0);
	$('#team2BetCount').text(0);
	$('#team2Amount').text(0);
	$('#team2Odds').text(0);
	$('#team2Percentage').text(0);
	$('.teamNameLoading').hide();
	$('.raidBoss').hide();
	
	$('.notice').hide();
	if(loading) {
		$('#loadingNotice').show();
	} else {
		$('#bettingNotice').show();
	}
	
}

function notifyMe(noticationString) {
	  // Let's check if the browser supports notifications

	  // Let's check whether notification permissions have already been granted
	  if (Notification.permission === "granted") {
	    // If it's okay let's create a notification
	    var notification = new Notification(noticationString);
	  }

	  // Otherwise, we need to ask the user for permission
	  else if (Notification.permission !== "denied") {
	    Notification.requestPermission().then(function (permission) {
	      // If the user accepts, let's create a notification
	      if (permission === "granted") {
	        var notification = new Notification(noticationString);
	      }
	    });
	  }

	  // At last, if the user has denied notifications, and you 
	  // want to be respectful there is no need to bother them any more.
}

resetMatchBlock(false);
pullCurrentData();
connect();