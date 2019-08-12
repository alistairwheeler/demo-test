//----------------------------------------------------
// Client-side logic for customer business object
//----------------------------------------------------
var DemoClient = typeof DemoClient !== "undefined" ? DemoClient : (function($) {
	var resp = typeof $ui !== "undefined";

	// Google Map action
	var map = function() {
		var rowId = getFieldValue("row_id");
		if (rowId && rowId != "0") {
			var n = $.escapeHTML(getFieldValue("demoCliFirstname") + " " + getFieldValue("demoCliLastname"));
			var a = $.escapeHTML(getFieldValue("demoCliAddress1") + ", " + getFieldValue("demoCliZipCode") + " " + getFieldValue("demoCliCity") + ", " + getFieldValue("demoCliCountry"));
			var c = getFieldValue("demoCliCoords");
			if (c !== "") {
				var l = c.replace(";", ",").split(",");
				var u = Simplicite.ROOT + "/googlemap?lat=" + l[0] + "&lng=" + l[1] + "&info=" + encodeURIComponent("<div style=\"width: 200px; height: 75px;\"><b>" + n + "</b><br/>" + a + "</div>");
				$("#client-map").show();
				window.frames.gmap.location.replace(u + "&width=500&height=450&title=false");
			}
			return false;
		}
	};

	// Google Hangouts button
	var hangout = function() {
		var email = getFieldValue("demoCliEmail");
		var invites = email ? [{ id: email, invite_type: "EMAIL" }]: [];
		gapi.hangout.render("hangout-button", { "render": "createhangout", "invites": invites });
	};

	if (resp) {
		// Responsive UI hook
		Simplicite.UI.hooks.DemoClient = function(o, cbk) {
			// Minimalistic backward compatibility with legacy UI getFieldValue for responsive UI
			window.getFieldValue = function(name) {
				return $ui.getUIField(null, o, name).ui.val();
			};

			try {
				o.locals.ui.form.onload = function() {
					try {
						if (typeof gapi === "undefined" || typeof gapi.hangout === "undefined") {
							$ui.loadScript({
								url: "https://apis.google.com/js/platform.js",
								onload: hangout
							})
						} else {
							hangout();
						}
					} catch(el) {
						console.error(el);
					}
				}
			} catch(e) {
				console.error(e.message);
			} finally {
				cbk && cbk();
			}
		};
	} else {
		// Legacy UI hook
		onload_functions.push(hangout);
	}

	// Exposed functions
	return { map: map }
})(jQuery);