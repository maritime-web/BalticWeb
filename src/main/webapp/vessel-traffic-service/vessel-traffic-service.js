angular.module('maritimeweb.app').controller('VesselTrafficServiceCtrl', ['$scope', '$uibModalInstance', '$window', '$sce',
    function ($scope, $uibModalInstance, $window, $sce) {
        //Add any new VTS centers here - call them if you miss information - be sure to triple check and ask for reserve VHF channels
        $scope.VTSCenterData = [
            {id: 0, shortname: 'BELTREP', name: 'Denmark - BELTREP - The Great Belt Vessel Traffic Service', callsign:'Great Belt Traffic', email:'vts@beltrep.org', telephone:'+45 58 37 68 68', telephone2:'', fax:'', vhfchannel1:'North 74', vhfchannel2:'South 11', vhfchannel3:'', vhfchannel4:'', vhfreservechannel1:'11', vhfreservechannel2:'',
                iconImage:"img/OpenPortGuideLogo_32.png",
                VTSGuideLink:"http://forsvaret.dk/VTSSTB/eng/Documents/BELTREP%20Information%20ver%200.pdf",
                showMaxDraught:false,
                showAirDraught:true,
                showFuelQuantity:false,
                showFuelDetails:true,
                showVesselType:false,
                showVesselLength:false,
                showDeadWeightTonnage:true,
            },
            {id: 1, shortname: 'SOUNDREP', name: 'Sweden - SOUNDREP - Sound Vessel Traffic Service', callsign:'Sound VTS', email:'contact@soundvts.org', telephone:'+46 771-630600', telephone2:'', fax:'', vhfchannel1:'North 73', vhfchannel2:'South 71', vhfchannel3:'', vhfchannel4:'', vhfreservechannel1:'68', vhfreservechannel2:'79',
                iconImage:"img/MaritimeCloud_logo_mini_45.png",
                VTSGuideLink:"http://www.sjofartsverket.se/pages/32062/SoundVTS2011.pdf",
                showMaxDraught:true,
                showAirDraught:true,
                showFuelQuantity:false,
                showFuelDetails:true,
                showVesselType:false,
                showVesselLength:false,
                showDeadWeightTonnage:false,
            },
            //All GOFREP have same criteria
            {id: 2, shortname: 'GOFREP Helsinki', name: 'Finland - GOFREP - Gulf Of Finland Vessel Traffic Service', callsign:'Helsinki Traffic', email:'gofrep@fta.fi', telephone:'+358 (0)204 48 5387', telephone2:'+358 (0)204 48 5388', fax:'+358 (0)204 48 5394', vhfchannel1:'60', vhfchannel2:'', vhfchannel3:'', vhfchannel4:'', vhfreservechannel1:'80', vhfreservechannel2:'',
                iconImage:"",
                VTSGuideLink:"http://www.vta.ee/public/GOFREP_web.pdf",
                showMaxDraught:true,
                showAirDraught:false,
                showFuelQuantity:true,
                showFuelDetails:false,
                showVesselType:true,
                showVesselLength:true,
                showDeadWeightTonnage:false,
                showCargoInfomation:true,
            },
            {id: 3, shortname: 'GOFREP Tallinn', name: 'Estonia - GOFREP Tallinn - Gulf Of Finland Vessel Traffic Service', callsign:'Tallinn Traffic', email:'gofrep@vta.ee', telephone:'+372 6 205 764', telephone2:'+372 6 205 777', fax:'+372 620 5766', vhfchannel1:'61', vhfchannel2:'', vhfchannel3:'', vhfchannel4:'', vhfreservechannel1:'81', vhfreservechannel2:'',
                iconImage:"",
                VTSGuideLink:"http://www.vta.ee/public/GOFREP_web.pdf",
                showMaxDraught:true,
                showAirDraught:false,
                showFuelDetails:false,
                showFuelQuantity:true,
                showVesselType:true,
                showVesselLength:true,
                showDeadWeightTonnage:false,
            },
            {id: 4, shortname: 'GOFREP St. Petersburg', name: 'Russia - GOFREP Helsinki - Gulf Of Finland Vessel Traffic Service', callsign:'St. Peterburg Traffic', email:'gofrep@rsbm.ru', telephone:'+7 12 380 70 21', telephone2:'+7 812 380 70 81', fax:'+7 812 3880 70 20', vhfchannel1:'74', vhfchannel2:'', vhfchannel3:'', vhfchannel4:'', vhfreservechannel1:'10', vhfreservechannel2:'',
                iconImage:"",
                VTSGuideLink:"http://www.vta.ee/public/GOFREP_web.pdf",
                showMaxDraught:true,
                showAirDraught:false,
                showFuelDetails:false,
                showFuelQuantity:true,
                showVesselType:true,
                showVesselLength:true,
                showDeadWeightTonnage:false,
            },
            {id: 5, shortname: 'TESTREP', name: 'Roland - ROREP - Awesome Vessel Traffic Service', callsign:'YOLO VTS', email:'yolo@swag.org', telephone:'555-no-idea', telephone2:'555-still-dunno', fax:'555-fax-fun', vhfchannel1:'North 1', vhfchannel2:'East 2', vhfchannel3:'West 3', vhfchannel4:'South 4', vhfreservechannel1:'11', vhfreservechannel2:'12',
                iconImage:"img/ring.png",
                VTSGuideLink:"http://images.fandango.com/images/fandangoblog/minions618F1.jpg",
                showMaxDraught:true,
                showAirDraught:true,
                showFuelDetails:true,
                showFuelQuantity:true,
                showVesselType:true,
                showVesselLength:true,
                showDeadWeightTonnage:true,
            },
        ];
        var VTSData = $scope.VTSCenterData;


        $scope.vesselTypes = ["General Cargo","Bulk Carrier","Container Ship","Tanker","Fishing boat (A)","Fishing Processer (B)","Non-Fishing Processer (C)","Oil Industry Vessel","Passenger Ship","Ferry","Tug boat","Barge","Other"];
        $scope.fuelTypes = [
            {name:"HFO", description:"Heavy Fuel Oil"},
            {name:"IFO", description:"Intermediate Fuel Oil"},
            {name:"MDO", description:"Marine Diesel Oil"},
            {name:"MGO", description:"Marine Gas Oil"},
            {name:"LPG", description:"Liquid Petroleum Gas"},
            {name:"LNG", description:"Liquid Natural Gas"},
            {name:"Other", description:""},
        ];
        $scope.cargoTypes = ["Ballast", "Bulk - grain", "Bulk - other than grain", "Chemicals", "Container/Trailer", "General Cargo", "Gas", "Oil", "Passenger", "Reefer", "Other"]

        //Specific VTS center route dropdowns (They usually have predefined routes and abbreviations as specified in their pilot/master's guide )
        $scope.BELTREPRoutes = ["", ""];
        $scope.SOUNDREPRoutes = ["", ""];



    //Input validation area

        //vessel information
        $scope.vtsvesselnameinput = ""; $scope.vtsvesselcallsigninput = ""; $scope.vtsvesselmmsiinput = ""; $scope.vtsvesselmmsilabel = "MMSI: "; $scope.vtsvesselimoinput = "";
        $scope.vtsvesselimolabel = "IMO"; $scope.vtsvesseldraughtinput = ""; $scope.vtsvesselairdraughtinput = ""; $scope.vtsvesselpersonsinput = ""; $scope.vtsvessellengthinput = "";

        //fuel information
        $scope.vtsvesselfueltype00_0input = ""; $scope.vtsvesselfueltype00_1input = ""; $scope.vtsvesselfueltype00_2input = "";
        $scope.vtsvesselfueltype01_0input = ""; $scope.vtsvesselfueltype01_1input = ""; $scope.vtsvesselfueltype01_2input = "";
        $scope.vtsvesselfueltype02_0input = ""; $scope.vtsvesselfueltype02_1input = ""; $scope.vtsvesselfueltype02_2input = "";
        $scope.vtsvesselfueltype03_0input = ""; $scope.vtsvesselfueltype03_1input = ""; $scope.vtsvesselfueltype03_2input = "";
        $scope.vtsvesselfueltype04_0input = ""; $scope.vtsvesselfueltype04_1input = ""; $scope.vtsvesselfueltype04_2input = "";
        $scope.vtsvesselfueltype05_0input = ""; $scope.vtsvesselfueltype05_1input = ""; $scope.vtsvesselfueltype05_2input = "";
        $scope.vtsvesselfueltype06_0input = ""; $scope.vtsvesselfueltype06_1input = ""; $scope.vtsvesselfueltype06_2input = "";

        //Cargo information
        $scope.selectedCargoType = "";
        $scope.vtsvesselcargossnidinput = "";
        $scope.vtsdangerouscargotype01input = ""; $scope.vtsdangerouscargotype02input = ""; $scope.vtsdangerouscargotype03input = ""; $scope.vtsdangerouscargotype04input = "";
        $scope.vtsdangerouscargotype05input = ""; $scope.vtsdangerouscargotype06input = ""; $scope.vtsdangerouscargotype07input = ""; $scope.vtsdangerouscargotype08input = "";
        $scope.vtsdangerouscargotype09input = ""; $scope.vtsdangerouscargotype10input = "";
        $scope.vtsDangCargoCheck = false; //checkbox to activate dangerous cargo in case is not default
        $scope.vtsDangCargoCheckDisabled = true;

        $scope.vtsetadateinput = "";
        $scope.vtsetatimeinput = "";

        $scope.VTSReadyToSend = false; //global readystate

        //vessel information - should be autofilled through service at login
        $scope.setvtsvesselnameValid = false;
        $scope.setvtsvesselcallsignValid = false;
        $scope.setvtsvesselMMSIValid = false;
        $scope.setvtsvesselIMOValid = false;
        $scope.setvtsvesselDraughtValid = false;
        $scope.setvtsvesselAirDraughtValid = false;
        $scope.setvtsvesselPersonsValid = false;
        $scope.setvtsvesselLengthValid = false;
        $scope.setvtsVesselTypeValid = false;

        //fuel information 0:invalid, 1:neutral, 2:valid
        $scope.setvtsFuelType00_0Valid = 0; $scope.setvtsFuelType00_1Valid = 0; $scope.setvtsFuelType00_2Valid = 0;
        $scope.setvtsFuelType01_0Valid = 0; $scope.setvtsFuelType01_1Valid = 0; $scope.setvtsFuelType01_2Valid = 0;
        $scope.setvtsFuelType02_0Valid = 0; $scope.setvtsFuelType02_1Valid = 0; $scope.setvtsFuelType02_2Valid = 0;
        $scope.setvtsFuelType03_0Valid = 0; $scope.setvtsFuelType03_1Valid = 0; $scope.setvtsFuelType03_2Valid = 0;
        $scope.setvtsFuelType04_0Valid = 0; $scope.setvtsFuelType04_1Valid = 0; $scope.setvtsFuelType04_2Valid = 0;
        $scope.setvtsFuelType05_0Valid = 0; $scope.setvtsFuelType05_1Valid = 0; $scope.setvtsFuelType05_2Valid = 0;
        $scope.setvtsFuelType06_0Valid = 0; $scope.setvtsFuelType06_1Valid = 0; $scope.setvtsFuelType06_2Valid = 0;

        //cargo information
        // $scope.setvtsCargoSSNIDValid = false; //SafeSeaNet manifest ID reference - has full manifest - should be autofilled through service at login - requires cooperation with SSN
        $scope.setvtsCargoTypeValid = false;

        //voyage information - should be autofilled through service at login
        $scope.vtsvesselposloninput = "";
        //$scope.vtsvesselposlatinput = "";
        $scope.vtsvesseltrueheadinginput = "";

        $scope.setvtsvesselPosLonValid = false;
        $scope.setvtsvesselPosLatValid = false;
        $scope.setvtsvesselTrueHeadingValid = false;

        $scope.setvtsETADateValid = false;
        $scope.setvtsETATimeValid = false;


        //allows only string of max number val 99.9 to pass
        $scope.VTSValidation999 = function(str){
            str = str.replace(/[^0-9.]/, '')
            if(str==".")str="0." //cant start with period
            if(str.length > 1) { //has to start with zero if period is first char
                if (str.substring(0,1) == ".") str = "0" + str;
            }
            //remove all periods but the first
            if(str.length > 1) {
                for (var i = 0; i != str.length - 1; i++) {
                    if (str.substring(i, i + 1) == ".") {
                        if (i != str.length - 1) {
                            var da = str.substring(0, i);
                            var db = str.substring(i + 1, str.length).replace(".", "");
                            db = db.substring(0,1); //only one decimal place allowed
                            str = da +"."+ db;
                        }
                        break;
                    }
                }
            }
            if(str.length > 4) str = str.substring(0,4) //cant be more than 4 chars
            return str;
        }



        //test if all items check out, then display "SEND" button
        $scope.VTSValidationAllDone = function(){
             if($scope.setvtsvesselnameValid && $scope.setvtsvesselcallsignValid && $scope.setvtsvesselMMSIValid
                 && $scope.setvtsvesselIMOValid && $scope.setvtsvesselDraughtValid && $scope.setvtsvesselAirDraughtValid
                 && $scope.setvtsvesselPersonsValid)
             {
                 $scope.VTSReadyToSend = true;
             }
        };

        //length greater than 1
        $scope.VTSVesselNameValidation = function(){
            ($scope.vtsvesselnameinput.length > 1) ? $scope.setvtsvesselnameValid = true : $scope.setvtsvesselnameValid = false;
            $scope.VTSValidationAllDone(); //If all done, display send button
        };

        //length greater than 1
        $scope.VTSVesselCallsignValidation = function(){
            ($scope.vtsvesselcallsigninput.length > 1) ? $scope.setvtsvesselcallsignValid = true : $scope.setvtsvesselcallsignValid = false;
            $scope.VTSValidationAllDone(); //If all done, display send button
        };

        //Only 9 numbers, no letters
        $scope.VTSVesselMMSIValidation = function(){
            var mmsi = $scope.vtsvesselmmsiinput;

            if(mmsi.length == 9){
                $scope.setvtsvesselMMSIValid = true
            } else if(mmsi.length > 9){
                mmsi = mmsi.substring(0,9);
            }else{
                $scope.setvtsvesselMMSIValid = false;
            }
            mmsi = mmsi.replace(/\D/g, '');
            $scope.vtsvesselmmsiinput = mmsi;
            $scope.vtsvesselmmsilabel = $sce.trustAsHtml("MMSI: "+mmsi); //send to label
            $scope.VTSValidationAllDone(); //If all done, display send button
        };


        //Only 10 chars, "IMO" (hardcoded) followed by 7 numbers.
        $scope.VTSVesselIMOValidation = function(){
            var imo = $scope.vtsvesselimoinput;
            if(imo.length == 7){
                $scope.setvtsvesselIMOValid = true
            } else if($scope.vtsvesselimoinput.length > 7){
                imo = imo.substring(0,7);
            }else{
                $scope.setvtsvesselIMOValid = false;
            }
            imo = imo.replace(/\D/g, '');
            $scope.vtsvesselimoinput = imo; //send cleaned to input field
            $scope.vtsvesselimolabel = $sce.trustAsHtml("IMO"+imo); //send to label
            $scope.VTSValidationAllDone(); //If all done, display send button
        };

        //max 99.9
        $scope.VTSVesselDraughtValidation = function() {
            var draught = $scope.VTSValidation999($scope.vtsvesseldraughtinput);
            var draughtFloat = parseFloat(draught);
            if(draughtFloat > 0 && draughtFloat < 100) {
                $scope.setvtsvesselDraughtValid = true
            }else{
                $scope.setvtsvesselDraughtValid = false;
            }
            $scope.vtsvesseldraughtinput = draught;
            $scope.VTSValidationAllDone(); //If all done, display send button
        }

        //max 99.9
        $scope.VTSVesselAirDraughtValidation = function() {
            var draught = $scope.VTSValidation999($scope.vtsvesselairdraughtinput);
            var draughtFloat = parseFloat(draught);
            if(draughtFloat > 0 && draughtFloat < 100) {
                $scope.setvtsvesselAirDraughtValid = true
            }else{
                $scope.setvtsvesselAirDraughtValid = false;
            }
            $scope.vtsvesselairdraughtinput = draught;
            $scope.VTSValidationAllDone(); //If all done, display send button
        }

        //minimum 1 person on board
        $scope.VTSVesselPersonsValidation = function(){
            var persons = $scope.vtsvesselpersonsinput;
            persons = persons.replace(/\D/g, '');
            if(persons.length>5) persons = persons.substring(0,5);
            if(parseInt(persons) > 0){
                $scope.setvtsvesselPersonsValid = true
            } else {
                $scope.setvtsvesselPersonsValid = false
            }
            $scope.vtsvesselpersonsinput = persons; //send cleaned to input field
            $scope.VTSValidationAllDone(); //If all done, display send button
        };

        // //No validation for now, just pops in the GPS coords into fields if can
        // $scope.VTSVesselPositionValidation = function(){
        //
        //     // !! - for some reason this code can run through without actually updating the fields and button must be pressed again. (try to press, delete values, press again)
        //
        //     function getLocationx() {
        //         if (navigator.geolocation) {
        //             navigator.geolocation.getCurrentPosition(showPosition);
        //         } else {
        //             // console.log("Geolocation is not supported by this browser.");
        //         }
        //     }
        //     function showPosition(position) {
        //         $scope.vtsvesselposloninput = position.coords.longitude;
        //         $scope.vtsvesselposlatinput = position.coords.latitude;
        //     }
        //     getLocation();
        // }


        $scope.geolocFail = function(){
            growl.error('Unable to retrieve current position, perhaps your browser is blocking this feature.');
            $scope.getGPSCoordsBtnDisabled = false;
        }
        $scope.getLocation = function(){
            $scope.getGPSCoordsBtnDisabled = true;
            if (navigator.geolocation) {
                var location_timeout = setTimeout("geolocFail()", 5000);

                navigator.geolocation.getCurrentPosition(function (position) {
                    clearTimeout(location_timeout);
                    $scope.getGPSCoordsBtnDisabled = false;


                    $scope.retMinutesFromDecDegrees = function(decDeg){
                        var deg = (decDeg+"").substring(0,(decDeg+"").indexOf("."));
                        deg = Math.round((60*(decDeg-deg))*10000)/10000
                        return deg
                    }

                    var lon = position.coords.longitude;
                    $scope.vtsvesselposlondegreesinput = (lon+"").substring(0,(lon+"").indexOf("."))
                    $scope.vtsvesselposlonminutesinput = $scope.retMinutesFromDecDegrees(lon);

                    // var lon = position.coords.longitude;
                    // var deg = (lon+"").substring(0,(lon+"").indexOf("."));
                    // $scope.vtsvesselposlondegreesinput = deg; //update the degrees
                    // $scope.vtsvesselposlonminutesinput = Math.round((60*(lon-deg))*10000)/10000; //update the minutes
                    // $scope.vtsvesselposlondegreeslabel = $sce.trustAsHtml(deg+"&deg;"+" "+(60*(lon-deg)))

                    var lat = position.coords.latitude;
                    $scope.vtsvesselposlatdegreesinput = (lat+"").substring(0,(lat+"").indexOf("."))
                    $scope.vtsvesselposlatminutesinput = $scope.retMinutesFromDecDegrees(lat);

                    //$scope.vtsvesselposlatinput = position.coords.latitude;
                }, function (error) {
                    clearTimeout(location_timeout);
                    $scope.geolocFail();
                });
            } else {
                // Fallback for no geolocation
                $scope.geolocFail();
            }
        }



        $scope.selectVesselTypeChange = function (selectedItem) {
            $scope.setvtsVesselTypeValid = true;
        }

        //fuel types - number 999999999, no decimal. One filled makes all other neutral because only one field is mandatory
        $scope.VTSFuelTypeValidation = function(field){
            var strValue = "";
            var totFuel = 0;
            var FTC = $scope.fuelTypes.length + 1; //Fuel Type Count

            if(field) {
                strValue = document.querySelector('#vtsparentdiv #fueltype' + field).value.replace(/\D/g, '');
                if (strValue != "") {
                    if (parseInt(strValue).isNaN) {
                        strValue = "";
                    } //validate to NaN
                }
                angular.element(document.querySelector('#vtsparentdiv  #fueltype' + field)).val(strValue).$apply;
            }

            //get totFuel - cycle through all boxes and add the fuel.
            for(var i = 0;i!=FTC;i++){
                for(var j=0;j!=3;j++) {
                    try {
                        var val = document.querySelector('#vtsparentdiv #fueltype0'+i+"_"+j).value.replace(/\D/g, '');
                        if(val.isNaN || val===""){}else{totFuel += parseInt(val);}
                    }catch(exceptionNoElements){}
                }
            }
            console.log("totFuel:",totFuel)

            //set/reset valid colours - must be more than 1 ton of fuel on board.
            if(totFuel>0) {
                for (var i = 0; i != FTC; i++) {
                    for (var j = 0; j != 3; j++) {
                        try {
                            angular.element(document.querySelector('#fueltype0' + i + "_" + j)).removeClass("vtsinvalid");
                            angular.element(document.querySelector('#fueltype0' + i + "_" + j)).addClass('vtsvalid');
                        }catch(exceptionNoElements){}
                    }
                }
            }else{
                for (var i = 0; i != FTC; i++) {
                    for (var j = 0; j != 3; j++) {
                        try{
                            angular.element(document.querySelector('#fueltype0'+i+"_"+j)).removeClass("vtsvalid");
                            angular.element(document.querySelector('#fueltype0'+i+"_"+j)).addClass('vtsinvalid');
                        }catch(exceptionNoElements){}
                    }
                }
            }

            //display total tonnage of fuel
            $scope.vtsTotalFuel = parseInt(totFuel).toLocaleString().replace(/,/g, '.'); // totfuel kilo/mega/giga seperator to period from comma: 9.999.999,999

        }

        //Can be used to reference total cargo using SafeSeaNet cargo entry, by newest date, using IMO number
        // $scope.selectVesselSSNIDChange = function () {
        //     if($scope.vtsvesselcargossnidinput != ""){
        //         $scope.setvtsCargoSSNIDValid = true;
        //         $scope.showCargoType = false;
        //     }
        // }

        $scope.selectVesselCargoChange = function (selectedItem) {
            if(selectedItem!="Bulk - grain" && selectedItem != "Ballast" && selectedItem != "Passenger" && selectedItem != "Bulk - other than grain"
                && selectedItem != "Reefer" && selectedItem != "Container/Trailer" && selectedItem != "General Cargo") {
                $scope.showCargoTypeFields = true;
                $scope.showCargoTypesCheckbox = true;
                $scope.vtsDangCargoCheck = true;
                $scope.vtsDangCargoCheckDisabled = true; //cannot turn off
            }else{
                $scope.showCargoTypeFields = false;
                $scope.showCargoTypesCheckbox = true;
                $scope.vtsDangCargoCheck = false;
                $scope.vtsDangCargoCheckDisabled = false;
            }
            $scope.setvtsCargoTypeValid = true;
        }

        $scope.VTSDangerousCargoCheckbox = function(check){
            (check) ? $scope.showCargoTypeFields = true : $scope.showCargoTypeFields = false;
        }

        $scope.VTSDangerousCargoTypeValidation = function (selectedItem) {

            if(selectedItem == 1) {
                var input = $scope.vtsdangerouscargotype01input.replace(/\D/g, '');
                $scope.vtsdangerouscargotype01input = input;
            }
            if(selectedItem == 2) {
                var input = $scope.vtsdangerouscargotype02input.replace(/\D/g, '');
                $scope.vtsdangerouscargotype02input = input;
            }
            if(selectedItem == 3) {
                var input = $scope.vtsdangerouscargotype03input.replace(/\D/g, '');
                $scope.vtsdangerouscargotype03input = input;
            }
            if(selectedItem == 4) {
                var input = $scope.vtsdangerouscargotype04input.replace(/\D/g, '');
                $scope.vtsdangerouscargotype04input = input;
            }
            if(selectedItem == 5) {
                var input = $scope.vtsdangerouscargotype05input.replace(/\D/g, '');
                $scope.vtsdangerouscargotype05input = input;
            }
            if(selectedItem == 6) {
                var input = $scope.vtsdangerouscargotype06input.replace(/\D/g, '');
                $scope.vtsdangerouscargotype06input = input;
            }
            if(selectedItem == 7) {
                var input = $scope.vtsdangerouscargotype07input.replace(/\D/g, '');
                $scope.vtsdangerouscargotype07input = input;
            }
            if(selectedItem == 8) {
                var input = $scope.vtsdangerouscargotype08input.replace(/\D/g, '');
                $scope.vtsdangerouscargotype08input = input;
            }
            if(selectedItem == 9) {
                var input = $scope.vtsdangerouscargotype09input.replace(/\D/g, '');
                $scope.vtsdangerouscargotype09input = input;
            }
            if(selectedItem == 10) {
                var input = $scope.vtsdangerouscargotype10input.replace(/\D/g, '');
                $scope.vtsdangerouscargotype10input = input;
            }
        }



    //END input validation area








    //clicking on dropdown menu to open the VTS form reates the input html according to VTSCenterData available - made as html insert because so many unknowns.
    $scope.selectVTSCenterChange = function (selectedItem) {
        var html = "",
            vtsID=0;

        for(var i=0;i!=VTSData.length;i++){
            if(selectedItem == VTSData[i].shortname){
                vtsID = VTSData[i].id;
            }
        }
        $scope.VTSSelectedTrafficCenterName = VTSData[vtsID].name;
        if(VTSData[vtsID].iconImage!=""){
            html = "<span style='min-width:24px;max-width:24px;display: inline-block; text-align: left; '><img style='width:20px;height:20px;' src='"+VTSData[vtsID].iconImage+"'></span>";
            $scope.VTSSelectedTrafficCenterLogo = $sce.trustAsHtml(html);
            html = "";
        }
        if(VTSData[vtsID].VTSGuideLink!=""){
            html = "<span style='min-width:200px;max-width:100%;display: inline-block; text-align: right; right:0;float:right;'><a target='_blank' href='"+VTSData[vtsID].VTSGuideLink+"'>View Master&#39;s Guide online</a></span>";
            $scope.VTSSelectedTrafficCenterGuide = $sce.trustAsHtml(html);
            html = "";
        }



        //create contact information for VTS center in cmpact form
        html += "<span style='min-width:220px;max-width:220px;display: inline-block; text-align: left;'>Call sign: &#34;"+VTSData[vtsID].callsign+"&#34;</span>";
        if(VTSData[vtsID].vhfchannel1!="") html += "<span style='min-width:60px;max-width:60px;display: inline-block; text-align: center;'>-</span><span style='min-width:140px;max-width:140px;display: inline-block;'>VHF ch. "+VTSData[vtsID].vhfchannel1+"</span>";
        if(VTSData[vtsID].vhfchannel2!="") html += "<span style='min-width:60px;max-width:60px;display: inline-block; text-align: center;'>-</span><span style='min-width:140px;max-width:140px;display: inline-block;'>VHF ch. "+VTSData[vtsID].vhfchannel2+"</span>";

        //radio channels VHF
        //if more than 2 channels, add them to their own div - very rare that happens.
        if(VTSData[vtsID].vhfchannel3!="" || VTSData[vtsID].vhfchannel4!="") html += "<div><span style='min-width:220px;max-width:220px;display: inline-block; text-align: left;'>&nbsp;</span>";
        if(VTSData[vtsID].vhfchannel3!="") html += "<span style='min-width:60px;max-width:60px;display: inline-block; text-align: center;'>&nbsp;</span><span style='min-width:140px;max-width:140px;display: inline-block;'>VHF ch. "+VTSData[vtsID].vhfchannel3+"</span>";
        if(VTSData[vtsID].vhfchannel4!="") html += "<span style='min-width:60px;max-width:60px;display: inline-block; text-align: center;'>-</span><span style='min-width:140px;max-width:140px;display: inline-block;'>VHF ch. "+VTSData[vtsID].vhfchannel4+"</span>";
        if(VTSData[vtsID].vhfchannel3!="" || VTSData[vtsID].vhfchannel4!="") html += "</div>";

        //There is always a reserve channel or two
        if(VTSData[vtsID].vhfreservechannel1!="" || VTSData[vtsID].vhfreservechannel2!="") html += "<div><span style='min-width:220px;max-width:220px;display: inline-block; text-align: left;font-style:italic;color:#999999'>Reserve channels:</span>";
        if(VTSData[vtsID].vhfreservechannel1!="") html += "<span style='min-width:60px;max-width:60px;display: inline-block; text-align: center;'>&nbsp;</span><span style='min-width:140px;max-width:140px;display: inline-block;font-style:italic;color:#999999'>VHF "+VTSData[vtsID].vhfreservechannel1+"</span>";
        if(VTSData[vtsID].vhfreservechannel2!="") html += "<span style='min-width:60px;max-width:60px;display: inline-block; text-align: center;'>-</span><span style='min-width:140px;max-width:140px;display: inline-block;font-style:italic;color:#999999'>VHF "+VTSData[vtsID].vhfreservechannel2+"</span>";
        if(VTSData[vtsID].vhfreservechannel1!="" || VTSData[vtsID].vhfreservechannel2!="") html += "</div>";

        //Email and telephone
        if(VTSData[vtsID].email!="" || VTSData[vtsID].telephone!="" || VTSData[vtsID].telephone2!="" ) html += "<div>";
        if(VTSData[vtsID].email!="") html += "<span style='min-width:220px;max-width:200px;display: inline-block;'>Email: "+VTSData[vtsID].email+"</span>";
        if(VTSData[vtsID].telephone!="") html += "<span style='min-width:60px;max-width:60px;display: inline-block; text-align: center;'>-</span><span style='min-width:220px;max-width:220px;display: inline-block;'>Phone: "+VTSData[vtsID].telephone+"</span>";
        if(VTSData[vtsID].telephone2!="") html += "<span style='min-width:60px;max-width:60px;display: inline-block; text-align: center;'>-</span><span style='min-width:220px;max-width:220px;display: inline-block;'>Phone2: "+VTSData[vtsID].telephone2+"</span>";
        if(VTSData[vtsID].email!="" || VTSData[vtsID].telephone!="" || VTSData[vtsID].telephone2!="") html += "</div>";

        //Not all VTS have a fax
        if(VTSData[vtsID].fax!="") html += "<div><span style='min-width:220px;max-width:220px;display: inline-block;'>Fax: "+VTSData[vtsID].fax+"</span></div>";

        //displays the form fields
        $scope.VTSSelected = true;


        $scope.VTSSelectedTrafficCenterData = $sce.trustAsHtml(html);
        $scope.VTSSelectedTrafficCenterShortname = $sce.trustAsHtml(VTSData[vtsID].shortname);

        //field logic - some fields are not required by certain VTS

        $scope.showMaxDraught = VTSData[vtsID].showMaxDraught;
        $scope.showAirDraught = VTSData[vtsID].showAirDraught;
        $scope.showFuelQuantity = VTSData[vtsID].showFuelQuantity;
        $scope.showVesselType = VTSData[vtsID].showVesselType;
        $scope.showVesselLength = VTSData[vtsID].showVesselLength;
        $scope.showDeadWeightTonnage = VTSData[vtsID].showDeadWeightTonnage;
        $scope.showFuelDetails = VTSData[vtsID].showFuelDetails;
        $scope.showCargoType = VTSData[vtsID].showCargoType;

        //disables all fuel inputs if nothing
        $scope.VTSFuelTypeValidation();

    };

    $scope.selectedVesselType = "";
    $scope.vtsTotalFuel=""; //added up and displayed from validation

    $scope.sendVTSForm = function () {
        alert("Sending VTS form now..");
        $uibModalInstance.close();
    };

    $scope.hideVTSForm = function () {
        $uibModalInstance.close();
    };

        $scope.$on('modal.closing', function(event, reason, closed) {
            console.log('modal.closing: ' + (closed ? 'close' : 'dismiss') + '(' + reason + ')');
            var message = "You are about to leave the edit view. Uncaught reason. Are you sure?";
            switch (reason){
                // clicked outside
                case "backdrop click":
                    message = "Any changes will be lost, are you sure?";
                    break;

                // cancel button
                case "cancel":
                    message = "Any changes will be lost, are you sure?";
                    break;

                // escape key
                case "escape key press":
                    message = "Any changes will be lost, are you sure?";
                    break;
            }
            if (!confirm(message)) {
                event.preventDefault();
            }
        });



}]);
