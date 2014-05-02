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
            open : function(data) {
                embryo.controllers.ais.updateInfo(data);
                $('#aisInformationPanel').css('display', 'block');
            },
            close : function(data) {
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
        },
        showAesDialog: function (data) {
            if (!data.ais) return;
    
            var html = "";
    
            var link = "http://www.marinetraffic.com/ais/shipdetails.aspx?mmsi="+data.ais.mmsi;
    
            var egenskaber = {
                "MMSI": data.ais.mmsi,
                "Class": data.ais["class"],
                "Name": data.ais.name,
                "Call Sign": data.ais.callsign,
                "Vessel Type": data.ais.vesselType,
                "Cargo": data.ais.cargo != "N/A" && data.ais.cargo != "Undefined" ? data.ais.cargo : null,
                "Lat": data.ais.lat,
                "Lon": data.ais.lon,
                "IMO": data.ais.imo,
                "Source": data.ais.source,
                "Type": data.ais.type,
                "Country": data.ais.country,
                "SOG": data.ais.sog,
                "COG": data.ais.cog,
                "Heading": data.ais.heading,
                "Draught": data.ais.draught,
                "ROT": data.ais.rot,
                "Width": data.ais.width,
                "Length": data.ais.length,
                "Destination": data.ais.destination,
                "Nav Status": data.ais.navStatus,
                "ETA": data.ais.eta,
                "Position Accuracy": data.ais.posAcc,
                "Last Report": data.ais.lastReport,
                "More Information": "<a href='"+link+"' target='new_window'>"+link+"</a>"
            };
            
            $.each(egenskaber, function(k,v) {
                if (v != null && v != "") html += "<tr><th>"+k+"</th><td>"+v+"</td></tr>";
            });
    
            $("#aesModal h2").html("AIS Information - " + data.ais.name);
            $("#aesModal table").html(html);
            $("#vesselControlPanel").css("display", "block");
        }
    };
})();