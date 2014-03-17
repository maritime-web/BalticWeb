module.exports = function(config) {
	config.set({
		// base path, that will be used to resolve files and exclude
		basePath : '../../../../',

		frameworks : [ 'jasmine' ],

		// list of files / patterns to load in the browser
		files : [
		        'http://ajax.aspnetcdn.com/ajax/jQuery/jquery-1.9.1.min.js',
		        'http://openlayers.org/api/OpenLayers.js',
		        'http://www.openstreetmap.org/openlayers/OpenStreetMap.js',
		        'http://code.jquery.com/ui/1.9.1/jquery-ui.js',
		        'src/main/webapp/js/ext/bootstrap.min.js',
		        'http://ajax.googleapis.com/ajax/libs/angularjs/1.1.5/angular.min.js',
				'src/main/webapp/js/ext/*.js',
				'src/test/lib/angular-1.1.5/angular-mocks.js',
                'src/main/webapp/js/utils.js',
				'src/main/webapp/js/embryo-for-test.js',
				'src/main/webapp/js/reporting/index-base.js',
				'src/main/webapp/js/menu.js', 
                'src/main/webapp/js/reporting/course.js',
				'src/main/webapp/js/reporting/position.js',
                'src/main/webapp/js/reporting/decimal.js',
				'src/main/webapp/js/reporting/route-upload.js',
				'src/test/jsUnit/**/*Test.js' ],

		// list of files to exclude
		exclude : ['src/main/webapp/js/ext/jquery.fileupload*.js'],

		// use dots reporter, as travis terminal does not support escaping
		// sequences
		// possible values: 'dots', 'progress'
		// CLI --reporters progress
		reporters : [ 'progress', 'junit' ],

		junitReporter : {
			// will be resolved to basePath (in the same way as files/exclude
			// patterns)
			outputFile : 'target/surefire-reports/karmaUnit.xml'
		},

		// web server port
		// CLI --port 9876
		port : 9876,

		// enable / disable colors in the output (reporters and logs)
		// CLI --colors --no-colors
		colors : true,

		// level of logging
		// possible values: config.LOG_DISABLE || config.LOG_ERROR ||
		// config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
		// CLI --log-level debug
		logLevel : config.LOG_INFO,

		// enable / disable watching file and executing tests whenever any file
		// changes
		// CLI --auto-watch --no-auto-watch
		autoWatch : true,

		// Start these browsers, currently available:
		// - Chrome
		// - ChromeCanary
		// - Firefox
		// - Opera
		// - Safari (only Mac)
		// - PhantomJS
		// - IE (only Windows)
		// CLI --browsers Chrome,Firefox,Safari
		browsers : [ process.env.TRAVIS ? 'Firefox' : 'Chrome', 'PhantomJS' ],

		// If browser does not capture in given timeout [ms], kill it
		// CLI --capture-timeout 5000
		captureTimeout : 20000,

		// Auto run tests on start (when browsers are captured) and exit
		// CLI --single-run --no-single-run
		singleRun : false,

		// report which specs are slower than 500ms
		// CLI --report-slower-than 500
		reportSlowerThan : 500,

		// compile coffee scripts
		preprocessors : {
			'**/*.coffee' : 'coffee'
		},

		plugins : [ 'karma-jasmine', 'karma-chrome-launcher',
				'karma-firefox-launcher', 'karma-junit-reporter', 'karma-phantomjs-launcher' ]
	});
};