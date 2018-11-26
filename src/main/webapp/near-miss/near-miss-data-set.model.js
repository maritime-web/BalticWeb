/**
 * @classdesc
 * Represents a Near miss data set containing all vessel states in a time interval.
 * @constructor
 * @param {*} parameters
 */
function NearMissDataSetModel(parameters) {
    var that = this;
    that.ownMmsi = parameters.mmsi;
    var rawStates = parameters.vesselStates;

    that.startTime = moment(rawStates[0].time).utc();
    that.endTime = moment(rawStates[rawStates.length - 1].time).utc();

    that.ownStates = extractOwnVesselStates();
    that.otherStates = extractOtherStates();

    that.nearMissEvents = extractNearMissEvents();

    that.animateTarget = null;
    that.zoomTarget = null;

    that.zoomNearMiss = function (nearMissEvent) {
        that.zoomTarget = nearMissEvent;
    };

    that.animateNearMiss = function (nearMissEvent) {
        that.animateTarget = nearMissEvent;
    };

    function extractOwnVesselStates() {
        return rawStates
            .filter(ownFilter).filter(sanityFilter)
            .map(function (state) {
                return new NearMissVesselStateModel(state);
            });
    }

    function ownFilter(state) {
        return that.ownMmsi === state.mmsi;
    }

    function extractOtherStates() {
        var result = {};
        rawStates.filter(otherFilter).filter(sanityFilter).forEach(function (state) {
            var mmsi = state.mmsi;
            if (!result[mmsi]) {
                result[mmsi] = [];
            }
            result[mmsi].push(new NearMissVesselStateModel(state));
        });
        return result;
    }

    function otherFilter(state) {
        return that.ownMmsi !== state.mmsi;
    }

    function sanityFilter(state, index, array) {
        if (index > 0) {
            var prevState = array[index - 1];
            return isSane(state, prevState);
        }
        return true;
    }

    function isSane(state, prevState) {
        if (prevState.mmsi !== state.mmsi) {
            return true;
        }

        var hdg = prevState.hdg;

        if (hdg > 0 && hdg < 180) {//going east
            if (prevState.position.lon > state.position.lon) {
                return false;
            }
        } else {
            if (prevState.position.lon < state.position.lon) {
                return false;
            }
        }

        if (hdg > 90 && hdg < 270) { //going south
            if (prevState.position.lat < state.position.lat) {
                return false;
            }
        } else {
            if (prevState.position.lat > state.position.lat) {
                return false;
            }
        }
        return true;
    }

    function extractNearMissEvents() {
        var nm = false;
        var nearMissEvents = [];
        var ownStates = [];
        var otherStates = [];
        var count = 1;

        function getNextOwnState(minIndex) {
            return rawStates.find(function (state, index) {
                return index > minIndex && state.mmsi === that.ownMmsi;
            });
        }

        function getPrevState(state, index) {
            var prevState = undefined;
            rawStates.forEach(function (s, i) {
                if (i < index && state.mmsi === s.mmsi) {
                    prevState = s;
                }
            });
            return prevState;
        }

        rawStates.forEach(function (state, index) {
            var prevState = getPrevState(state, index);

            if (prevState && !isSane(state, prevState)) {
                return;
            }
            var otherMmsi = state.mmsi !== that.ownMmsi;

            if (!nm && otherMmsi && state.nearMissFlag) {
                //    start near miss
                nm = true;
                otherStates.push(new NearMissVesselStateModel(state));

            } else if (nm && otherMmsi && !state.nearMissFlag) {
                //    end near miss
                nm = false;
                otherStates.push(new NearMissVesselStateModel(state));
                ownStates.push(new NearMissVesselStateModel(getNextOwnState(index)));

                nearMissEvents.push(new NearMissEventModel({id: count++, ownStates: ownStates, otherStates: otherStates}));
                ownStates = [];
                otherStates = [];

            } else if (nm) {
                if (state.mmsi === that.ownMmsi) {
                    ownStates.push(new NearMissVesselStateModel(state));
                } else {
                    otherStates.push(new NearMissVesselStateModel(state));
                }
            }
        });
        return nearMissEvents;
    }

}
