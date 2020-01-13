//----------------------------------------------------
// Client-side logic for order business object
//----------------------------------------------------
var DemoOrder = typeof DemoOrder !== "undefined" ? DemoOrder : (function($) {
	// Responsive UI hook
	Simplicite.UI.hooks.DemoOrder = function(o, cbk) {
		try {
			// Helper to dynamically change unit price when selecting product (also done on server side)
			o.locals.ui.form.onload = function(ctn, obj) {
				var f = $ui.getUIField(ctn, obj, "demoOrdPrdId.demoPrdUnitPrice");
				f.ui.on("change", function() {
					$ui.getUIField(ctn, obj, "demoOrdUnitPrice").ui.val(f.ui.val());
				});
			};
		} catch(e) { console.error(e.message); } finally { cbk && cbk(); }
	};
})(jQuery);
