function ForecastLayer() {

	OpenLayers.Strategy.RuleCluster = OpenLayers.Class(OpenLayers.Strategy.Cluster, {
		/**
		 * the rule to use for comparison
		 */
		rule : null,
		/**
		 * Method: shouldCluster Determine whether to include a feature in a
		 * given cluster.
		 * 
		 * Parameters: cluster - {<OpenLayers.Feature.Vector>} A cluster.
		 * feature - {<OpenLayers.Feature.Vector>} A feature.
		 * 
		 * Returns: {Boolean} The feature should be included in the cluster.
		 */
		shouldCluster : function(cluster, feature) {
			var superProto = OpenLayers.Strategy.Cluster.prototype;
			return this.rule.evaluate(cluster.cluster[0]) && this.rule.evaluate(feature) && superProto.shouldCluster.apply(this, arguments);
		},
		CLASS_NAME : "OpenLayers.Strategy.RuleCluster"
	});

	this.init = function() {
		this.zoomLevels = [ 4, 6, 11 ];

		var that = this;

		this.context = {
			transparency : function() {
				return that.active ? 0.5 : 0.25;
			},
			size : function(feature) {
				return [ 16, 20, 24, 24 ][that.zoomLevel];
			},
			offset : function() {
				return -that.context.size() / 2;
			},
			zoomDependentDescription : function(feature) {
				if (that.zoomLevel < 3) {
					return "";
				}
				return that.context.description(feature);
			},
			description : function(feature) {
				return feature.cluster ? feature.cluster.length + " Forecast locations" : feature.attributes.iceDescription.Number + ": "
						+ feature.attributes.iceDescription.Placename;
			},
			display : function(feature) {
				return "yes";
			}
		};

		this.layers.forecasts = new OpenLayers.Layer.Vector("Forecasts", {
			styleMap : new OpenLayers.StyleMap({
				"default" : new OpenLayers.Style({
					// externalGraphic : "img/inshoreIceReport.png",
					graphicOpacity : "${transparency}",
					graphicWidth : '${size}',
					graphicHeight : '${size}',
					graphicYOffset : "${offset}",
					graphicXOffset : "${offset}",
					fillColor : '${level}',
					strokeWidth : 1,
					strokeColor : '#333333',
					strokeOpacity : 1,
					fontColor : "#000000",
					fontSize : "10px",
					fontFamily : "Courier New, monospace",
					// label : "${obs}",
					fontOpacity : 1,
					fontWeight : "bold",
					labelOutlineWidth : 0,
					labelYOffset : -20
				}, {
					context : this.context
				}),
				"select" : new OpenLayers.Style({
					// externalGraphic : "img/inshoreIceReport.png",
					graphicOpacity : 1,
					graphicWidth : 24,
					graphicHeight : 24,
					graphicXOffset : -12,
					graphicYOffset : -12,
					backgroundGraphic : "img/ring.png",
					backgroundXOffset : -16,
					backgroundYOffset : -16,
					backgroundHeight : 32,
					backgroundWidth : 32,
					fontOpacity : 1,
					fontColor : "#000",
					fontSize : "10px",
					fontFamily : "Courier New, monospace",
					label : "${obs}",
					fontWeight : "bold",
					labelOutlineWidth : 0,
					labelYOffset : -20,
					display : "${display}",
				}, {
					context : this.context
				})
			})
		});

		this.selectableLayers = [ this.layers.forecasts ];
		this.selectableAttribute = "number";
	};

	this.getIceConcentrationLevel = function(obs) {
		if (obs < 0.1) {
			return '#96c7ff';
		} else if (obs < 0.3) {
			return '#8effa0';
		} else if (obs < 0.6) {
			return '#ffff00';
		} else if (obs < 0.8) {
			return '#ff7c06';
		} else if (obs < 1) {
			return '#ff0000';
		} else {
			return '#979797';
		}
	};
	
	this.getIceAccretionLevel = function(obs) {
		if(obs < 0) {
			return '#96c7ff';
		} else if(obs < 22.5) {
			return '#8effa0';
		} else if(obs < 53.4) {
			return '#ffff00';
		} else if(obs < 83.1) {
			return '#ff7c06';
		} else {
			return '#979797';
		}
	};

	this.drawConcentration = function(forecast, time) {
		var index = forecast.variables['Ice concentration'];
		var lats = forecast.metadata.lat;
		var lons = forecast.metadata.lon;
		var features = [];
		var half = 0.5;
		
		var entries = forecast.data[time].entries;

		for(var e in entries) {
			var obs = entries[e][index];
			var level = this.getIceConcentrationLevel(obs);
			var lat = lats[e.substr(0, e.indexOf('_'))];
			var lon = lons[e.substr(e.indexOf('_') + 1, e.length - 1)];
			
			if (obs && lon && lat) {

				var points = [ embryo.map.createPoint(lon - half, lat - half), embryo.map.createPoint(lon + half, lat - half),
						embryo.map.createPoint(lon + half, lat + half), embryo.map.createPoint(lon - half, lat + half) ];
				var square = new OpenLayers.Geometry.LinearRing(points);
				var feature = new OpenLayers.Feature.Vector(square, {
					level : level,
					obs : obs
				});
				features.push(feature);
			}
			console.log('Ice conc: ' + lat + ':' + lon + ' - ' + obs);
			
		}
		return features;
	};

	this.drawThickness = function(forecast, time) {
		var index = forecast.variables['Ice thickness'];
		var lats = forecast.metadata.lat;
		var lons = forecast.metadata.lon;
		var features = [];
		var half = 0.5;
		
		var entries = forecast.data[time].entries;

		for(var e in entries) {
			var obs = entries[e][index];
			var level = this.getIceConcentrationLevel(obs);
			var lat = lats[e.substr(0, e.indexOf('_'))];
			var lon = lons[e.substr(e.indexOf('_') + 1, e.length - 1)];
			
			if (obs && lon && lat) {

				var points = [ embryo.map.createPoint(lon - half, lat - half), embryo.map.createPoint(lon + half, lat - half),
						embryo.map.createPoint(lon + half, lat + half), embryo.map.createPoint(lon - half, lat + half) ];
				var square = new OpenLayers.Geometry.LinearRing(points);
				var feature = new OpenLayers.Feature.Vector(square, {
					level : level,
					obs : obs
				});
				features.push(feature);
			}
			console.log('Ice thickness: ' + lat + ':' + lon + ' - ' + obs);
			
		}
		return features;

	};

	this.drawSpeed = function(forecast, time) {
		var indexEast = forecast.variables['Ice speed east'];
		var indexNorth = forecast.variables['Ice speed north'];
		var lats = forecast.metadata.lat;
		var lons = forecast.metadata.lon;
		var features = [];
		var half = 0.2;

		var entries = forecast.data[time].entries;

		for(var e in entries) {
			var east = entries[e][indexEast];
			var north = entries[e][indexNorth];
			if(east || north) {
				var level = this.getIceConcentrationLevel(Math.sqrt(north * north + east * east));
				var lat = lats[e.substr(0, e.indexOf('_'))];
				var lon = lons[e.substr(e.indexOf('_') + 1, e.length - 1)];
				var points = new Array(embryo.map.createPoint(lon, lat + half), embryo.map.createPoint(lon + half, lat + (half * 0.2)), embryo.map.createPoint(
						lon + (half * 0.2), lat + (half * 0.2)), embryo.map.createPoint(lon + (half * 0.2), lat - (half * 0.5)), embryo.map.createPoint(lon
						- (half * 0.2), lat - (half * 0.5)), embryo.map.createPoint(lon - (half * 0.2), lat + (half * 0.2)), embryo.map.createPoint(lon - half,
						lat + (half * 0.2)));

				var rad = Math.acos(north / Math.sqrt(north * north + east * east));
				var degrees = Math.round(rad * 180 / Math.PI);

				var linearRing = new OpenLayers.Geometry.LinearRing(points);
				linearRing.rotate(degrees, embryo.map.createPoint(lon, lat));
				var feature = new OpenLayers.Feature.Vector(linearRing, {
					level : level,
					obs : east + '/' + north
				});
				features.push(feature);

				console.log('Ice speed: ' + lat + ':' + lon + ' - ' + east + '/' + north + '; cos: '
						+ Math.acos(north / Math.sqrt(north * north + east * east)));
				
			}

		}
		return features;
	};
	
	this.drawAccretion = function(forecast, time) {
		var index = forecast.variables['Ice accretion risk'];
		var lats = forecast.metadata.lat;
		var lons = forecast.metadata.lon;
		var features = [];
		var half = 0.5;
		
		var entries = forecast.data[time].entries;

		for(var e in entries) {
			var obs = entries[e][index];
			var level = this.getIceAccretionLevel(obs);
			var lat = lats[e.substr(0, e.indexOf('_'))];
			var lon = lons[e.substr(e.indexOf('_') + 1, e.length - 1)];
			
			if (obs && lon && lat) {

				var points = [ embryo.map.createPoint(lon - half, lat - half), embryo.map.createPoint(lon + half, lat - half),
						embryo.map.createPoint(lon + half, lat + half), embryo.map.createPoint(lon - half, lat + half) ];
				var square = new OpenLayers.Geometry.LinearRing(points);
				var feature = new OpenLayers.Feature.Vector(square, {
					level : level,
					obs : obs
				});
				features.push(feature);
			}
			console.log('Ice accretion: ' + lat + ':' + lon + ' - ' + obs);
			
		}
		return features;
	};


	this.drawIceForecast = function(forecast, time, mapType) {
		var that = this;
		that.clear();

		var features = [];
		switch (mapType) {
		case 'iceConcentration':
			features = this.drawConcentration(forecast, time);
			break;
		case 'iceThickness':
			features = this.drawThickness(forecast, time);
			break;
		case 'iceSpeed':
			features = this.drawSpeed(forecast, time);
			break;
		case 'iceAccretion':
			features = this.drawAccretion(forecast, time);
			break;
		}

		that.layers.forecasts.addFeatures(features);
		that.layers.forecasts.refresh();

	};

	this.getWaveConcentrationLevel = function(obs) {
		if (obs < 0.7) {
			return '#00DE00';
		} else if (obs < 1.0) {
			return '#FFFF00';
		} else if (obs < 1.3) {
			return '#FA4242';
		} else {
			return '#E8B332';
		}
	};

	this.drawWaveForecastFeature = function(lat, lon, obs, vars) {
		var half = 0.2;
		if (obs && obs[0]) {
			var level = this.getWaveConcentrationLevel(obs[vars['Significant wave height']]);
			var points = new Array(embryo.map.createPoint(lon, lat + half), embryo.map.createPoint(lon + half, lat + (half * 0.2)), embryo.map.createPoint(lon
					+ (half * 0.2), lat + (half * 0.2)), embryo.map.createPoint(lon + (half * 0.2), lat - (half * 0.5)), embryo.map.createPoint(lon
					- (half * 0.2), lat - (half * 0.5)), embryo.map.createPoint(lon - (half * 0.2), lat + (half * 0.2)), embryo.map.createPoint(lon - half, lat
					+ (half * 0.2)));
			var linearRing = new OpenLayers.Geometry.LinearRing(points);
			linearRing.rotate(obs[vars['Wave direction']], embryo.map.createPoint(lon, lat));
			var feature = new OpenLayers.Feature.Vector(linearRing, {
				level : level
			});
			console.log('Sign. wave height: ' + obs[vars['Significant wave height']]);
			return feature;
		}
		return null;
	};

	this.drawWaveForecast = function(forecast, time) {
		var that = this;
		that.clear();

		var vars = forecast.variables;
		var lats = forecast.metadata.lat;
		var lons = forecast.metadata.lon;
		var features = [];

		for ( var d in forecast.data) {
			var data = forecast.data[d];
			if (time == data.time) {
				var feature = this.drawWaveForecastFeature(lats[data.lat], lons[data.lon], data.obs, vars);
				if (feature) {
					features.push(feature);
				}
			}
		}

		that.layers.forecasts.addFeatures(features);
		that.layers.forecasts.refresh();
	};

	this.drawCurrentForecast = function(forecast, time) {
		var that = this;
		that.clear();

		var indexEast = forecast.variables['Current east'];
		var indexNorth = forecast.variables['Current north'];
		var lats = forecast.metadata.lat;
		var lons = forecast.metadata.lon;
		var features = [];
		var half = 0.2;

		var entries = forecast.data[time].entries;
		
		for(var e in entries) {
			var east = entries[e][indexEast];
			var north = entries[e][indexNorth];
			if(east || north) {
				var level = this.getIceConcentrationLevel(Math.sqrt(north * north + east * east));
				var lat = lats[e.substr(0, e.indexOf('_'))];
				var lon = lons[e.substr(e.indexOf('_') + 1, e.length - 1)];

				var points = new Array(embryo.map.createPoint(lon, lat + half), embryo.map.createPoint(lon + half, lat + (half * 0.2)), embryo.map.createPoint(
						lon + (half * 0.2), lat + (half * 0.2)), embryo.map.createPoint(lon + (half * 0.2), lat - (half * 0.5)), embryo.map.createPoint(lon
						- (half * 0.2), lat - (half * 0.5)), embryo.map.createPoint(lon - (half * 0.2), lat + (half * 0.2)), embryo.map.createPoint(lon - half,
						lat + (half * 0.2)));

				var rad = Math.acos(north / Math.sqrt(north * north + east * east));
				var degrees = Math.round(rad * 180 / Math.PI);

				var linearRing = new OpenLayers.Geometry.LinearRing(points);
				linearRing.rotate(degrees, embryo.map.createPoint(lon, lat));
				var feature = new OpenLayers.Feature.Vector(linearRing, {
					level : level,
					obs : east + '/' + north
				});
				features.push(feature);

				console.log('Current speed: ' + lat + ':' + lon + ' - ' + east + '/' + north + '; cos: '
						+ Math.acos(north / Math.sqrt(north * north + east * east)));

			}
		}
		that.layers.forecasts.addFeatures(features);
		that.layers.forecasts.refresh();
	};

}

ForecastLayer.prototype = new EmbryoLayer();
