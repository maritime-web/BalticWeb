var Browser = {
		Version: function() {
			var version = 999; // we assume a sane browser
			if (navigator.appVersion.indexOf("MSIE") != -1)
				// bah, IE again, lets downgrade version number
				version = parseFloat(navigator.appVersion.split("MSIE")[1]);
			return version;
			}
};
if (Browser.Version() < 9) {
	alert("This web application will only work with browsers: Chrome, Firefox, Safari and IE 9");
}
