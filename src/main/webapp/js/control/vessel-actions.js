(function() {
    embryo.vessel.actions = {
        selectedVessel : function() {
            return [ "ArcticWeb Reporting", embryo.additionalInformation.route, "Additional Information",
                    embryo.additionalInformation.historicalTrack, embryo.additionalInformation.nearestShips,
                    embryo.additionalInformation.distanceCircles ];
        }
    };
})();
