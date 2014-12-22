/*
 * Copyright (c) 2011 Danish Maritime Authority.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
function Grid(size) {
    this.cellSizeInDegrees = size;
    this.multiplier = 360 / this.cellSizeInDegrees;

    this.getCellId = function (_lat, _lon) {
        // Negative longitudes are handled by adding 360 (for backwards compatibility).
        //
        // floor(_lat / GEO_CELL_SIZE) Range -1800..1800, span 3600
        // static_cast<long>((360.0 + _lon)/GEO_CELL_SIZE) Range 3600..10800, span 7200
        // static_cast<long>(360.0 / GEO_CELL_SIZE) Constant = 7200
        // Result of last two lines Range -3600..3600, span 7200
        return Math.floor(Math.floor(_lat / this.cellSizeInDegrees) * this.multiplier)
            + Math.floor((360 + _lon) / this.cellSizeInDegrees) - Math.floor(360 / this.cellSizeInDegrees);
    }

    this.getGeoPosOfCellId = function (id) {
        // Make lonPart range be 0..7200
        var modId = id + Math.floor((360 / this.cellSizeInDegrees) / 2);
        // Cut off lonPart
        var latPart = Math.floor(modId / this.multiplier);
        // Move lonPart range back again
        modId -= Math.floor((360 / this.cellSizeInDegrees) / 2);
        var lonPart = Math.floor(modId - latPart * this.multiplier);

        var lat = this.cellSizeInDegrees * latPart;
        var lon = this.cellSizeInDegrees * lonPart;

        return new Position(lat, lon);
    }

    this.getCell = function (id) {
        var from = this.getGeoPosOfCellId(id);
        var toLon = from.lon + this.cellSizeInDegrees;
        var toLat = from.lat + this.cellSizeInDegrees;

        return { from: from, to: new Position(toLon, toLat)};

    }


}

function Position(lon, lat) {
    this.lon = lon;
    this.lat = lat;

    this.geodesicDistanceTo = function (pos2) {
        return vincentyFormula(this.lat, this.lon, pos2.lat, pos2.lon, "DISTANCE");
    }
}

function toRadians(degrees) {
    return degrees * Math.PI / 180;
}

function toDegrees(radians) {
    return radians * 180 / Math.PI;
}

/**
 * Vincenty formula
 */
function vincentyFormula(latitude1, longitude1, latitude2, longitude2, type) {
    var a = 6378137;
    var b = 6356752.3142;
    var f = 1 / 298.257223563; // WGS-84 ellipsiod
    var L = toRadians(longitude2 - longitude1);
    var U1 = Math.atan((1 - f) * Math.tan(toRadians(latitude1)));
    var U2 = Math.atan((1 - f) * Math.tan(toRadians(latitude2)));
    var sinU1 = Math.sin(U1), cosU1 = Math.cos(U1);
    var sinU2 = Math.sin(U2), cosU2 = Math.cos(U2);

    var lambda = L;
    var lambdaP = 2 * Math.PI;
    var iterLimit = 20;
    var sinLambda = 0;
    var cosLambda = 0;
    var sinSigma = 0;
    var cosSigma = 0;
    var sigma = 0;
    var sinAlpha = 0;
    var cosSqAlpha = 0;
    var cos2SigmaM = 0;
    var C;
    while (Math.abs(lambda - lambdaP) > 1e-12 && --iterLimit > 0) {
        sinLambda = Math.sin(lambda);
        cosLambda = Math.cos(lambda);
        sinSigma = Math.sqrt((cosU2 * sinLambda) * (cosU2 * sinLambda)
            + (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda) * (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda));
        if (sinSigma == 0) {
            return 0; // co-incident points
        }
        cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * cosLambda;
        sigma = Math.atan2(sinSigma, cosSigma);
        sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma;
        cosSqAlpha = 1 - sinAlpha * sinAlpha;
        cos2SigmaM = cosSigma - 2 * sinU1 * sinU2 / cosSqAlpha;
        if (Number.isNaN(cos2SigmaM)) {
            cos2SigmaM = 0; // equatorial line
        }
        C = f / 16 * cosSqAlpha * (4 + f * (4 - 3 * cosSqAlpha));
        lambdaP = lambda;
        lambda = L + (1 - C) * f * sinAlpha
            * (sigma + C * sinSigma * (cos2SigmaM + C * cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM)));
    }
    if (iterLimit == 0) {
        return Number.NaN; // formula failed to converge
    }

    var uSq = cosSqAlpha * (a * a - b * b) / (b * b);
    var A = 1 + uSq / 16384 * (4096 + uSq * (-768 + uSq * (320 - 175 * uSq)));
    var B = uSq / 1024 * (256 + uSq * (-128 + uSq * (74 - 47 * uSq)));
    var deltaSigma = B
        * sinSigma
        * (cos2SigmaM + B
            / 4
            * (cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM) - B / 6 * cos2SigmaM
                * (-3 + 4 * sinSigma * sinSigma) * (-3 + 4 * cos2SigmaM * cos2SigmaM)));
    var distance = b * A * (sigma - deltaSigma);
    if (type == "DISTANCE") {
        return distance;
    }
    // initial bearing
    var fwdAz = Math.toDegrees(Math.atan2(cosU2 * sinLambda, cosU1 * sinU2 - sinU1 * cosU2 * cosLambda));
    if (type == "INITIAL_BEARING") {
        return fwdAz;
    }
    // final bearing
    return toDegrees(Math.atan2(cosU1 * sinLambda, -sinU1 * cosU2 + cosU1 * sinU2 * cosLambda));
}

function ClusterCell(from, to) {
    this.from = from;
    this.to = to;
    this.count = 0;
    this.items = [];

    this.incrementCount = function () {
        this.count = this.count + 1;
        return this.count;
    }

    this.addItem = function (item) {
        this.items.push(item);
    }

    this.clearItems = function () {
        this.items = [];
    }

    this.getDensity = function () {
        if (!this.density) {
            var topRight = new Position(this.to.lon, this.from.lat);
            var botLeft = new Position(this.from.lon, this.to.lat);
            var width = from.geodesicDistanceTo(topRight) / 1000;
            var height = from.geodesicDistanceTo(botLeft) / 1000;
            var areaSize = width * height;
            this.density = this.count / areaSize;
        }
        return this.density;
    }

}

function Cluster(vessels, grid, lim) {
    var limit = lim;
    var cells = {}

    this.getCells = function () {
        var result = [];
        for (var key in cells) {
            result.push(cells[key]);
        }
        return result;
    }

    this.getCell = function (id) {
        return cells[id];
    }

    this.addCell = function (id, clusterCell) {
        cells[id] = clusterCell;
    }

    this.contains = function (cellId) {
        return cells.hasOwnProperty(cellId);
    }

    var that = this;
    $.each(vessels, function (index, vessel) {

        //console.log(vessel);
        // vessel has no position info
        // return

        var cellId = grid.getCellId(vessel.x, vessel.y);

        if (!that.contains(cellId)) {
            var cell = grid.getCell(cellId);
            var clusterCell = new ClusterCell(cell.from, cell.to)
            that.addCell(cellId, clusterCell);
        }
        var cell = that.getCell(cellId);
        if (cell.incrementCount() < limit) {
            cell.addItem(vessel);
        } else {
            cell.clearItems();
        }
    });

};