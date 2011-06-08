$(document).ready(function() {
	thisPage.init();

	$(window).load(function() {
		thisPage.run();
	});
});




var thisPage = {
	'init': function() {
		$("#calendarSelect select option:first").attr("selected", true);
		$("#calendarSelect select").change(thisPage.calendarSelected);
		$("#calendarHeader #importButton").click(thisPage.budgetImport);
		$("#calendar #removeCalanderButton").click(thisPage.removeCalendar)
		$("#calendar .month .day").click(thisPage.dayClick);
		$("#calendar .month .day .tooltip-arrow")
			.removeClass()
			.addClass("tooltip-arrow")
			.addClass("tooltip-arrow-top-right");
		$("#calendar .month .day .tooltip-arrow-border")
			.removeClass()
			.addClass("tooltip-arrow-border")
			.addClass("tooltip-arrow-border-top-right");
		$("#calendar .month .day .mask").tooltip({
			effect: "slide",
			relative: true,
			position: "bottom right",
			offset: [15, -50],
			onBeforeShow: function(event) {
				if ( ! this.getTrigger().parent(".day").hasClass("absentDay") || 
					 $("#calendarHeader .title").html() == "Master Calendar") {
					return false;
				}
				
			}
		});
		
		$("#header #searchButton").click(thisPage.search);
		$("#searchResultsContainer .label .title .ui-icon").click(thisPage.employmentTypeSelect);
		$("#header #searchQuery").keypress(function(event) {
			if (event.which == '13') {
				$("#header #searchButton").click();
			}
		});
		$("#calendarHeader textarea").keypress(function(event) {
			if (event.ctrlKey && event.shiftKey && event.which == '13') {
				$("#calendarHeader #importButton").click();
			}
		});
		var uiButtonDom = $(".ui-button");
		uiButtonDom.mousedown(thisPage.mouseDown);
		uiButtonDom.mouseup(thisPage.mouseUp);
		uiButtonDom.hoverIntent(thisPage.hoverOn, thisPage.hoverOff);
		
		
		var icon = $("#searchResultsContainer .label #icon");
		icon.mousedown(thisPage.mouseDown);
		icon.mouseup(function() {
			$(this).removeClass("ui-state-active");
			var childDom = $(this).children(".ui-icon");
			if (childDom.hasClass("ui-icon-arrowthickstop-1-s")) {
				thisPage.showSearchResults();
			}
			else if (childDom.hasClass("ui-icon-arrowthickstop-1-n")) {
				thisPage.hideSearchResults();
			}
		});
		icon.hoverIntent(thisPage.hoverOn, thisPage.hoverOff);
		
		var nextYearDom = $("#calendar .year .ui-icon-circle-triangle-e");
		nextYearDom.click(function() {
			var year = parseInt($("#calendar .year .text").html());
			thisPage.yearChange(year + 1);
		});
		var prevYearDom = $("#calendar .year .ui-icon-circle-triangle-w");
		prevYearDom.click(function() {
			var year = parseInt($("#calendar .year .text").html());
			thisPage.yearChange(year - 1);
		});
		
		
		$("#calendar .month .day").bind("contextmenu", thisPage.displayDayMenu);
		
		$("#appNavBar #moreNavItem").click(function() {
			menuItemList = new Array();
			menuItemList.push({
				'name': 'CSV Import ' + $("#searchResultsContainer .label .title .text").html(), 
				'action': thisPage.csvImport,
				'data': {}
			});
			menuItemList.push({
				'name': 'CSV Export ' + $("#searchResultsContainer .label .title .text").html(), 
				'action': thisPage.csvExport,
				'data': {}
			});

    		thisPage.displayPopupMenu(this, menuItemList);
		});
		
		thisPage.initDialog();
		
		thisPage.loadMasterCalendar();
		
		$("#appNavBar #userNavItem").click(thisPage.displayUserMenu);
		
		$("input[type=text], select").focus(function() {
			$(this).addClass("auraSelected");
		});
		$("input[type=text], select").focusout(function() {
			thisPage.clearAura($(this));
		});
		
		$("#searchResultsContainer .tableContainer").resizable({ 
			handles: 's',
			maxHeight: 500
		});
	},
	
	'initDialog': function() {
		/*
		$("#addAbsenceDialog .ui-state-error").tooltip({ 
			effect: 'slide',
			relative: true
		});
		*/
		var absenceDialogSelect = $("#addAbsenceDialog #nameField select");
		absenceDialogSelect.focus(function() {
			$(this).css("width", "auto");
			$("#addAbsenceDialog #nameField input").css("width", "20px");
		});
		absenceDialogSelect.change(function() {
			$("#addAbsenceDialog #nameField input").val("");
			if ($(this).val() == "newCalendar") {
				$("#addAbsenceDialog #nameField input").focus();
			}
		});
		
		var absenceDialogInput = $("#addAbsenceDialog #nameField input");
		absenceDialogInput.focus(function() {
			$(this).css("width", "auto");
			$("#addAbsenceDialog #nameField select").css("width", "20px");
			$("#addAbsenceDialog #nameField select option[value='newCalendar']").attr("selected", true);
		});
		
		$("#addAbsenceDialog").dialog({ 
			title: "Absence",
			minWidth: 500,
			autoOpen: false,
			closeOnEscape: true,
			modal: true,
			resizable: false,
			open: function(event, ui) {
				// clear away ui-button-text class so buttons are so big.
				$(".ui-dialog button span").removeClass("ui-button-text");
				
				// get and display date
				var year = parseInt($("#calendar .year .text").html());
				$("#addAbsenceDialog .year").val(year);
				var month = parseInt($("#calendar #selectedMonth").val());
				var day = parseInt($("#calendar #selectedDay").val());
				var date = new Date(year, month, day);
				$(".ui-dialog .date").html(date.toDateString());
				
				// employmentType
				$(".ui-dialog .employmentType").html($("#searchResultsContainer .label .title .text").html());
				
				// copy employee/calendar select box into dialog and add "New Calendar"
				// to allow for new employee/calendar creation
				var dialogSelectDom = $("#addAbsenceDialog #nameField select");
				dialogSelectDom.html($("#calendarHeader #calendarSelect select").html());
				var html = '<option value="newCalendar">New Calendar</option>';
				dialogSelectDom.children(":first").after(html);
				
				// hide all errors
				$("#addAbsenceDialog .ui-state-error").hide();
				
				// clear input fields
				$("#addAbsenceDialog #nameField input").val("");
				$("#addAbsenceDialog #reasonField input").val("");
				$("#addAbsenceDialog #hoursField input").val("");
				
				
				var curSelected = $("#calendarSelect select option:selected").val();
				if (curSelected != "" && curSelected != "newCalendar") {
					dialogSelectDom.children("option[value='" + curSelected + "']").attr("selected", true);
				}
				
				// only display hour field for non-Teachers
				if ($("#searchResultsContainer .label .title .text").html().trim() == "Teachers") {
					$("#addAbsenceDialog #hoursField").parent("tr").hide();
				}
				else {
					$("#addAbsenceDialog #hoursField").parent("tr").show();
				}
			},
			buttons: {
				'Add': function() {
					var curSelect = $("#addAbsenceDialog table select option:selected").html();
					var employmentType = $("#searchResultsContainer .label .title .text").html();
					var name = $("#addAbsenceDialog table .name").val();
					var validName = (curSelect != "Master Calendar");
					var emptyName = isEmpty(name);
					var nameErrorDom = $("#addAbsenceDialog table td #nameError");
					var title = "";
					
					var selectDom = $("#addAbsenceDialog #nameField select");
					var nameDom = $("#addAbsenceDialog #nameField .name");
					var reasonDom = $("#addAbsenceDialog #reasonField .reason");
					
					if (curSelect == "Master Calendar") {
						title = "Please select a calendar or create new one.";
						
						$("#addAbsenceDialog #nameField .tooltip .tooltip-arrow")
							.removeClass()
							.addClass("tooltip-arrow")
							.addClass("tooltip-arrow-left-top");
						$("#addAbsenceDialog #nameField .tooltip .tooltip-arrow-border")
							.removeClass()
							.addClass("tooltip-arrow-border")
							.addClass("tooltip-arrow-border-left-top");
						$("#addAbsenceDialog #nameField .tooltip .tooltip-content").html(title);
						nameErrorDom.tooltip({
							effect: "slide",
							relative: true,
							position: "bottom right",
							offset: [-30, 10]
						});
						
						thisPage.clearAura(selectDom);
						thisPage.clearAura(nameDom);
						selectDom.addClass("auraRed");
						nameErrorDom.show();
						
						
					}
					else if ( curSelect == "New Calendar") {
						// validate name
						
						var tmp = name.split(",");
						// contains only 1 comma; only alphanumeric; spaces; dashes; apostrophes;
						var regex = /^[a-zA-Z0-9', \-]+$/g
						validName = validName && tmp.length < 3;
						validName = validName && (name.match(regex) != null);
						validName = validName && name.length > 0;
						validName = validName && ! emptyName;
						
						if (! validName) {
							title = "Allowed characters: Numbers, Letters, Spaces, Dashes, and up to one Comma.<br><br>"
								  +	"Example: LAST, FIRST";
							
							$("#addAbsenceDialog #nameField .tooltip .tooltip-arrow")
								.removeClass()
								.addClass("tooltip-arrow")
								.addClass("tooltip-arrow-left-top");
							$("#addAbsenceDialog #nameField .tooltip .tooltip-arrow-border")
								.removeClass()
								.addClass("tooltip-arrow-border")
								.addClass("tooltip-arrow-border-left-top");
							$("#addAbsenceDialog #nameField .tooltip .tooltip-content").html(title);
							nameErrorDom.tooltip({
								effect: "slide",
								relative: true,
								position: "bottom right",
								offset: [-30, 10]
							});
							
						
							thisPage.clearAura(selectDom);
							thisPage.clearAura(nameDom);
							nameDom.addClass("auraRed");
							nameErrorDom.show();
							
						}
						else {
							nameErrorDom.hide();
							thisPage.clearAura(selectDom);
							thisPage.clearAura(nameDom);
						}
						
					}
					
					
					// validate reason
					var reason = $("#addAbsenceDialog table .reason").val();
					var reasonErrorDom = $("#addAbsenceDialog table td #reasonError");
					var emptyReason = isEmpty(reason);
					var regex = /^[0-9]+$/g
					var validReason = ( ! emptyReason ) && reason.match(regex);
					if (! validReason ) {
						var title = emptyReason ? "Please enter Reason.<br><br>" : "";
						title += "Numbers only.";
						
						$("#addAbsenceDialog #reasonField .tooltip .tooltip-arrow")
							.removeClass()
							.addClass("tooltip-arrow")
							.addClass("tooltip-arrow-left-top");
						$("#addAbsenceDialog #reasonField .tooltip .tooltip-arrow-border")
							.removeClass()
							.addClass("tooltip-arrow-border")
							.addClass("tooltip-arrow-border-left-top");
						$("#addAbsenceDialog #reasonField .tooltip .tooltip-content").html(title);
						reasonErrorDom.tooltip({
							effect: "slide",
							relative: true,
							position: "bottom right",
							offset: [-30, 10]
						});
						
						reasonDom.addClass("auraRed");
						reasonErrorDom.show();
						
					}
					else {
						reasonErrorDom.hide();
						thisPage.clearAura(reasonDom);
					}
					
					// validate hours
					var hoursDom = $("#addAbsenceDialog table .hours");
					var hours = (employmentType == "Teachers") ? "7" : hoursDom.val();
					var hoursErrorDom = $("#addAbsenceDialog table td #hoursError");
					var emptyHours = isEmpty(hours);
					var regex = /^[0-9]+$/g
					var validHours = ( ! emptyHours ) && hours.match(regex);
					if (! validHours ) {
						var title = emptyHours ? "Please enter Hours.<br><br>" : "";
						title += "Numbers only.";
						
						$("#addAbsenceDialog #hourField .tooltip .tooltip-arrow")
							.removeClass()
							.addClass("tooltip-arrow")
							.addClass("tooltip-arrow-left-top");
						$("#addAbsenceDialog #hourField .tooltip .tooltip-arrow-border")
							.removeClass()
							.addClass("tooltip-arrow-border")
							.addClass("tooltip-arrow-border-left-top");
						$("#addAbsenceDialog #hourField .tooltip .tooltip-content").html(title);
						hoursErrorDom.tooltip({
							effect: "slide",
							relative: true,
							position: "bottom right",
							offset: [-30, 10]
						});
						
						hoursDom.addClass("auraRed");
						hoursErrorDom.show();
					}
					else {
						hoursErrorDom.hide();
					}
					
					
					if (validReason && validName && validHours) {
						var date = thisPage.getSelectedDate();
						var offset = date.getTimezoneOffset() / 60 * -1;
						date.setHours(offset, 0, 0, 0);
						
						name = emptyName ? curSelect : name.toUpperCase();
						
						var data = {
							'type': 'POST',
							'employmentType': $("#searchResultsContainer .label .title .text").html(),
							'date': date.getTime(),
							'reason': parseInt(reason),
							'name': name,
							'hours': parseInt(hours)
						}
						querySite('add', data, function(absenceList, textStatus, jqXHR) {
							var curSelect = $("#addAbsenceDialog table select option:selected").html();
							
							thisPage.fillInCalendarSelect(absenceList, name);
							
							$("#addAbsenceDialog").dialog("close");
							$("#calendarSelect select").change();
						})
					}
					
				},
				'Cancel': function() {
					$(this).dialog("close");
				}
			}
		});
	},
	'clearAura': function(dom) {
		dom.removeClass("auraSelected");
		dom.removeClass("auraRed");
		dom.removeClass("auraGreen");
		dom.removeClass("auraLightBlue");
	},
	'employmentTypeSelect': function() {
		var menuItemList = new Array();
		var curTitle = $("#searchResultsContainer .label .title .text").html();
		if (curTitle != "Classified Employees") {
			menuItemList.push({
				'name': 'Classified Employees',
				'action': function() {
					thisPage.hideSearchResults();
					$("#searchResultsContainer .label span.text").html("Search Results:");
					$("#searchResults").html("");
					
					var textDom = $("#searchResultsContainer .label .title .text");
					textDom.html("Classified Employees");
					textDom.removeClass("teacherView");
					textDom.addClass("classifiedEmployeeView");
					var bodyDom = $("body");
					bodyDom.removeClass();
					bodyDom.addClass("classifiedBody");
					$("#calendarHeader .header .text").hide();
					$("#calendarHeader .header #importButton").hide();
					//$("#calendarHeader .header #calendarSelect select").change();
					var data = {
						'type': 'POST',
						'query': "",
						'employmentType': $("#searchResultsContainer .label .title .text").html()
					};
					querySite('search', data, function(absenceList, textStatus, jqXHR) {
						thisPage.fillInCalendarSelect(absenceList, "");
						//thisPage.fillInMasterCalendar(absenceList);
						$("#calendarHeader #calendarSelect select").change();
					});
				},
				'data':{}
			});
		}
		if (curTitle != "Teachers") {
			menuItemList.push({
				'name': 'Teachers',
				'action': function() {
					thisPage.hideSearchResults();
					$("#searchResultsContainer .label span.text").html("Search Results:");
					$("#searchResults").html("");
					
					var textDom = $("#searchResultsContainer .label .title .text");
					textDom.html("Teachers");
					textDom.removeClass("classifiedEmployeeView");
					textDom.addClass("teacherView");
					var bodyDom = $("body");
					bodyDom.removeClass();
					bodyDom.addClass("teacherBody");
					$("#calendarHeader .header .text").show();
					$("#calendarHeader .header #importButton").show();
					//$("#calendarHeader .header #calendarSelect select").change();
					var data = {
						'type': 'POST',
						'query': "",
						'employmentType': $("#searchResultsContainer .label .title .text").html()
					};
					querySite('search', data, function(absenceList, textStatus, jqXHR) {
						thisPage.fillInCalendarSelect(absenceList, "");
						//thisPage.fillInMasterCalendar(absenceList);
						$("#calendarHeader #calendarSelect select").change();
						
					});
				},
				'data':{}
			});
		}
		
		thisPage.displayPopupMenu(this, menuItemList);
	},
	'removeCalendar': function() {
		var data = {
			'type': 'POST',
			'name': $("#calendarHeader .header .title").html(),
			'employmentType': $("#searchResultsContainer .label .title .text").html()
		};
		querySite('remove', data, function(absenceList, textStatus, jqXHR) {
			var name = '';
			$.each(absenceList, function(index) {
				name = this.name;
				thisPage.displaySearchResult(this, index);
			});
			$("#calendarSelect select option[value='']").attr("selected", true);
			$("#calendarSelect select option[value='" + name + "']").remove();
			$("#calendarSelect select").change();
		});
	},
	'getSelectedDate': function() {
		var year = parseInt($("#calendar .year .text").html());
		var month = parseInt($("#calendar #selectedMonth").val());
		var day = parseInt($("#calendar #selectedDay").val());
		var date = new Date(year, month, day);
		
		return date;
	},
	'loadMasterCalendar': function() {
		var data = {
			'type': 'POST',
			'searchBy': 'year',
			'year': parseInt($("#calendar .year .text").html()),
			'employmentType': $("#searchResultsContainer .label .title .text").html()
		};
		querySite('search', data, function(absenceList, textStatus, jqXHR) {
			thisPage.fillInMasterCalendar(absenceList);
		});
	},
	'yearChange': function(year) {
		$("#calendar .year .text").html(year);
		$("#calendar .month .day").attr("title", "");
		$("#calendar .weekend").removeClass("weekend");
		$("#calendar .absentDay").removeClass("absentDay");
		$("#calendar .day .mask").removeClass("formSubmitted");
		//$("#calendarSelect select option[value='']").attr("selected", true);
		$("#calendar .dayPlaceHolder").remove();
		
		var date = new Date(year, 01, 01);
		var isLeapYear = new Date(year,1,29).getDate() == 29;
		$.each($("#calendar .month"), function() {
			var index = parseInt($(this).attr("id").replace("month_", ""));
			date.setMonth(index);
			date.setDate(1);
			var firstDayIndex = date.getDay();
			var firstDayDom = $(this).children("#day_1");
			for (i = 0; i < firstDayIndex; i++) {
				var html = '<div class="day dayPlaceHolder"><div class="mask">&nbsp;</div></div>'
				firstDayDom.before(html);
			}
			if (index == 1) {
				
				if (isLeapYear) {
					var html = '<div class="day dayOfMonth" id="day_29">'
							 +		'<input class="absenceId" type="hidden" value="">'
							 + 		'<div class="mask">'
							 +			'29'
							 +		'</div>'
							 + '</div>';
					$(this).children("#day_28").after(html);
				}
				else {
					$(this).children("#day_29").remove();
				}
			}
			$.each($(this).children(".dayOfMonth"), function() {
				date.setDate(parseInt($(this).children(".mask").html()));
				var weekDay = date.getDay();
				if (weekDay == 0 || weekDay == 6) {
					$(this).addClass("weekend");
				}
			});
			
		});
		$("#calendarHeader #calendarSelect select").change();
	},
	'run': function() {
		
	},
	'mouseDown': function() {
		$(this).addClass("ui-state-active");
	},
	'mouseUp': function() {
		$(this).removeClass("ui-state-active");
	},
	'hoverOn': function() {
		$(this).addClass("ui-state-hover");
	},
	'hoverOff': function() {
		$(this).removeClass("ui-state-hover");
	},
	'hideSearchResults': function() {
		var searchResultsDom = $("#searchResultsContainer .tableContainer");
		var minHeight = parseInt(searchResultsDom.css("min-height").replace("px",""));		
		var height = parseInt(searchResultsDom.css("height").replace("px",""));
		if (height != minHeight) {
			searchResultsDom.animate({"height": minHeight}, 250, function(){});
		}
		var icon = $("#searchResultsContainer .label span#icon");
		icon.removeClass("ui-state-active");
		var childDom = icon.children(".ui-icon");
		childDom.removeClass("ui-icon-arrowthickstop-1-n");
		childDom.addClass("ui-icon-arrowthickstop-1-s");
	},
	'showSearchResults': function() {
		var searchResultsDom = $("#searchResultsContainer .tableContainer");
		var maxHeight = 200;
		var height = parseInt(searchResultsDom.css("height").replace("px",""));
		if (height ==  parseInt(searchResultsDom.css("min-height").replace("px",""))) {
			searchResultsDom.animate({"height": maxHeight}, 250, function(){});	
		}
		var icon = $("#searchResultsContainer .label span#icon");
		icon.removeClass("ui-state-active");
		var childDom = icon.children(".ui-icon");
		childDom.removeClass("ui-icon-arrowthickstop-1-s");
		childDom.addClass("ui-icon-arrowthickstop-1-n");
	},
	'clearSearchResults': function() {
		
		var html = '<tr>'
				 + 		'<th id="formSubmitted" class="ui-widget-header">Form</th>'
				 +		'<th id="date" class="ui-widget-header">Date</th>'
				 +		'<th id="name" class="ui-widget-header">Name</th>'
				 +		'<th id="info" class="ui-widget-header">Info</th>'
				 + '</tr>';
		$("#searchResultsContainer #searchResults").html(html);
		
		
	},
	'displaySearchResult': function(absence, index) {
		
		
		var bg = (index % 2 == 0) ? "bg1" : "bg2";
		var style = (absence.isDuplicate == "true") ? "background-color:green;" : "";
		var checked = absence.formSubmitted ? 'checked="checked"' : "";
		var date;
		
		if (absence.date == null) {
			date = "";
		}
		else {
			date = new Date(absence.date);
			if ( Object.prototype.toString.call(date) === "[object Date]" ) {
				// it is a date
				if ( isNaN( date.getTime() ) ) {  
					// date is not valid
					date = "";
				}
				else {
					// date is valid
					var months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
					date = months[date.getUTCMonth()] + " " + date.getUTCDate() + " " + date.getUTCFullYear();
				}
			}
			else {
			  // not a date
				date = "";
			}
			
		}
		var duplicate = (absence.duplicate) ? "Absence already exists. " : "";
		var success = "";
		if (absence.action == "add") {
			success = "Successfully added absence.";
		} 
		else if (absence.action == "update") {
			success = "Successfully updated absence.";
		} 
		else if (absence.action == "remove") {
			success = "Successfully removed absence.";
		} 
		else if (absence.action == "search") {
			success = "";
		} 
		
		var infoClass = "";
		if (absence.error == null) {
			absence.error = success;
			infoClass = "ui-state-active";
		}
		else {
			infoClass = "ui-state-error";
		}
		var err = duplicate + absence.error;
		var html = '<tr class="item" style="' + style + '">'
				 + 		'<input id="absenceId" type="hidden" value="' + absence.id +  '">'
				 + 		'<td id="formSubmitted"><input type="checkbox"' + checked + '></td>'
				 + 		'<td id="date">' + date +'</td>'
				 + 		'<td id="name">' + absence.name + '</td>' 
				 + 		'<td id="info" class="' + infoClass + '" title="' + err + '">' + err + '</td>'
				 + '</tr>';
		var htmlDom = $(html);
		htmlDom.click(function() {
			var isMasterCalendar = $("#calendarHeader #calendarSelect select").val() == "";
			if (! isMasterCalendar) {
				var id = $(this).children("#absenceId").val();
				$("#calendarHeader #calendarSelect select option[value='" + id + "']").attr("selected", true);
				$("#calendarHeader #calendarSelect select").change();
			}
			
		});
		htmlDom.dblclick(function() {
			var isMasterCalendar = $("#calendarHeader #calendarSelect select").val() == "";
			if (isMasterCalendar) {
				var id = $(this).children("#name").html().trim();
				$("#calendarHeader #calendarSelect select option[value='" + id + "']").attr("selected", true);
				$("#calendarHeader #calendarSelect select").change();
			}
			
		});
		//htmlDom.mousedown(thisPage.mouseDown);
		//htmlDom.mouseup(thisPage.mouseUp);
		htmlDom.hoverIntent(thisPage.hoverOn, thisPage.hoverOff);
		htmlDom.children("#formSubmitted").children("input").click(function() {
			var data = {
				'type': 'POST',
				'id': parseInt($(this).parent("#formSubmitted").parent(".item").children("input#absenceId").val()),
				'formSubmitted': this.checked	
			};
			querySite('complete', data, function() {
				var checked = 0;
				var total = 0;
				$.each($("#searchResults .item #formSubmitted input"), function() {
					if ($(this).attr("checked")) {
						checked++;
					}
					total++;
				});
				var year = parseInt($("#calendar .year .text").html());
				var month = parseInt($("#calendar #selectedMonth").val());
				var day = parseInt($("#calendar #selectedDay").val());
				var date = new Date(year,month,day,0,0,0,0);
				var dom = $("#month_" + date.getUTCMonth() 
						+ " #day_" + date.getUTCDate());
				if (checked == total) {
					dom.children(".mask").addClass("formSubmitted");
				}
				else {
					dom.children(".mask").removeClass("formSubmitted");
				}
			});
		});
		
		
		var searchResultsDom = $("#searchResultsContainer #searchResults");
		searchResultsDom.append(htmlDom);
		
		//var icon = $("#searchResultsContainer .label #icon .ui-icon");
		//icon.removeClass("ui-icon-triangle-1-s");
		//icon.addClass("ui-icon-triangle-1-n");
		
		//thisPage.showSearchResults();
	},
	'fillInCalendarSelect': function(absenceList, selectedName) {
		var calendarSelectDom =  $("#calendarHeader #calendarSelect select");
		calendarSelectDom.html("");
		$("#calendarHeader .header .title").html("Master Calendar");
		var html = '<option value="" selected="selected">Master Calendar</option>';
		var noneSelected = isEmpty(selectedName);
		var nameList = new Array();
		if (! isEmpty(absenceList) ) {
			$.each(absenceList, function() {
				if ($.inArray(this.name, nameList) == -1) {
					var selected = "";
					if ((! noneSelected) && selectedName == this.name) {
						selected = 'selected="selected"'
					}
					html += '<option value="' + this.name + '" ' + selected + '>' + this.name + '</option>';
					
					nameList.push(this.name);
				}
			});
		}
		
		calendarSelectDom.html(html);
	},
	'fillInMasterCalendar': function(absenceList) {
		$("#calendar .month .day").attr("title", "");
		$("#calendar .month .absentDay").removeClass("absentDay");
		$("#calendar .month .day .mask").removeClass("formSubmitted");
		
		var dateList = {};
		$.each(absenceList, function() {
			var date = new Date(this.date);
			if (date.getUTCFullYear() == parseInt($("#calendar .year .text").html())) {
				var dom = $("#month_" + date.getUTCMonth() 
						+ " #day_" + date.getUTCDate());
				dom.addClass("absentDay");
				if (isEmpty(dateList[this.date])) {
					dateList[this.date] = {
						'submitted': this.formSubmitted ? 1 : 0,
						'total': 1
					};
				}
				else {
					dateList[this.date].total += 1;
					if (this.formSubmitted) {
						dateList[this.date].submitted += 1;
					}
				}
			}
		});
		$.each(dateList, function(key, val) {
			if (val.submitted == val.total) {
				var date = new Date(parseInt(key));
				var offset = date.getTimezoneOffset() / 60;
				date.setUTCHours(offset, 0, 0, 0);
				var dom = $("#month_" + date.getUTCMonth() 
						+ " #day_" + date.getUTCDate()
						+ " .mask");
				dom.addClass("formSubmitted");
				var tmp = '';
			}
		});
		
		$("#calendarHeader .header img").hide();
	},
	'search': function() {
		var query = $("#header #searchQuery").val();
		
		var data = {
			'type': 'POST',
			'query': query,
			'employmentType': $("#searchResultsContainer .label .title .text").html()
		};
		
		$("#header #searchBar img").show();
		
		querySite('search', data, function(absenceList, textStatus, jqXHR) {
			var icon = $("#searchResultsContainer .label #icon .ui-icon");
			
			if (isEmpty(absenceList)) {
				var html = '<tr class="searchResultEmpty"><td>No results found.</td></tr>';
				$("#searchResultsContainer #searchResults").html(html);
				icon.removeClass("ui-icon-arrowthickstop-1-s");
				icon.addClass("ui-icon-arrowthickstop-1-n");
				
				thisPage.showSearchResults();
			}
			else {
				thisPage.clearSearchResults();
				$.each(absenceList, function(index) {
					this.action = "search";
					thisPage.displaySearchResult(this, index);
				});
				icon.removeClass("ui-icon-arrowthickstop-1-s");
				icon.addClass("ui-icon-arrowthickstop-1-n");
				thisPage.showSearchResults();
				$("#searchResultsContainer .tableContainer").resizable({ 
					handles: 's',
					maxHeight: 500
				});
			}
			//thisPage.showSearchResults();
			$("#searchResultsContainer .label span.text").html("Search Results:");
			$("#searchResults #formSubmitted").hide();
			$("#searchResults #date").hide();
			$("#searchResults #info").hide();
			$("#header #searchBar img").hide();
		});
	},
	'calendarSelected': function() {
		
		var data = {
			'name': $("#calendarSelect select option:selected").text(),
			'employmentType': $("#searchResultsContainer .label .title .text").html()
		};
		$("#calendarHeader .header img").show();
		$("#calendarHeader .header .title").html(data.name);
		
		if (data.name == "Master Calendar") {
			thisPage.loadMasterCalendar();
			$("#calendar #removeCalanderButton").hide();
		}
		else {
			querySite('get', data, function(absenceList, textStatus, jqXHR) {
				$("#calendar .month .day .tooltip-content").html("");
				$("#calendar .month .absentDay").removeClass("absentDay");
				$("#calendar .month .day .mask").removeClass("formSubmitted");
				$.each(absenceList, function() {
					var date = new Date(this.date);
					if (date.getUTCFullYear() == parseInt($("#calendar .year .text").html())) {

						var dom = $("#month_" + date.getUTCMonth() 
									+ " #day_" + date.getUTCDate());
						dom.addClass("absentDay");
						dom.children(".absenceId").val(this.id);
						
						var title = "Reason: " + this.reason;
						if ( $("#searchResultsContainer .title .text").html() == "Classified Employees" ) {
							title += "<br><br>Hours: " + this.hours;
						}
						dom.children(".tooltip").children(".tooltip-content").html(title);
						
						if (this.formSubmitted) {
							dom.children(".mask").addClass("formSubmitted");
						}
					}
					
					
				});
				
				//thisPage.clearSearchResults()
				//$("#searchResultsContainer .label .text").html("Search Results:");
				thisPage.hideSearchResults();
				
				$("#calendarHeader .header img").hide();
				$("#calendar #removeCalanderButton").show();
			});
		}
		
	},
	'budgetImport': function() {
		var textareaDom = $("#calendarHeader textarea");
		if (isEmpty(textareaDom.val())) {
			if (textareaDom.is(":visible")) {
				textareaDom.hide();
				$("#importButton span").html("Import");
			} else {
				textareaDom.show();
				$("#importButton span").html("Submit");
				$("#calendarHeader textarea").focus();
				thisPage.hideSearchResults();
			}
		}
		else {
			var data = {
				'type': 'POST',
				'rawText': textareaDom.val(),
				'year': parseInt($("#calendar .year .text").html()),
				'employmentType': $("#searchResultsContainer .label .title .text").html().trim()
			};
			
			$("#calendarHeader .header img").show();
			$("#calendarHeader textarea").val("");
			$("#calendarHeader textarea").hide();
			$("#calendarHeader #importButton span").html("Import");
			$("#searchResultsContainer .label span.text").html("Import Results:");
			thisPage.clearSearchResults();
			
			querySite('parse', data, function(absenceList, textStatus, jqXHR) {
				
				// select list
				var selectList = new Array();
				var nameList = new Array();
				$.each($("#calendarHeader #calendarSelect option"), function() {
					var name = $(this).html();
					if ( "Master Calendar" != name) {
						var calendar = {
							'id': $(this).val(),
							'name': name
						}
						selectList.push(calendar);
						nameList.push(name);
					}
				});
				
				
				// import results
				var importList = new Array();
				$.each(absenceList, function(index) {
					if ($.inArray(this.name, importList) == -1) {
						// not a duplicate name from import
						thisPage.displaySearchResult(this, index);
						importList.push(this.name);
						
						if ($.inArray(this.name, nameList) == -1) {
							// not already in select list
							var calendar = {
								'id': this.id,
								'name': this.name
							};
							selectList.push(calendar);
							nameList.push(name);
						}
					}
				});
				
				//var sortedSelectList = new Array();
				selectList.sort(function(a, b){
			        var keyA = a.name;
			        var keyB = b.name;
			        return (keyA > keyB) ? 1 : 0;
			    });
				
				thisPage.fillInCalendarSelect(selectList, "");
				
				$("#searchResultsContainer #searchResults").focus();
				$("#calendarHeader .header img").hide();
				$("#calendarHeader #calendarSelect select").change();
				$("#searchResults #formSubmitted").hide();
				
				
			});
		}
	},
	'dayClick': function() {
		$("#calendar #selectedMonth").val(parseInt($(this).parent(".month").attr("id").replace("month_", "")));
		$("#calendar #selectedDay").val(parseInt($(this).attr("id").replace("day_", "")));
		
		if ($(this).hasClass("absentDay")) {
			if ($("#calendarHeader #calendarSelect select").val() == "") {
				thisPage.listAbsences();
			}
			else {
				var mask = $(this).children(".mask");
				var id = parseInt($(this).children("input").val());
				var data = {};
				var complete;
				
				if (! mask.hasClass("formSubmitted")) {
					data = {
						'type': 'POST',
						'id': id,
						'formSubmitted': true
					};
					complete = function() {
						mask.addClass("formSubmitted");
					}
				}
				else {
					data = {
						'type': 'POST',
						'id': id,
						'formSubmitted': false	
					};
					complete = function() {
						mask.removeClass("formSubmitted");
					}
				}
				querySite('complete', data, complete);
			}
		}
	},
	'listAbsences': function() {
		var query = (parseInt($("#calendar #selectedMonth").val()) + 1) + "-"
				  + $("#calendar #selectedDay").val() + "-"
				  + $("#calendar .year .text").html();
		var data = {
			'type': 'POST',
			'searchBy': 'date',
			'query': query,
			'employmentType': $("#searchResultsContainer .label .title .text").html().trim()
		};
		querySite('search', data, function(absenceList, textStatus, jqXHR) {
			//$("#calendar .month .day").attr("title", "");
			//$("#calendar .month .absentDay").removeClass("absentDay");
			//$("#calendar .month .day .mask").removeClass("formSubmitted");
			
			if (absenceList != null) {
				thisPage.clearSearchResults();
				$.each(absenceList, function(index) {
					this.action = "search";
					thisPage.displaySearchResult(this, index);
					tmp='';
				});
			}
			$("#calendarHeader .header img").hide();
			
			$("#searchResults .item #formSubmitted").show();
			
			var date = thisPage.getSelectedDate();
			$("#searchResultsContainer .label span.text").html("Absences for " + $.format.date(date, "MMM dd, yyyy"));
			
			$("#calendarSelect select option:first").attr("selected", true);
			$("#calendarSelect select").change();
			
			//$("#searchResults #info").hide();
			$("#searchResults #info").removeClass("ui-state-active");
			thisPage.showSearchResults();
		});
		var tmp = '';
	},
	'removeAbsence': function() {
		if (confirm("Are you sure you want to remove this absence?")) {
			var data = $(this).data("data");
			querySite('remove', {'type':'POST', 'id': data.absenceId}, function(absenceList, textStatus, jqXHR){
				$("#" + data.monthDomId + " #" + data.dayDomId).removeClass("absentDay");
				$("#" + data.monthDomId + " #" + data.dayDomId + " .mask").removeClass("formSubmitted");
			});
		}
	},
	'addAbsence': function() {
		$("#addAbsenceDialog").dialog("open");
	},
	'displayUserMenu': function(event) {
		event.preventDefault();
		
		menuItemList = new Array();
		menuItemList.push({
			'name': 'Logout',
			'action': function() {
				window.location = $("#appNavBar #userNavItem .logoutUrl").val();
			},
			'data':{}
		});
	
		thisPage.displayPopupMenu(this, menuItemList);
	
	},
	'displayDayMenu': function(event) {
		if ( ! $(this).hasClass("weekend")) {
			event.preventDefault();
			
			$("#calendar #selectedMonth").val(parseInt($(this).parent(".month").attr("id").replace("month_", "")));
			$("#calendar #selectedDay").val(parseInt($(this).attr("id").replace("day_", "")));
			var isMasterCalender = $("#calendarHeader #calendarSelect select").val() == "";
			
			menuItemList = new Array();
			if ( isMasterCalender ||  (! $(this).hasClass("absentDay")) ) {
				menuItemList.push({
					'name': 'Add Absence',
					'action': thisPage.addAbsence,
					'data':{}
				});
			}
			if ($(this).hasClass("absentDay") && (! isMasterCalender)) {
				menuItemList.push({
					'name': 'Remove Absence', 
					'action': thisPage.removeAbsence,
					'data': {
						'absenceId': parseInt($(this).children("input").val()),
						'monthDomId': $(this).parent(".month").attr("id"),
						'dayDomId': $(this).attr("id")
					}
				});
			}
			if ((! isMasterCalender) || $(this).hasClass("absentDay")) {
				menuItemList.push({
					'name': 'List Absences', 
					'action': thisPage.listAbsences,
					'data': {}
				});
			}
			thisPage.displayPopupMenu(this, menuItemList);
		}
	},
	'displayPopupMenu': function(srcDom, menuItemList) {
		if (! isEmpty(menuItemList) ) {
			$("#popupMenu").remove();


			var popupMenuDom = $('<div id="popupMenu" class="ui-widget-content"></div>');
			$.each(menuItemList, function() {
				var html = '<div class="item">'
						 +		this.name
						 + '</div>';
				var htmlDom = $(html);
				htmlDom.click(this.action);
				htmlDom.data('data', this.data);
				popupMenuDom.append(htmlDom);
			});

			var srcDomOffset = $(srcDom).offset();
			popupMenuDom.css('position', 'absolute');
			popupMenuDom.css('left', srcDomOffset.left);
			var top = srcDomOffset.top + $(srcDom).height() 
							+ parseInt($(srcDom).css('padding-top').replace('px',''))
							+ parseInt($(srcDom).css('padding-bottom').replace('px', ''));
			popupMenuDom.css('top', top);

			var popupBackground = $('<div id="popupBackground"></div>');
			popupBackground.click(function(event) {
				$("#popupBackground").remove();
				$("#fileUploadMenu").remove();
			})
			
			
			popupBackground.append(popupMenuDom);
			$("body").append(popupBackground);

		}
	},
	'csvImport': function(event) {
		event.preventDefault();
		var employmentType = $("#searchResultsContainer .title .text").html();
		var html	= '<div id="fileUploadMenu" class="ui-widget-content auraGreen">'
					+	'<form action="/import" method="POST" type="multipart/form-data">'
					+		'<input type="hidden" name="employmentType" value="' + employmentType + '">'
					+		'<input type="file" id="csvFile" name="csvFile">'
					+	'</form>'
					+ '</div>'
					+ '<div id="fileUploadMenuTooltip" class="tooltip">'
					+	'<div class="tooltip-content"></div>'
					+	'<div class="tooltip-arrow-border"></div>'
					+	'<div class="tooltip-arrow"></div>'
					+ '</div>';
		
		var htmlDom = $(html);
		var srcDomOffset = $(this).offset();
		htmlDom.css('position', 'absolute');
		var top = srcDomOffset.top
				- parseInt($(this).parent().css('border-top-width').replace('px',''));
		htmlDom.css('top', top);
		var left = srcDomOffset.left + $(this).width() 
						+ parseInt($(this).css('padding-left').replace('px',''))
						+ parseInt($(this).css('padding-right').replace('px', ''));
		htmlDom.css('left', left);
		htmlDom.children("form").children("#csvFile").change(function(){
			$("#fileUploadMenu form").submit();
		});
		htmlDom.children('form').ajaxForm({
			dataType: "json",
			success: function(info, textStatus, jqXHR) {
				$("#searchResultsContainer .label span.text").html("Csv Import Results:");
				thisPage.fillInCalendarSelect(info.employeeList, "");
				thisPage.clearSearchResults();
				$.each(info.absenceList, function(index) {
					thisPage.displaySearchResult(this, index);
				})
				thisPage.showSearchResults();
				$("#fileUploadMenu").remove();
				$("#fileUploadMenuTooltip").remove();
				$("#searchResults #date").show();
				$("#searchResults #formSubmitted").hide();
				$("#popupBackground").click();
				$("#calendarHeader #calendarSelect select").change();
				
				
				
			}
		});
		$("body").append(htmlDom);
		
		var title = "Import record limit: " + $("#importRecordLimit").val() + " records.";
		$("#fileUploadMenuTooltip .tooltip-arrow")
			.removeClass()
			.addClass("tooltip-arrow")
			.addClass("tooltip-arrow-top-right");
		$("#fileUploadMenuTooltip .tooltip-arrow-border")
			.removeClass()
			.addClass("tooltip-arrow-border")
			.addClass("tooltip-arrow-border-top-right");
		$("#fileUploadMenuTooltip .tooltip-content").html(title);
		$("#fileUploadMenu").tooltip({
			effect: "slide",
			relative: true,
			position: "bottom center",
			offset: [10, 0]
		});
		//$("#fileUploadMenu .tooltip").show();
		
		
		return false;
	},
	'csvExport': function() {
		//<input type="hidden" id="exportdata" name="exportdata" />
		var html = '<input name="employmentType" value="' + $("#searchResultsContainer .label .title .text").html() + '">';
		$("body").append('<form id="exportform" action="/export" method="post" target="_blank">' + html + '</form>');
		//$("#exportdata").val(header_string + export_string);
		$("#exportform").submit().remove(); 
	}
	
	
}
