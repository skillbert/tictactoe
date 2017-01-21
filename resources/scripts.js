///<reference path="/runeappslib.js">
"use strict";


var field = null;
var ui = null;
var con = null;
var dispaynet = false;






function start() {
	field = new Field(4, 4, 4);
	ui = new UI(field);
	connect();
	document.body.appendChild(ui.el);
	ui.drawField();
}

function connect() {
	if (con) { con.close();}
	con = new WebSocket("ws://" + location.hostname + ":12345");
	con.onerror = e=>ui.showMessage("connection error: " + e);
	con.onmessage = messageReceived;
	con.onopen = e=>sendMessage("login random" + (Math.random() * 1000 | 0));
}

function messageReceived(e){
	var message = e.data;
	if (dispaynet) { ui.showMessage("<< " + message); }
	var match;
	if (match = message.match(/^waiting$/)) {
		ui.showMessage("Waiting for opponent...");
	}
	if (match = message.match(/^startGame (\w+) (\w+)$/)) {
		field.reset();
		ui.drawField();
		ui.showMessage("New game started. " + match[1] + " VS " + match[2]);
		ui.showMessage(match[1] + "'s turn");
	}
	if (match = message.match(/^placed (onGoing|won|draw) (\d+) (\d+) (\w+) (\w+)$/)) {
		field.place(+match[2], +match[3]);
		ui.drawField();
		if (match[1] == "won") {
			ui.showMessage(match[4] + " won!");
		}
		if (match[1] == "draw") {
			ui.showMessage("Board is full, game is a draw!");
		}
		if (match[1] == "onGoing") {
			ui.showMessage(match[5] + "'s turn");
		}
	}
	if (match = message.match(/^error (\w+)( (.+))?$/)) {
		if (match[1] == "errorMessage") {
			ui.showMessage(match[3]);
		}
		else {
			ui.showMessage("Error: " + match[1]);
		}
	}
}

function sendMessage(mes) {
	if (dispaynet) { ui.showMessage(">> " + mes); }
	con.send(mes);
}

function Field(w,d,h) {
	this.fields = [];
	this.width = w;
	this.depth = d;
	this.height = h;

	var nPlayers = 2;
	var turn = 0;
	var layeroffset = this.width * this.depth;

	this.place = function (x, y) {
		for (var i = x + this.width * y; i < this.fields.length; i += layeroffset) {
			if (this.fields[i] == -1) {
				this.setMark(i, turn);
				return;
			}
		}
		qw("couldn't place piece");
	}

	this.setMark = function (index, mark) {
		this.fields[index] = mark;
		turn = (turn + 1) % nPlayers;
	}

	this.reset = function () {
		for (var a = 0; a < w * d * h; a++) {
			this.fields[a] = -1;
		}
	}

	this.isAvailable = function (index) {
		if (this.fields[index] != -1) { return false; }
		for (var i = index % layeroffset; i < index; i += layeroffset) {
			if (this.fields[i] == -1) { return false; }
		}
		return true;
	}

	this.getIndex = function (x, y, z) {
		return x + y * this.width + z * this.width * this.depth;
	}
	this.getCoord = function (i) {
		return [i % this.width, Math.floor(i / this.width) % this.depth, Math.floor(i / this.width / this.depth)];
	}

	this.reset();
}

function UI(field) {
	var fieldUi = new FieldUI(field);

	var els = {};
	this.el = eldiv("maincontainer", [
		fieldUi.el,
		eldiv("messagecontainer", [
			els.messagebox = eldiv("messagebox"),
			eldiv("inputcontainer", [
				els.input = eldiv("input:input/text"),
				els.inputbutton = eldiv("inputbutton:input/button", { value: " > " })
			])
		])
	]);

	var commandhist = [];
	var commandindex = 0;
	var sendInput = function () {
		var input = els.input.value;
		commandhist.unshift("");
		els.input.value = "";
		sendMessage(input);
	}

	var stepcommand = function (step) {
		commandindex = Math.min(commandhist.length - 1, Math.max(0, commandindex + step));
		els.input.value = commandhist[commandindex]
	}

	els.inputbutton.onclick = sendInput;
	els.input.onkeydown = e => {
		if (e.keyCode == 13) { sendInput(); }

		//suggest command stuff
		if (e.keyCode == 38) { stepcommand(1); }
		else if (e.keyCode == 40) { stepcommand(-1); }
		else { commandhist[0] = els.input.value; commandindex = 0; }
	};


	this.showMessage = function (message) {
		els.messagebox.appendChild(eldiv("message", [message]));
		els.messagebox.scrollTop = els.messagebox.scrollHeight;
	}

	this.drawField = function () {
		fieldUi.draw();
	}

}

function FieldUI(field) {
	var cam = { hor: 0, ver: -Math.PI / 2, rot: 0, fov: 0.6, dist: 500 };
	var cammatrix = null;
	var fieldels = [];
	var dragdist = 0;

	var pieceClicked = function (x, y, e) {
		if (dragdist >= 5) { return;}
		sendMessage("place " + x + " " + y);
	}

	var el = this.el = eldiv("field");
	for (var a in field.fields) {
		fieldels[a] = eldiv("mark");
		fieldels[a].onclick = pieceClicked.bind(null, a % field.width, ((a / field.width | 0) % field.depth));
		if (a < field.width * field.depth) { fieldels[a].classList.add("floor"); }
		el.appendChild(fieldels[a]);
	}

	//cam dragging
	el.onmousedown = function (estart) {
		dragdist = 0;
		estart.preventDefault();
		var xlast = estart.clientX;
		var ylast = estart.clientY;
		var move = function (emove) {
			cam.hor += (emove.clientX - xlast) / 100;
			cam.ver -= (emove.clientY - ylast) / 100;
			dragdist += Math.abs(emove.clientX - xlast) + Math.abs(emove.clientY - ylast);

			xlast = emove.clientX;
			ylast = emove.clientY;

			fixcam();
			draw();
		}
		var mouseup = function () {
			window.removeEventListener("mousemove", move);
			window.removeEventListener("mouseup", mouseup);
		}
		window.addEventListener("mousemove", move);
		window.addEventListener("mouseup", mouseup);
	}
	el.onwheel = function (e) {
		e.preventDefault();
		cam.dist += e.deltaY;
		fixcam();
		draw();
	}

	var draw = this.draw = function () {
		var centerx = el.clientWidth / 2;
		var centery = el.clientHeight / 2;
		var size = 100;
		var spacing = 100;
		for (var x = 0; x < field.width; x++) {
			for (var y = 0; y < field.depth; y++) {
				for (var z = 0; z < field.height; z++) {
					var i = field.getIndex(x, y, z);
					var pos = vmpr([spacing * (x - 1.5), spacing * (y - 1.5), spacing * (z - 1.5), 1], cammatrix);

					var mark = fieldels[i];
					mark.setAttribute("p", "p" + field.fields[i]);
					toggleclass(mark, "available", field.isAvailable(i));

					var scale = 500 / pos[3];
					if (pos[3] < 100) {
						mark.style.display = "none";
					}
					else {
						mark.style.display = "block";

						mark.style.left = centerx + pos[0] * scale + "px";
						mark.style.top = centery + pos[1] * scale + "px";
						mark.style.zIndex = -Math.round(pos[2]);

						mark.style.width = (size * scale) + "px";
						mark.style.height = (size * scale) + "px";
						mark.style.margin = (-size * scale / 2) + "px";
					}
				}
			}
		}
	}

	var fixcam = function () {
		var m = verticleMatrix(cam.hor, cam.ver, cam.rot, cam.fov, cam.dist);
		var translate = [
			-1, 0, 0, 0,
			0, 0, 1, 0,
			0, -1, 0, 0,
			0, 0, 0, 1
		]
		cammatrix = mmpr(translate, m);
	}


	fixcam();
}


function verticleMatrix(hor, ver, rot, fov,dist) {

	var initial = [
		1, 0, 0, 0,
		0, 1, 0, 0,
		0, 0, 1, 0,
		0, 0, 0, 1
	]

	var tr1 = [1, 0, 0, 0, 0, Math.cos(ver), -Math.sin(ver), 0, 0, Math.sin(ver), Math.cos(ver), 0, 0, 0, 0, 1];
	var tr2 = [Math.cos(hor), 0, Math.sin(hor), 0, 0, 1, 0, 0, -Math.sin(hor), 0, Math.cos(hor), 0, 0, 0, 0, 1];
	var tr3 = [Math.cos(rot), -Math.sin(rot), 0, 0, Math.sin(rot), Math.cos(rot), 0, 0, 0, 0, 1, 0, 0, 0, 0, 1];

	var r = mmpr(mmpr(mmpr(initial, tr2), tr1), tr3);
	
	//field of view
	r[3] = r[2] * fov;
	r[7] = r[6] * fov;
	r[11] = r[10] * fov;

	//translations
	r[12] = 0;
	r[13] = 0;
	r[14] = 0;
	r[15] = dist;
	//printmatrix(r);
	//printarray(vmpr([10, 10, 10, 1], r));
	return r;
	print
}

function printarray(arr) {
	var size = arr.length;
	qw("printing array (" + size + ")");
	var str = "";
	for (var b = 0; b < size; b++) {
		var v = arr[b];
		str += (v < 0 ? "" : " ") + v.toFixed(2) + "\t";
	}
	qw(str);
}

function printmatrix(m) {
	var size = Math.sqrt(m.length);
	qw("printing matrix (" + size + "x" + size + ")");
	for (var a = 0; a < size; a++) {
		var str = "";
		for (var b = 0; b < size; b++) {
			var v=m[a * size + b];
			str += (v < 0 ? "" : " ") + v.toFixed(2) + "\t";
		}
		qw(str);
	}
}
