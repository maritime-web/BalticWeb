// AIS information + tracks for individual vessels
// Listens for vessel selected events.

(function() {

    var module = angular.module('embryo.aisinformation', ['embryo.vessel.service']);
    
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
    
    module.controller("AISInformationCtrl", ['$scope','VesselService', 'VesselInformation', function($scope, VesselService, VesselInformation) {
       function updateInfo (data) {
            $scope.vessel = [];
            for(var x in convertTable) {
                if(data[x] && data[x] != 'N/A' && data[x] != 'Undefined') {
                    $scope.vessel.push({'key' : convertTable[x], 'value' : data[x]});
                }
            }
            $scope.vname = data['name'];
            $scope.link = 'http://www.marinetraffic.com/ais/shipdetails.aspx?mmsi=' + data['mmsi'];
        };
        
        $scope.provider = {
            title : 'AIS information',
            type : 'view',
            doShow : false,
            show : function(data) {
                updateInfo(data);
                this.doShow = true;
            },
            close : function() {
                this.doShow = false;
            }
        };
        $scope.close = function($event) {
            $event.preventDefault();
            $scope.provider.close();
        };
        VesselInformation.addInformationProvider($scope.provider);
        embryo.controllers.ais = $scope.provider;
    }]);
})();