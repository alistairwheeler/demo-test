//-----------------------------------------------------------
// Client side JavaScript for order agenda
//-----------------------------------------------------------

var DemoOrderAgenda = typeof DemoOrderAgenda !== "undefined" ? DemoOrderAgenda : (function($) {

var ord, debug = false;

function render(url) {
	$ui.loadCalendar(function() {
		$ui.getUIObject("DemoOrder", "agenda_DemoOrder", function(o) {
			ord = o;
			ord.getMetaData(calendar);
		});
	});
}

function calendar() {
	$("#ordercalendar").fullCalendar({
		header: { left: "prev,next today", center: "title", right: "month,agendaWeek" },
		timezone: $ui.grant.timezone || "local",
		defaultView: "agendaWeek",
		editable: true,
		firstDay: 1, minTime: "06:00:00", maxTime: "22:00:00",
		businessHours: { dow: [ 1, 2, 3, 4, 5 ], start: "08:00", end: "20:00" },
		eventClick: function(e) {
			if (debug) console.log("Order " + e.id + " clicked");
			$ui.displayForm(null, "DemoOrder", e.id, { nav: "add" });
		},
		eventDrop: function(e) {
			var s = e.start.format( "YYYY-MM-DD HH:mm:ss");
			if (debug) console.log("Order " + e.id + " dropped to " + s);
			e.data.demoOrdDeliveryDate = s;
			ord.update(function() {
				e.data = ord.item;
				if (debug) console.debug("Order " + e.data.demoOrdNumber + " delivery date updated to " + s);
			}, e.data);
		},
		events: function(start, end, tz, callback) {
			var f = "YYYY-MM-DD HH:mm:ss Z";
			var dmin = start.format(f);
			var dmax = end.format(f);
			if (debug) console.debug("Calendar view range = " + dmin + " to " + dmax);
			ord.search(function() {
				if (debug) console.debug(ord.list.length + " orders found between " + dmin + " and " + dmax);
				var status = ord.getField("demoOrdStatus");
				var evts = [];
				for (var i = 0; i < ord.list.length; i++) {
					var item = ord.list[i];
					if (item.demoOrdDeliveryDate !== "") { // ZZZ When using intervals empty values are included !
						var s = moment(item.demoOrdDeliveryDate);
						var e = s.add(2, "h");
						var st = status.getEnumItem(item.demoOrdStatus);
						evts.push({
							id: item.row_id,
							data: item,
							title: item.demoOrdNumber + "\n" + item.demoOrdCliId__demoCliCode + " / " + item.demoOrdPrdId__demoPrdReference + "\n" + status.getDisplay() + ": " + status.displayValue(item.demoOrdStatus),
							start: s,
							end: e,
							editable: item.demoOrdStatus == "P" || item.demoOrdStatus == "V",
							durationEditable: false,
							color: st.bgcolor,
							borderColor: st.bgcolor,
							textColor: st.color
						});
					}
				}
				if (debug) console.debug(evts.length + " orders displayed between " + dmin + " and " + dmax);
				callback(evts);
			}, { demoOrdDeliveryDate: dmin + ";" + dmax, demoOrdStatus: "P;V;D" }, { inlineDocs: false });
		}
	});
}

return { render: render };

})(jQuery);
