/**
 * 
 */
function setBorderColor(teamName, elementId) {
	var element = document.getElementById(elementId);
	var prefix = '1px solid ';
	switch(teamName) {
		case 'BLUE':
			element.style.border = prefix + "blue";
			break;
		case 'WHITE':
			element.style.border = prefix + 'grey';
			break;
		case 'BLACK':
			element.style.border = prefix + 'black';
			break;
		case 'RED':
			element.style.border = prefix + 'red';
			break;
		case 'PURPLE':
			element.style.border = prefix + 'purple';
			break;
		case 'GREEN':
			element.style.border = prefix + 'green';
			break;
		case 'YELLOW':
			$('#'+elementId).css('border', prefix + 'rgb(204,204,0)'); //default yellow looks hideous
			break;
		case 'BROWN':
			element.style.border = prefix + 'brown';
			break;
		case 'CHAMPION': case 'CHAMP':
			element.style.border = prefix + 'orange';
			break;
	}
}

function setColor(teamName, elementId) {
	var element = document.getElementById(elementId);
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
	}
}

function setElementColors() {
	setBorderForClass('allegianceTable');
	setBorderForClass('allegiancNameColumn');
	setBorderForClass('topPlayerColumn');
	setBorderForClass('statsColumn');
	
	$('.teamName').each(function() {
		var color = this.id.split('_')[0];
		setColor(color, this.id);
	});
}

function setBorderForClass(className) {
	$('.' + className).each(function() {
		var color = this.id.split('_')[0];
		setBorderColor(color, this.id);
	});
}