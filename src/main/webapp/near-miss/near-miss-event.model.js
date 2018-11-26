/**
 * @classdesc
 * Represents a Near miss event between two vessels.
 * @constructor
 * @param {*} parameters
 */
function NearMissEventModel(parameters) {
    var that = this;
    that.id = parameters.id;
    that.ownStates = parameters.ownStates;
    that.otherStates = parameters.otherStates;
    that.otherMmsi = that.otherStates[0].mmsi;
    that.startTime = that.otherStates[0].time;

    that.getOwnLonLats = function () {
        return that.ownStates.map(function (state) {
            return state.getLonLat();
        });
    };

    that.getOtherLonLats = function () {
        return that.otherStates.map(function (state) {
            return state.getLonLat();
        });
    }
}
