// AIS information + tracks for individual vessels
// Listens for vessel selected events.

(function() {

    var module = angular.module('embryo.aisinformation', ['embryo.vessel']);
    
    var convertTable = {
            'mmsi' : 'MMSI',
            'class' : 'Class',
            'name' : 'Name',
            'callsign' : 'Call Sign',
            'cargo' : 'Cargo',
            'country' : 'Country',
            'sog' : 'SOG',
            'cog' : 'COG',
            'destination' : 'Destination',
            'navStatus' : 'Nav Status',
            'eta' : 'ETA',
            'lat' : 'Lat',
            'lon' : 'Lon',
            'posAcc' : 'Position Accuracy',
            'imo' : 'IMO',
            'source' : 'Source',
            'type' : 'Type',
            'sog' : 'SOG',
            'cog' : 'COG',
            'heading' : 'Heading',
            'draught' : 'Draught',
            'rot' : 'ROT',
            'width' : 'Width',
            'length' : 'Length',
            'lastReport' : 'Last Report'
    };
    
    embryo.AISInformationCtrl = function($scope, VesselService) {
        
        embryo.controllers.ais = {
            updateInfo : function(data) {
                $scope.vessel = [];
                for(var x in convertTable) {
                    if(data[x] && data[x] != 'N/A' && data[x] != 'Undefined') {
                        $scope.vessel.push({'key' : convertTable[x], 'value' : data[x]});
                    }
                }
                $scope.vname = data['name'];
                $scope.link = 'http://www.marinetraffic.com/ais/shipdetails.aspx?mmsi=' + data['mmsi'];
                $scope.$apply();
            },
            show : function(data) {
                embryo.controllers.ais.updateInfo(data);
                $('#aisInformationPanel').css('display', 'block');
            },
            hide : function(data) {
                $('#aisInformationPanel').css('display', 'none');
            }
        };
        
        $scope.close = embryo.controllers.ais.close;
        
    };
    
    embryo.aisInformation = {
        renderYourShipShortTable: function (data) {
            if (!data.ais) return "<span class='label label-important'>AIS UNAVAILABLE</span>";
            var html = "";
    
            var egenskaber = {
                "MMSI": data.ais.mmsi,
                "Call Sign": data.ais.callsign,
                "Country": data.ais.country,
                "Destination": data.ais.destination,
                "Nav Status": data.ais.navStatus,
                "ETA": data.ais.eta
            };
    
            $.each(egenskaber, function(k,v) {
                if (v != null && v != "") html += "<tr><th>"+k+"</th><td>"+v+"</td></tr>";
            });
    
            return html;
        },
        renderSelectedShipShortTable: function (data) {
            if (!data.ais) return "<span class='label label-important'>AIS UNAVAILABLE</span>";
            var html = "";
            
            var egenskaber = {
                "MMSI": data.ais.mmsi,
                "Class": data.ais["class"],
                "Call Sign": data.ais.callsign,
                "Vessel Type": data.ais.vesselType,
                "Cargo": data.ais.cargo != "N/A" && data.ais.cargo != "Undefined" ? data.ais.cargo : null,
                "Country": data.ais.country,
                "SOG": data.ais.sog,
                "COG": data.ais.cog,
                "Destination": data.ais.destination,
                "Nav Status": data.ais.navStatus,
                "ETA": data.ais.eta
            };
            
            $.each(egenskaber, function(k,v) {
                if (v != null && v != "") html += "<tr><th>"+k+"</th><td>"+v+"</td></tr>";
            });
    
            return html;
        }
    };
})();