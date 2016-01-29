function ForecastLayer() {
    var that = this;

    OpenLayers.Strategy.RuleCluster = OpenLayers.Class(
			OpenLayers.Strategy.Cluster, {
				/**
				 * the rule to use for comparison
				 */
				rule : null,
				/**
				 * Method: shouldCluster Determine whether to include a feature
				 * in a given cluster.
				 * 
				 * Parameters: cluster - {<OpenLayers.Feature.Vector>} A
				 * cluster. feature - {<OpenLayers.Feature.Vector>} A feature.
				 * 
				 * Returns: {Boolean} The feature should be included in the
				 * cluster.
				 */
				shouldCluster : function(cluster, feature) {
					var superProto = OpenLayers.Strategy.Cluster.prototype;
					return this.rule.evaluate(cluster.cluster[0])
							&& this.rule.evaluate(feature)
							&& superProto.shouldCluster.apply(this, arguments);
				},
				CLASS_NAME : "OpenLayers.Strategy.RuleCluster"
			});

    this.init = function () {
        that.zoomLevels = [3, 4, 5, 6, 7, 8, 9, 10];

        that.context = {
			transparency : function() {
				return that.active ? 0.5 : 0.25;
			},
			size : function(feature) {
                return [ 4, 6, 8, 10, 16, 18, 20, 24 ][that.zoomLevel];
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
				return feature.cluster ? feature.cluster.length
						+ " Forecast locations"
						: feature.attributes.iceDescription.Number + ": "
								+ feature.attributes.iceDescription.Placename;
			},
			display : function(feature) {
				return "yes";
			}
		};

        that.layers.forecasts = new OpenLayers.Layer.Vector("Forecasts", {
			styleMap : new OpenLayers.StyleMap({
				"default" : new OpenLayers.Style({
                    externalGraphic: "img/forecast/arrow_${col}.png",
                    rotation: "${angle}",
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
                    context: that.context
				}),
				"select" : new OpenLayers.Style({
                    externalGraphic: "img/forecast/arrow_${col}.png",
                    rotation: "${angle}",
                    fillOpacity: 1,
					graphicOpacity : 1,
                    graphicWidth: '${size}',
                    graphicHeight: '${size}',
                    graphicYOffset: "${offset}",
                    graphicXOffset: "${offset}",
					backgroundGraphic : "img/ring.png",
					backgroundXOffset : -16,
					backgroundYOffset : -16,
					backgroundHeight : 32,
					backgroundWidth : 32,
					fontOpacity : 1,
					fontColor : "#000",
					fontSize : "10px",
					fontFamily : "Courier New, monospace",
                    label: "${obs} : ${angle}",
					fontWeight : "bold",
					labelOutlineWidth : 0,
					labelYOffset : -20,
					display : "${display}",
				}, {
                    context: that.context
				})
			})
		});

        that.selectableLayers = [ that.layers.forecasts ];
        that.selectableAttribute = "number";
	};

    that.getIceConcentrationLevel = function (obs) {
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

    that.getIceThicknessLevel = function (obs) {
		if (obs < 0.1) {
			return '#96c7ff';
		} else if (obs < 0.3) {
			return '#8effa0';
		} else if (obs < 0.5) {
			return '#ffff00';
		} else if (obs < 1) {
			return '#ff7c06';
		} else if (obs < 2) {
			return '#ff0000';
		} else {
			return '#979797';
		}
	};

    that.getSpeedLevel = function (obs) {
		if (obs < 0.1) {
			return '#96c7ff';
		} else if (obs < 0.3) {
			return '#8effa0';
		} else if (obs < 0.5) {
			return '#ffff00';
		} else if (obs < 1) {
			return '#ff7c06';
		} else if (obs < 2) {
			return '#ff0000';
		} else {
			return '#979797';
		}
	};

    that.getIceAccretionLevel = function (obs) {
		if (obs < 0) {
			return '#96c7ff';
		} else if (obs < 22.5) {
			return '#8effa0';
		} else if (obs < 53.4) {
			return '#ffff00';
		} else if (obs < 83.1) {
			return '#ff7c06';
		} else {
			return '#979797';
		}
	};
    that.getWaveHeightLevel = function (obs) {
        if (obs < 2) {
			return '#96c7ff';
        } else if (obs < 4) {
			return '#8effa0';
        } else if (obs < 6) {
			return '#ffff00';
        } else if (obs < 8) {
			return '#ff7c06';
        } else if (obs < 10) {
			return '#ff0000';
		} else {
			return '#979797';
		}
	};
	
	that.getBounds = function (forecast) {
		var lats = forecast.metadata.lat;
		var lons = forecast.metadata.lon;
		var minLat = lats[0];
		var maxLat = lats[lats.length - 1];
		var minLon = lons[0];
		var maxLon = lons[lons.length - 1];
		
		return {
			minLat : minLat,
			maxLat : maxLat,
			minLon : minLon,
			maxLon : maxLon
		};
	};
	
	that.zoomToForecast = function (bounds) {
		var minPoint = embryo.map.createPoint(bounds.minLon, bounds.minLat);
		var maxPoint = embryo.map.createPoint(bounds.maxLon, bounds.maxLat);
		that.zoomToCoords(minPoint, maxPoint);
	};

    that.drawFrame = function (bounds) {
		var half = 0.2;

		var points = [ embryo.map.createPoint(bounds.minLon, bounds.minLat - half),
				embryo.map.createPoint(bounds.minLon, bounds.maxLat + half),
				embryo.map.createPoint(bounds.maxLon, bounds.maxLat + half),
				embryo.map.createPoint(bounds.maxLon, bounds.minLat - half),
				embryo.map.createPoint(bounds.minLon, bounds.minLat - half) ];
		var square = new OpenLayers.Geometry.LineString(points);
		var feature = new OpenLayers.Feature.Vector(square);
		return feature;
	};

    that.drawConcentration = function (forecast, time) {
		var index = forecast.variables['Ice concentration'];
		var lats = forecast.metadata.lat;
		var lons = forecast.metadata.lon;
		var features = [];
		var half = 0.2;

		var entries = forecast.data[time].entries;

		for ( var e in entries) {
			var obs = entries[e][index];
            var level = that.getIceConcentrationLevel(obs);
			var lat = lats[e.substr(0, e.indexOf('_'))];
			var lon = lons[e.substr(e.indexOf('_') + 1, e.length - 1)];

			if (obs && lon && lat) {

				var points = [ embryo.map.createPoint(lon - half, lat - half),
						embryo.map.createPoint(lon + half, lat - half),
						embryo.map.createPoint(lon + half, lat + half),
						embryo.map.createPoint(lon - half, lat + half) ];
				var square = new OpenLayers.Geometry.LinearRing(points);
				var feature = new OpenLayers.Feature.Vector(square, {
					level : level,
					obs : obs
				});
				features.push(feature);
			}
		}
		return features;
	};

    that.drawThickness = function (forecast, time) {
		var index = forecast.variables['Ice thickness'];
		var lats = forecast.metadata.lat;
		var lons = forecast.metadata.lon;
		var features = [];
		var half = 0.2;

		var entries = forecast.data[time].entries;

		for ( var e in entries) {
			var obs = entries[e][index];
            var level = that.getIceThicknessLevel(obs);
			var lat = lats[e.substr(0, e.indexOf('_'))];
			var lon = lons[e.substr(e.indexOf('_') + 1, e.length - 1)];

			if (obs && lon && lat) {

				var points = [ embryo.map.createPoint(lon - half, lat - half),
						embryo.map.createPoint(lon + half, lat - half),
						embryo.map.createPoint(lon + half, lat + half),
						embryo.map.createPoint(lon - half, lat + half) ];
				var square = new OpenLayers.Geometry.LinearRing(points);
				var feature = new OpenLayers.Feature.Vector(square, {
					level : level,
					obs : obs
				});
				features.push(feature);
			}
		}
		return features;

	};

    that.drawSpeed = function (forecast, time) {
		var indexEast = forecast.variables['Ice speed east'];
		var indexNorth = forecast.variables['Ice speed north'];
		var lats = forecast.metadata.lat;
		var lons = forecast.metadata.lon;
		var features = [];
		var half = 0.1;

		var entries = forecast.data[time].entries;

		for ( var e in entries) {
			var east = entries[e][indexEast];
			var north = entries[e][indexNorth];
			if (east || north) {
				var speed = Math.sqrt(north * north + east * east);
                var level = that.getSpeedLevel(speed);
				var lat = lats[e.substr(0, e.indexOf('_'))];
				var lon = lons[e.substr(e.indexOf('_') + 1, e.length - 1)];

                // Once we know the new code works, this (and the two similar blocks below) needs to go.
                /*var points = new Array(embryo.map.createPoint(lon, lat + half),
                 embryo.map.createPoint(lon + half, lat + (half * 0.2)),
                 embryo.map.createPoint(lon + (half * 0.2), lat
                 + (half * 0.2)), embryo.map.createPoint(lon
								+ (half * 0.2), lat - (half * 0.5)), embryo.map
								.createPoint(lon - (half * 0.2), lat
										- (half * 0.5)), embryo.map
								.createPoint(lon - (half * 0.2), lat
										+ (half * 0.2)), embryo.map
								.createPoint(lon - half, lat + (half * 0.2)));


				var linearRing = new OpenLayers.Geometry.LinearRing(points);
                 linearRing.rotate(degrees, embryo.map.createPoint(lon, lat));*/

                var col = level.slice(1);

                var rad = Math.atan2(north, east);
                var degrees = Math.round(rad * 180 / Math.PI);

                var point = embryo.map.createPoint(lon, lat);

                var feature = new OpenLayers.Feature.Vector(point, {
					level : level,
                    obs: east + '/' + north,
                    angle: degrees,
                    col: col
				});
				features.push(feature);

			}

		}
		return features;
	};

    that.drawAccretion = function (forecast, time) {
		var index = forecast.variables['Ice accretion risk'];
		var lats = forecast.metadata.lat;
		var lons = forecast.metadata.lon;
		var features = [];
		var half = 0.2;

		var entries = forecast.data[time].entries;

		for ( var e in entries) {
			var obs = entries[e][index];
            var level = that.getIceAccretionLevel(obs);
			var lat = lats[e.substr(0, e.indexOf('_'))];
			var lon = lons[e.substr(e.indexOf('_') + 1, e.length - 1)];

			if (obs && lon && lat) {

				var points = [ embryo.map.createPoint(lon - half, lat - half),
						embryo.map.createPoint(lon + half, lat - half),
						embryo.map.createPoint(lon + half, lat + half),
						embryo.map.createPoint(lon - half, lat + half) ];
				var square = new OpenLayers.Geometry.LinearRing(points);
				var feature = new OpenLayers.Feature.Vector(square, {
					level : level,
					obs : obs
				});
				features.push(feature);
			}
		}
		return features;
	};

    that.drawIceForecast = function (forecast, time, mapType) {
		that.clear();

		var bounds = that.getBounds(forecast);
        that.layers.forecasts.addFeatures(that.drawFrame(bounds));

		var features = [];
		switch (mapType) {
		case 'iceConcentration':
            features = that.drawConcentration(forecast, time);
			break;
		case 'iceThickness':
            features = that.drawThickness(forecast, time);
			break;
		case 'iceSpeed':
            features = that.drawSpeed(forecast, time);
			break;
		case 'iceAccretion':
            features = that.drawAccretion(forecast, time);
			break;
		}

		that.layers.forecasts.addFeatures(features);
		that.layers.forecasts.refresh();
		that.zoomToForecast(bounds);
	};

    that.getWaveConcentrationLevel = function (obs) {
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

    that.drawWaveForecast = function (forecast, time, provider) {
		that.clear();
		var bounds = that.getBounds(forecast);
        that.layers.forecasts.addFeatures(that.drawFrame(bounds));

		var vars = forecast.variables;
		var lats = forecast.metadata.lat;
		var lons = forecast.metadata.lon;
		var features = [];

		var indexHeight = vars['Significant wave height'];
		var indexDirection = vars['Mean wave direction'];
		var indexPeriod = vars['Mean wave period'];
		var half = 0.1;

		var entries = forecast.data[time].entries;

		for ( var e in entries) {
			var height = entries[e][indexHeight];
			var direction = entries[e][indexDirection];
			var period = entries[e][indexPeriod];

			if (direction) {
				var lat = lats[e.substr(0, e.indexOf('_'))];
				var lon = lons[e.substr(e.indexOf('_') + 1, e.length - 1)];
                var level = that.getWaveHeightLevel(height);

                var col = level.slice(1);

                /*var pFactor = period / 8;
                 var points = new Array(embryo.map.createPoint(lon, lat + (half * pFactor)),
                 embryo.map.createPoint(lon + half, lat + (half * 0.2)),
                 embryo.map.createPoint(lon + (half * 0.2), lat
								+ (half * 0.2)), embryo.map.createPoint(lon
								+ (half * 0.2), lat - (half * 0.5 * pFactor)), embryo.map
								.createPoint(lon - (half * 0.2), lat
										- (half * 0.5 * pFactor)), embryo.map
								.createPoint(lon - (half * 0.2), lat
										+ (half * 0.2)), embryo.map
								.createPoint(lon - half, lat + (half * 0.2)));
				var linearRing = new OpenLayers.Geometry.LinearRing(points);
				var degrees = Math.round(direction * 180 / Math.PI);
				linearRing.rotate(degrees, embryo.map
                 .createPoint(lon, lat));*/

                /*if (provider == 'FCOO') {*/
                    direction = Math.round(direction * 180 / Math.PI);
                /*} else {
                    direction += 180;
                }*/

                var point = embryo.map.createPoint(lon, lat);

                var feature = new OpenLayers.Feature.Vector(point, {
					level : level,
                    obs: height + '/' + period,
                    col: col,
                    angle: direction + (provider == 'FCOO' ? 0 : 180)
				});
				features.push(feature);
			}
		}

		that.layers.forecasts.addFeatures(features);
		that.layers.forecasts.refresh();
		that.zoomToForecast(bounds);
	};

    that.drawCurrentForecast = function (forecast, time) {
		that.clear();
		var bounds = that.getBounds(forecast);
        that.layers.forecasts.addFeatures(that.drawFrame(bounds));

		var indexEast = forecast.variables['Current east'];
		var indexNorth = forecast.variables['Current north'];
		var lats = forecast.metadata.lat;
		var lons = forecast.metadata.lon;
		var features = [];
		var half = 0.1;

		var entries = forecast.data[time].entries;

		for ( var e in entries) {
			var east = entries[e][indexEast];
			var north = entries[e][indexNorth];
			if (east || north) {
				var speed = Math.sqrt(north * north + east * east);
                var level = that.getSpeedLevel(speed);
				var lat = lats[e.substr(0, e.indexOf('_'))];
				var lon = lons[e.substr(e.indexOf('_') + 1, e.length - 1)];

                /*var points = new Array(embryo.map.createPoint(lon, lat + half),
                 embryo.map.createPoint(lon + half, lat + (half * 0.2)),
                 embryo.map.createPoint(lon + (half * 0.2), lat
                 + (half * 0.2)), embryo.map.createPoint(lon
								+ (half * 0.2), lat - (half * 0.5)), embryo.map
								.createPoint(lon - (half * 0.2), lat
										- (half * 0.5)), embryo.map
								.createPoint(lon - (half * 0.2), lat
										+ (half * 0.2)), embryo.map
								.createPoint(lon - half, lat + (half * 0.2)));

				var linearRing = new OpenLayers.Geometry.LinearRing(points);
                 linearRing.rotate(degrees, embryo.map.createPoint(lon, lat));*/

                var col = level.slice(1);

                var rad = Math.atan2(north, east);
                var degrees = Math.round(rad * 180 / Math.PI);

                var point = embryo.map.createPoint(lon, lat);

                var feature = new OpenLayers.Feature.Vector(point, {
					level : level,
                    obs: speed,
                    angle: degrees,
                    col: col
				});
				features.push(feature);

			}
		}
		that.layers.forecasts.addFeatures(features);
		that.layers.forecasts.refresh();
		that.zoomToForecast(bounds);
	};

}

ForecastLayer.prototype = new EmbryoLayer();
