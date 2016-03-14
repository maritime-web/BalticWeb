maritimeweb.groupLayers = new function () {
    return new ol.layer.Group({
        'title': 'Base maps',
        layers: [
            new ol.layer.Tile({
                title: 'Stamen - Water color',
                type: 'base',
                visible: false,
                source: new ol.source.Stamen({
                    layer: 'watercolor'
                })
            }),
            new ol.layer.Tile({
                title: 'Stamen - Toner',
                type: 'base',
                visible: false,
                source: new ol.source.Stamen({
                    layer: 'toner'
                })
            }),
            new ol.layer.Tile({
                title: 'MapQuest - OSM',
                type: 'base',
                visible: false,
                source: new ol.source.MapQuest({
                    layer: 'osm'
                })
            }),
            new ol.layer.Tile({
                title: 'MapQuest - Satellite',
                type: 'base',
                visible: false,
                source: new ol.source.MapQuest({
                    layer: 'sat'
                })
            }),
            new ol.layer.Tile({
                title: 'MapQuest - Hybrid',
                type: 'base',
                visible: false,
                source: new ol.source.MapQuest({
                    layer: 'hyb'
                })
            }),
            new ol.layer.Tile({
                title: 'Thunderforest - OpenCycleMap',
                type: 'base',
                visible: false,
                source: new ol.source.OSM({
                    url: 'http://{a-c}.tile.thunderforest.com/cycle/{z}/{x}/{y}.png',
                    attributions: thunderforestAttributions
                })
            }),
            new ol.layer.Tile({
                title: 'Thunderforest - Outdoors',
                type: 'base',
                visible: false,
                source: new ol.source.OSM({
                    url: 'http://{a-c}.tile.thunderforest.com/outdoors/{z}/{x}/{y}.png',
                    attributions: thunderforestAttributions
                })
            }),
            new ol.layer.Tile({
                title: 'Thunderforest - Landscape',
                type: 'base',
                visible: false,
                source: new ol.source.OSM({
                    url: 'http://{a-c}.tile.thunderforest.com/landscape/{z}/{x}/{y}.png',
                    attributions: thunderforestAttributions
                })
            }),
            new ol.layer.Tile({
                title: 'Thunderforest - Transport',
                type: 'base',
                visible: false,
                source: new ol.source.OSM({
                    url: 'http://{a-c}.tile.thunderforest.com/transport/{z}/{x}/{y}.png',
                    attributions: thunderforestAttributions
                })
            }),
            new ol.layer.Tile({
                title: 'Thunderforest - Transport Dark',
                type: 'base',
                visible: false,
                source: new ol.source.OSM({
                    url: 'http://{a-c}.tile.thunderforest.com/transport-dark/{z}/{x}/{y}.png',
                    attributions: thunderforestAttributions
                })
            }),
            new ol.layer.Tile({
                title: 'OSM',
                type: 'base',
                visible: true,
                source: new ol.source.OSM()
            }),
            //uyyy

            new ol.layer.Tile({
                title: 'wind_vector_5',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'wind_vector/5/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'wind_vector_7',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'wind_vector/7/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'wind_vector_9',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'wind_vector/9/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'wind_vector_11',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'wind_vector/11/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'wind_vector_15',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'wind_vector/15/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'wind_vector_19',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'wind_vector/19/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'wind_vector_23',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'wind_vector/23/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'wind_vector_27',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'wind_vector/27/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'wind_stream_5',
                visible: true,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'wind_stream/5/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'wind_stream_7',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'wind_stream/7/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'wind_stream_9',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'wind_stream/9/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'wind_stream_11',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'wind_stream/11/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'wind_stream_15',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'wind_stream/15/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'wind_stream_19',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'wind_stream/19/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'wind_stream_23',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'wind_stream/23/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'wind_stream_27',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'wind_stream/27/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'gust_5',
                visible: false,
                opacity: 0.3,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'gust/5/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'gust_7',
                visible: false,
                opacity: 0.3,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'gust/7/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'gust_9',
                visible: false,
                opacity: 0.3,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'gust/9/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'gust_11',
                visible: false,
                opacity: 0.3,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'gust/11/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'gust_15',
                visible: false,
                opacity: 0.3,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'gust/15/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'gust_19',
                visible: false,
                opacity: 0.3,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'gust/19/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'gust_23',
                visible: false,
                opacity: 0.3,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'gust/23/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'gust_27',
                visible: false,
                opacity: 0.3,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'gust/27/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'surface_pressure_5',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'surface_pressure/5/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'surface_pressure_7',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'surface_pressure/7/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'surface_pressure_9',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'surface_pressure/9/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'surface_pressure_11',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'surface_pressure/11/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'surface_pressure_15',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'surface_pressure/15/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'surface_pressure_19',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'surface_pressure/19/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'surface_pressure_23',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'surface_pressure/23/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'surface_pressure_27',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'surface_pressure/27/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'air_temperature_5',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'air_temperature/5/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'air_temperature_7',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'air_temperature/7/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'air_temperature_9',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'air_temperature/9/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'air_temperature_11',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'air_temperature/11/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'air_temperature_15',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'air_temperature/15/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'air_temperature_19',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'air_temperature/19/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'air_temperature_23',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'air_temperature/23/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'air_temperature_27',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'air_temperature/27/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'precipitation_5',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'precipitation/5/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'precipitation_7',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'precipitation/7/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'precipitation_9',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'precipitation/9/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'precipitation_11',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'precipitation/11/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'precipitation_15',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'precipitation/15/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'precipitation_19',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'precipitation/19/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'precipitation_23',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'precipitation/23/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'precipitation_27',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'precipitation/27/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'significant_wave_height_5',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'significant_wave_height/5/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'significant_wave_height_7',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'significant_wave_height/7/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'significant_wave_height_9',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'significant_wave_height/9/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'significant_wave_height_11',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'significant_wave_height/11/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'significant_wave_height_15',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'significant_wave_height/15/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'significant_wave_height_19',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'significant_wave_height/19/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'significant_wave_height_23',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'significant_wave_height/23/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'significant_wave_height_27',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'significant_wave_height/27/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'wind_barb_5',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'wind_barb/5/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'wind_barb_7',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'wind_barb/7/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'wind_barb_9',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'wind_barb/9/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'wind_barb_11',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'wind_barb/11/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'wind_barb_15',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'wind_barb/15/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'wind_barb_19',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'wind_barb/19/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'wind_barb_23',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'wind_barb/23/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'wind_barb_27',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'wind_barb/27/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'FL100_wind_barb_5',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'FL100_wind_barb/5/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'FL100_wind_barb_7',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'FL100_wind_barb/7/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'FL100_wind_barb_9',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'FL100_wind_barb/9/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'FL100_wind_barb_11',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'FL100_wind_barb/11/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'FL100_wind_barb_15',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'FL100_wind_barb/15/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'FL100_wind_barb_19',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'FL100_wind_barb/19/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'FL100_wind_barb_23',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'FL100_wind_barb/23/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'FL100_wind_barb_27',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'FL100_wind_barb/27/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'FL200_wind_barb_5',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'FL200_wind_barb/5/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'FL200_wind_barb_7',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'FL200_wind_barb/7/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'FL200_wind_barb_9',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'FL200_wind_barb/9/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'FL200_wind_barb_11',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'FL200_wind_barb/11/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'FL200_wind_barb_15',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'FL200_wind_barb/15/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'FL200_wind_barb_19',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'FL200_wind_barb/19/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'FL200_wind_barb_23',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'FL200_wind_barb/23/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'FL200_wind_barb_27',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'FL200_wind_barb/27/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'FL300_wind_barb_5',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'FL300_wind_barb/5/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'FL300_wind_barb_7',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'FL300_wind_barb/7/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'FL300_wind_barb_9',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'FL300_wind_barb/9/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'FL300_wind_barb_11',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'FL300_wind_barb/11/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'FL300_wind_barb_15',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'FL300_wind_barb/15/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'FL300_wind_barb_19',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'FL300_wind_barb/19/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'FL300_wind_barb_23',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'FL300_wind_barb/23/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'FL300_wind_barb_27',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'FL300_wind_barb/27/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'FL400_wind_barb_5',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'FL400_wind_barb/5/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'FL400_wind_barb_7',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'FL400_wind_barb/7/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'FL400_wind_barb_9',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'FL400_wind_barb/9/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'FL400_wind_barb_11',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'FL400_wind_barb/11/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'FL400_wind_barb_15',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'FL400_wind_barb/15/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'FL400_wind_barb_19',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'FL400_wind_barb/19/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'FL400_wind_barb_23',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'FL400_wind_barb/23/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'FL400_wind_barb_27',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'FL400_wind_barb/27/{z}/{x}/{y}.png'
                })
            })

            //xxxx
        ]
    });

};
maritimeweb.groupOverlays = new function () {
    var group =  new ol.layer.Group({
        title: 'Overlays',
        layers: [
            new ol.layer.Tile({
                title: 'Countries',
                source: new ol.source.TileWMS({
                    url: 'http://demo.opengeo.org/geoserver/wms',
                    params: {'LAYERS': 'ne:ne_10m_admin_1_states_provinces_lines_shp'},
                    serverType: 'geoserver'
                })
            }),
            new ol.layer.Tile({
                title: 'Weather !',
                source: new ol.source.TileWMS({
                    url: 'http://weather.openportguide.de/tiles/actual/wind_stream/5/{z}/{x}/{y}.png'
                    //url: 'http://weather.openportguide.de/tiles/actual/air_temperature/wind_stream/5/{z}/{x}/{y}.png'
                }),
                visible: false
            }),
            new ol.layer.Tile({
                title: 'Temperature !',
                source: new ol.source.TileWMS({
                    url: 'http://weather.openportguide.de/tiles/actual/wind_stream/5/{z}/{x}/{y}.png'
                    //url: 'http://weather.openportguide.de/tiles/actual/air_temperature/wind_stream/5/{z}/{x}/{y}.png'
                }),
                visible: false
            })



            //http://weather.openportguide.de/tiles/actual//air_temperature/
        ]
    });
    return group;
};