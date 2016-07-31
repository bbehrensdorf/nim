/**
 * 
 */
var currentPlayer = -1;
var currentRow = -1;
var oldRow = -1;
var startCount = -1;
var oldCount = -1;
var currentCount = -1;
var ready = false;
var gameOver = true;
$('#btnReset').prop('disabled', true);
// var token = '<i aria-hidden="true" class="fa fa-heart fa-3x"></i>';
var token = '<i aria-hidden="true" class="fa fa-star fa-3x"></i>';
var rowValues = [ 2 ];
// var normalColor = "rgb(49, 112, 143)";
var normalColor = "rgb(60, 118, 60)";
var enhancedColor = "rgb(169, 68, 95)";

function printMessage(message) {
	$('#message').html(message);

}

function setGameOver(status) {
	var btn = $("#btnSubmit");
	if (status == true) {
		btn.removeClass("btn-primary");
		btn.removeClass("btn-warning");
		btn.addClass("btn-success");
		btn.html("Neues Spiel");
		$('#btnBack').prop('disabled', false);
	} else {
		btn.removeClass("btn-success");
		btn.addClass("btn-primary");
		btn.html("Zug ausführen");
		$('#btnBack').prop('disabled', true);
	}
	gameOver = status;
}

function setCurrentRow(value) {
	if (value==-1) {
		$('#btnReset').prop('disabled', true);
	} else {
		$('#btnReset').prop('disabled', false);
	}
	currentRow=value;
}


function fillNimTable(rowValues) {
	var theTable = document.getElementById("nimtable");
	var rowCount = theTable.rows.length;
	var colCount = theTable.rows[0].cells.length;
	for (var i = 0; i < rowCount; i++) {
		for (j = 1; j < colCount; j++) {
			theTable.rows[i].cells[j].style.color = normalColor;
			theTable.rows[i].cells[j].innerHTML = token;
			if (j >= rowValues[i]) {
				break;
			}
		}
	}

}

function fillNimRow(rowNum, rowValue) {
	var theTable = document.getElementById("nimtable");
	var theCells = theTable.rows[rowNum].cells;
	var rowCount = theTable.rows.length;
	var colCount = theTable.rows[0].cells.length;
	for (var i = 1; i < theCells.length; i++) {
		if (i <= rowValue) {
			theCells[i].innerHTML = token;
		} else {
			theCells[i].innerHTML = "";
		}
	}
}

function onError(data, status) {
	// handle an error
	// console.log(data, status);
	console.log("Da ist was Schiefgegangen");

}

function initGame() {
	$.ajax({
		type : "POST",
		url : "/Nim/Game",
		cache : false,
		data : {
			'action' : 'init',
		},
		success : onSuccess,
		error : onError
	});
	$('btnBack').prop('disabled', true);
}

function doMove() {
	var btn = $("#btnSubmit");
	if (currentPlayer == 2) {
		btn.html("Computer zieht");
		btn.toggleClass("btn-primary btn-warning");
	} else {
		btn.html("Zug ausführen");
		btn.toggleClass("btn-primary btn-warning");
	}

}

function onSuccess(data, status) {
	console.log("Data: " + data + "\nStatus: " + status);
	var obj = JSON.parse(data);
	var outMessage = "";
	switch (obj.action) {
	case "init_nimtable":
		rowValues = obj.values;
		fillNimTable(rowValues);
		outMessage = obj.message;
		currentPlayer = obj.player;
		setGameOver(false);
		ready = true;
		break;
	case "confirm_humanmove":
		outMessage = obj.message;
		currentPlayer = obj.player;
		doMove();
		if (obj.gameover) {
			setGameOver(true);
		}
		ready = true;
		break;
	case "confirm_computermove":
		outMessage = obj.message;
		currentPlayer = obj.player;
		fillNimRow(obj.move[0], obj.move[1]);
		doMove();
		if (obj.gameover) {
			setGameOver(true);
		}
		ready = true;
		break;
	default:
		outMessage = "so und so ...";
	}
	$("#message").html(outMessage);
}

function colorizeRow(rowNum, color, all) {
	if (typeof all === 'undefined') {
		all = false;
	}
	theTable = document.getElementById('nimtable');
	var theCells = theTable.rows[rowNum].cells;
	for (var i = 1; i < theCells.length; i++) {
		if (theCells[i].innerHTML == "" && !all) {
			break;
		}
		theCells[i].style.color = color;
	}
}

function prepareRowForSubmitting(row) {
	var theTableCells = document.getElementById("nimtable").rows[row].cells;
	for (var i = 0; i < theTableCells.length; i++) {
		theTableCells[i].style.color = normalColor;
		if (i > currentCount) {
			theTableCells[i].innerHTML = "";
		}
	}
	setCurrentRow(-1);
	oldRow = -1;
	startCount = -1;
	oldCount = -1;
	currentCount = -1;
}

$(function() {
	$('#btnBack').prop('disabled', true);
	initGame();
	$(".nim-cell")
			.click(
					function() {
						// alert(currentRow);
						if (gameOver)
							return;
						if (!ready)
							return;
						if (currentPlayer != 1) {
							printMessage('Der Computer ist dran. Klick auf "Computer zieht"');

							return;
						}
						var theColor = $(this).css("color");
						/*
						 * if (theColor == 'rgb(0, 0, 0)'){ $( this
						 * ).css('color','rgb(255, 0, 0)'); } else { $( this
						 * ).css('color','rgb(0, 0, 0)'); }
						 */
						var col = $(this).parent().children().index($(this));
						var row = $(this).parent().parent().children().index(
								$(this).parent());
						var theTableCells = document.getElementById("nimtable").rows[row].cells;
						var cellContent = theTableCells[col].innerHTML;
						if ((cellContent != "")
								&& (currentRow == -1 || row == currentRow)) {
							if (currentRow == -1) {
								startCount = rowValues[row];
								oldCount = startCount;
								colorizeRow(row, enhancedColor);
							} else {
								oldCount = currentCount;
							}
							currentCount = col - 1;
							oldRow = currentRow;
							setCurrentRow(row);
							for (var i = col; i < theTableCells.length; i++) {
								if (theTableCells[i].innerHTML == "") {
									break;
								}
								// theTableCells[i].innerHTML.fadeTo(0,0.2)
								theTableCells[i].innerHTML = "&times;";
							}
							if (currentCount == 0) {
								message = "Du möchtest Reihe "
										+ (currentRow + 1) + " leeren.";
							} else {
								var weg = oldCount - currentCount;
								var message = "Du möchtest in der "
										+ (currentRow + 1)
										+ ". Reihe  "
										+ ((weg == 1) ? "einen Stein" : weg
												+ " Steine")
										+ " wegnehmen.<br>"
										+ "Es bliebe"
										+ ((currentCount == 1) ? "" : "n")
										+ " dann noch "
										+ ((currentCount == 1) ? "ein Stein"
												: currentCount + " Steine")
										+ ".";
							}
							printMessage(message);
						} else if (currentRow >= 0 && row != currentRow
								&& currentCount > 0) {
							var message = "Deine gewählte Reihe ist  Reihe "
									+ (currentRow + 1)
									+ ". Nur dort kannst du weitere Steine wegnehmen.";
							printMessage(message);

						}

					});

	$("#btnReset").click(function() {
		if (gameOver) {
			return;
		}
		if (currentPlayer != 1) {
			printMessage('Der Computer ist dran. Klick auf "Computer zieht"');
			return;
		}
		if (currentRow >= 0) {
			colorizeRow(currentRow, normalColor, true);
			fillNimRow(currentRow, startCount);
			setCurrentRow(-1);
			currentCount = -1;
			printMessage("Bitte in eine Reihe klicken!");

		}

	});

	$("#btnSubmit").click(function() {
		if (gameOver) {
			initGame();
		} else {
			if (currentPlayer == 2) {
				$.ajax({
					type : "POST",
					url : "/Nim/Game",
					cache : false,
					data : {
						action : 'computermove',
						row : currentRow,
						count : currentCount
					},
					success : onSuccess,
					error : onError
				});

			} else {
				if (currentRow == -1) {
					printMessage("Du hast noch keine Steine weggenommen!!!");
				} else {
					ready = false;
					var tmpCurrentRow = currentRow;
					var tmpCurrentCount = currentCount;
					prepareRowForSubmitting(currentRow);
					$.ajax({
						type : "POST",
						url : "/Nim/Game",
						cache : false,
						data : {
							action : 'humanmove',
							row : tmpCurrentRow,
							count : tmpCurrentCount
						},
						success : onSuccess,
						error : onError
					});

				}
			}
		}
	});
	$('#btnBack').click(function() {
		if (!gameOver) {
			return;
		} 
		window.location.href = "/Nim";
	});


});
