//copied from my personal util library

//shorthand to create an element of the given class, properties and children
function eldiv(strClass, objAttr, arrayChildren) {
	var classname, attr, children, tag, tagarg, el, childfrag;
	//reorder arguments
	var argi = 0;
	if (typeof arguments[argi] == "string") {
		var typedata = arguments[argi++].split(":");
		classname = typedata[0];
		var tagdata = typedata[1] ? typedata[1].split("/") : [];
		tag = tagdata[0];
		tagarg = tagdata[1];
	}
	if (typeof arguments[argi] == "object" && !Array.isArray(arguments[argi]) && !(arguments[argi] instanceof DocumentFragment)) { attr = arguments[argi++]; }
	if (typeof arguments[argi] == "object" && Array.isArray(arguments[argi])) { children = arguments[argi++]; }
	else if (typeof arguments[argi] == "object" && arguments[argi] instanceof DocumentFragment) { childfrag = arguments[argi++]; }
	attr = attr || {};
	if (classname) { attr["class"] = classname; }

	//start actual work
	tag = attr && attr.tag || tag || "div";
	if (tag == "input" && tagarg) { attr.type = tagarg; }
	if (tag == "frag") { el = document.createDocumentFragment(); }
	else {
		var el = (attr && attr.namespace ? document.createElementNS(attr.namespace, tag) : document.createElement(tag));
	}
	if (attr) {
		for (var a in attr) {
			if (attr[a] === false || attr[a] == null || a == "tag" || a == "namespace") { continue; }
			if (a.substr(0, 2) == "on") { el[a] = attr[a]; }
			else { el.setAttribute(a, attr[a]); }
		}
	}
	if (children != null && children != undefined) {
		if (!Array.isArray(children)) { children = [children]; }
		for (var a in children) {
			if (children[a] == null) { continue; }
			if (typeof children[a] != "object") { el.appendChild(document.createTextNode(children[a].toString())); }
			else { el.appendChild(children[a]); }
		}
	}
	else if (childfrag != null) {
		el.appendChild(childfrag);
	}
	return el;
}

//vector-matrix product
function vmpr(v, m) {
	var a, b, c, r, vl;
	r = [];
	vl = v.length;
	for (a = 0; a < vl; a++) {
		r[a] = 0;
		for (b = 0; b * vl < m.length; b++) {
			r[a] += v[b] * m[a + b * vl];
		}
	}
	return r;
}

//matrix-matrix produxt
function mmpr(m1, m2) {
	var a, b, c, d, size, r;
	size = Math.sqrt(m1.length);
	r = [];
	for (b = 0; b < size; b++) {
		for (a = 0; a < size; a++) {
			r[a + size * b] = 0;
			for (c = 0; c < size; c++) {
				r[a + size * b] += m1[c + size * b] * m2[a + size * c];
			}
		}
	}
	return r;
}

//shorthand to toggle classes of an el on or off
function toggleclass(el, classname, state) {
	if (typeof el == "string") { el = elid(el); }
	if (state == undefined) { state = !el.classList.contains(classname); }
	if (state) { el.classList.add(classname); }
	else { el.classList.remove(classname); }
	return state;
}