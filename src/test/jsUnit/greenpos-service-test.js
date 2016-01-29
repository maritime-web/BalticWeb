/* Copyright (c) 2011 Danish Maritime Authority.
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
describe('GreenposService', function () {
    function lastKnownReport(type) {
        return {
            ts: new Date().getTime(),
            type: type
        };
    }

    describe('defaultReportType', function () {
        var service;
        beforeEach(module('embryo.greenposService', function ($provide) {
            $rootScope = {};
            $http = {};
            SessionStorageService = {};
            LocalStorageService = {};
            $provide.value('$rootScope', $rootScope);
            $provide.value('$http', $http);
            $provide.value('SessionStorageService', SessionStorageService);
            $provide.value('LocalStorageService', LocalStorageService);
        }));
        beforeEach(inject(function (GreenposService) {
            service = GreenposService;
        }));

        it('Default report type must be SP if last known report is not available', function () {
            var lastKnownReport = null;
            expect(service.defaultReportType(lastKnownReport)).toBe("SP");
        });
        it('Default report type must be SP if last known report timestamp is not available', function () {
            var lastKnownReport = {};
            expect(service.defaultReportType(lastKnownReport)).toBe("SP");
        });
        it('Default report type must be SP if last known report type is FR', function () {
            expect(service.defaultReportType(lastKnownReport("FR"))).toBe("SP");
        });
        it('Default report type must be SP if last known report type is FR', function () {
            expect(service.defaultReportType(lastKnownReport("FR"))).toBe("SP");
        });
        it('Default report type must be PR if last known report type is PR', function () {
            expect(service.defaultReportType(lastKnownReport("PR"))).toBe("PR");
        });
        it('Default report type must be PR if last known report type is DR', function () {
            expect(service.defaultReportType(lastKnownReport("DR"))).toBe("PR");
        });
        it('Default report type must be PR if last known report type is SP', function () {
            expect(service.defaultReportType(lastKnownReport("SP"))).toBe("PR");
        });
        it('Default report type must be SP if last known report type is unknown', function () {
            var lastKnownReport = {
                ts: new Date().getTime()
            };
            expect(service.defaultReportType(lastKnownReport)).toBe("SP");
        });
    });
    describe('nextReportNumber', function () {
        var service;
        var nextNumber;
        var localStorageReturnValue;
        beforeEach(module('embryo.greenposService', function ($provide) {
            $rootScope = {};
            $http = {};
            SessionStorageService = {};
            LocalStorageService = {
                getItem: function (key, callback) {
                    callback(localStorageReturnValue);
                }
            };
            $provide.value('$rootScope', $rootScope);
            $provide.value('$http', $http);
            $provide.value('SessionStorageService', SessionStorageService);
            $provide.value('LocalStorageService', LocalStorageService);
        }));
        beforeEach(inject(function (GreenposService) {
            service = GreenposService;
        }));


        function executeAndVerify(done, mmsi, recipient, type, expected) {
            nextNumber = null;
            service.nextReportNumber(mmsi, recipient, type, function (nn) {
                done();
                nextNumber = nn;
            });
            expect(nextNumber).toBeDefined();
            expect(nextNumber.number).toBe(expected.number);
            expect(nextNumber.uncertainty).toBe(expected.uncertainty);
        }

        it('next number is always 1 with no uncertainty for reports of type SP', function (done) {
            executeAndVerify(done, 12345678, "greenpos", "SP", {number: 1, uncertainty: false});
        });
        it('next number is always 1 but uncertain for reports of type PR if previous report number is unknown', function (done) {
            localStorageReturnValue = null;
            executeAndVerify(done, 12345678, "greenpos", "PR", {number: 1, uncertainty: true});
        });
        it('next number is always 1 but uncertain for reports of type DR if previous report number is unknown', function (done) {
            localStorageReturnValue = null;
            executeAndVerify(done, 12345678, "greenpos", "DR", {number: 1, uncertainty: true});
        });
        it('next number is always 1 but uncertain for reports of type FR if previous report number is unknown', function (done) {
            localStorageReturnValue = null;
            executeAndVerify(done, 12345678, "greenpos", "FR", {number: 1, uncertainty: true});
        });
        it('next number is incremented and certain for reports of type PR performed less than 7 hours since last report', function (done) {
            localStorageReturnValue = {
                number: 1,
                ts: Date.now() - 6 * 60 * 60 * 1000
            };
            executeAndVerify(done, 12345678, "greenpos", "PR", {number: 2, uncertainty: false});
        });
        it('next number is incremented and certain for reports of type DR performed less than 7 hours since last report', function (done) {
            localStorageReturnValue = {
                number: 2,
                ts: Date.now() - 6 * 60 * 60 * 1000
            };
            executeAndVerify(done, 12345678, "greenpos", "DR", {number: 3, uncertainty: false});
        });
        it('next number is incremented and certain for reports of type FR performed less than 7 hours since last report', function (done) {
            localStorageReturnValue = {
                number: 3,
                ts: Date.now() - 4 * 60 * 60 * 1000
            };
            executeAndVerify(done, 12345678, "greenpos", "FR", {number: 4, uncertainty: false});
        });
        it('next number is incremented and uncertain for reports of type PR performed more than 7 hours later than last report', function (done) {
            localStorageReturnValue = {
                number: 1,
                ts: Date.now() - 8 * 60 * 60 * 1000
            };
            executeAndVerify(done, 12345678, "greenpos", "PR", {number: 2, uncertainty: true});
        });
        it('next number is incremented and uncertain for reports of type DR performed more than 7 hours later than last report', function (done) {
            localStorageReturnValue = {
                number: 2,
                ts: Date.now() - 7 * 60 * 60 * 1000 - 60 * 1000
            };
            executeAndVerify(done, 12345678, "greenpos", "DR", {number: 3, uncertainty: true});
        });
        it('next number is incremented and uncertain for reports of type FR performed more than 7 hours later than last report', function (done) {
            localStorageReturnValue = {
                number: 3,
                ts: Date.now() - 12 * 60 * 60 * 1000
            };
            executeAndVerify(done, 12345678, "greenpos", "FR", {number: 4, uncertainty: true});
        });

    });

});