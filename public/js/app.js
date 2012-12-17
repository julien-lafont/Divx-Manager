// Codé à l'arrache complète ! Et j'assume ;)
$(function() {
	loadMenu();
	initOpenDirAction();
	initReorderLinks();
});

var lastPath;
var lastType;

function load(path, type, success, order) {
	if (order == undefined) order = "";

	if (type == "files") {
		loadFiles(path, success, order);
	} else {
		loadFolders(path, success, order);
	}
	lastPath = path;
	lastType = type;
}

function loadFiles(path, success, order) {
	$.get("/api/list/files/"+path+"?"+order, function(files) {
		var $base = $("#list-body").empty();
		_.each(files, function(file) {
			addFile($base, file);
		});
		success();
	});
}

function loadFolders(path, success, order) {
	$.get("/api/list/folders/"+path+"?"+order, function(files) {
		var $base = $("#list-body").empty();
		_.each(files, function(file) {
			addFile($base, file);
		});
		success();
	});
}

function loadMenu() {
	$.get("/api/roots", function(roots) {
		var $base = $("#folders-menu");
		_.each(roots, function(root) {
			$base.append("<li><a href='#' data-directory='"+root.dir+"' data-type='"+root.type+"' class='open-dir'>"+root.name+"</a></li>");
		});
		$("#folders-menu a:first").trigger('click');
	});
}

function addFile($base, e) {
	var html = "<tr>";
	var lien = "";
	if (e.isFile) {
		lien = "<a href='/api/download/"+e.path+"' title='"+e.rawName+"'>"+e.name+"</a>";
	} else {
		lien = "<a href='#' data-directory='"+e.path+"' class='open-dir' title='"+e.rawName+"'>"+e.name+"</a>";
	}

	// Icone
	html += "<td>";
	var icone = "file.png";
	if (e.extension == "avi" || e.extension == "mkv") icone = "file-avi.png";
	else if (e.extension == "mpg" || e.extension == "mp4") icone = "file-mp4.png";
	else if (e.extension == "mp3") icone = "file-mp3.png";
	else if (e.extension == "mov") icone = "file-mov.png";
	else if (!e.isFile) icone = "folder.png";
	html += "<img src='/assets/images/icones/"+icone+"' alt='"+e.extension+"' /> ";
	html += "</td>";

	// Nom
	html += "<td>";
	html += lien;
	if (e.details.year) {
		html += " (<strong>"+e.details.year+"</strong>)";
	}
	html += "</td>";
	
	// Série
	html += "<td>";
	if (e.details.season) {
		html += "<span class='label label-inverse'>S"+e.details.season+"</span> ";
	}
	if (e.details.episode) {
		html += "<span class='label label-inverse'>E"+e.details.episode+"</span>";
	}
	html += "</td>";
	
	html += "<td>";
	html += "<a href='http://www.allocine.fr/recherche/?q="+e.name+"' target='_blank'><img src='/assets/images/allocine.ico' height='15' width='15' /></a>&nbsp;&nbsp;";

	// Qualité
	if (e.details.quality == "720p") html += "<span class='label label-warning'>720p</span> ";
	if (e.details.quality == "1080p") html += "<span class='label label-important'>1080p</span> ";

	// Langue
	if (e.details.lang == "vostfr") html += "<span class='label'>vostfr</span> ";
	if (e.details.lang == "vo")  html += "<span class='label label-success'>vo</span> ";

	html += "</td>";

	// Taille
	html += "<td>"+e.size+"</td>";

	// Date ajout
	html += "<td>"+e.lastModified+"</td>";

	html += "</tr>";
	$base.append(html);
}

function initOpenDirAction() {
	$("#folders-menu").on('click', ".open-dir", function() {
		var dir = $(this).data('directory');
		var type = $(this).data('type');
		var success = _.bind(function() {
			$("#folders-menu li").removeClass("active");
			$(this).parent().addClass("active");
			$("#n2").text($(this).text()).addClass('active');
			$("#n3").text("").removeClass('active');
			resetOrder();
		}, this);
		if (type == 'TvShow') {
			load(dir, 'folders', success);
		} else {
			load(dir, 'files', success);
		}
	});

	$(".breadcrumb").on('click', ".open-dir", function() {
		var dir = $(this).data('directory');
		var type = $(this).data('type');
		var success = _.bind(function() {
			$("#n2").text($(this).text()).addClass('active');
			$("#n3").text("").removeClass('active');
			resetOrder();
		}, this);
		if (type == 'TvShow') {
			load(dir, 'folders', success);
		} else {
			load(dir, 'files', success);
		}
	});

	$("#list-body").on('click', ".open-dir", function() {
		var dir = $(this).data('directory');
		var success = _.bind(function() {
			$("#n2").html($("#folders-menu li.active").html() + "<span class='divider'>/</span>").removeClass('active');
			$("#n3").text($(this).text()).addClass('active');
			resetOrder();
		}, this);
		load(dir, 'files', success);
	});
}

function initReorderLinks() {
	$(".link-reload").on('click', function() {
		var tri = $(this).data('order');

		$(".link-reload i").remove();
		$(this).prepend("<i class='icon-chevron-"+(tri=="asc" ? "up" : "down")+"'></i>");
		$(this).data('order', (tri == 'asc' ? 'desc' : 'asc'));

		var order = "column="+$(this).data('column')+"&order="+tri;
		load(lastPath, lastType, function() { }, order);
	});
}

function resetOrder() {
	$(".link-reload").each(function() {
		$(this).data('order', 'asc');
	});
	$(".link-reload i").remove();
}