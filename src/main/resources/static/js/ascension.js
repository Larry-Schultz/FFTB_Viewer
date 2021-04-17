
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