angular.module('maritimeweb.app').controller('VesselTrafficServiceCtrl', ['$scope', '$uibModalInstance', '$window', '$sce',
    function ($scope, $uibModalInstance, $window, $sce) {

        //Add any new VTS centers here - call them if you miss information - be sure to triple check and ask for reserve VHF channels
        $scope.VTSCenterData = [
            {id: 0, shortname: 'BELTREP', name: 'Denmark - BELTREP - The Great Belt Vessel Traffic Service', callsign:'Great Belt Traffic', email:'vts@beltrep.org', telephone:'+45 58 37 68 68', vhfchannel1:'North 74', vhfchannel2:'South 11', vhfchannel3:'', vhfchannel4:'', vhfreservechannel1:'11', vhfreservechannel2:'',

            },
            {id: 1, shortname: 'SOUNDREP', name: 'Sweden - SOUNDREP - Sound Vessel Traffic Service', callsign:'Sound VTS', email:'contact@soundvts.org', telephone:'+46 771-630600', vhfchannel1:'North 73', vhfchannel2:'South 71', vhfchannel3:'', vhfchannel4:'', vhfreservechannel1:'68', vhfreservechannel2:'79',

            },
        ];
        var VTSData = $scope.VTSCenterData;

    //Input validation area
        $scope.vtsvesselnameinput = "";
        $scope.vtsvesselcallsigninput = "";
        $scope.vtsvesselmmsiinput = "";
        $scope.vtsvesselmmsilabel = "MMSI: ";
        $scope.vtsvesselimoinput = "";
        $scope.vtsvesselimolabel = "IMO";
        $scope.vtsvesseldraughtinput = "";
        $scope.vtsvesselairdraughtinput = "";
        $scope.vtsvesselpersonsinput = "";
        $scope.vtsvesselposloninput = "";
        $scope.vtsvesselposlatinput = "";
        $scope.vtsvesseltrueheadinginput = "";


        $scope.vtsetadateinput = "";
        $scope.vtsetatimeinput = "";

        $scope.VTSReadyToSend = false;

        $scope.setvtsvesselnameValid = false;
        $scope.setvtsvesselcallsignValid = false;
        $scope.setvtsvesselMMSIValid = false;
        $scope.setvtsvesselIMOValid = false;
        $scope.setvtsvesselDraughtValid = false;
        $scope.setvtsvesselAirDraughtValid = false;
        $scope.setvtsvesselPersonsValid = false;
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
             if($scope.setvtsvesselnameValid && $scope.setvtsvesselcallsignValid && $scope.setvtsvesselMMSIValid && $scope.setvtsvesselIMOValid && $scope.setvtsvesselDraughtValid && $scope.setvtsvesselAirDraughtValid && $scope.setvtsvesselPersonsValid){
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

        //No validation for now, just pops in the GPS coords into fields if can
        $scope.VTSVesselPositionValidation = function(){

            // !! - for some reason this code can run through without actually updating the fields and button must be pressed again. (try to press, delete values, press again)

            function getLocation() {
                if (navigator.geolocation) {
                    navigator.geolocation.getCurrentPosition(showPosition);
                } else {
                    // console.log("Geolocation is not supported by this browser.");
                }
            }
            function showPosition(position) {
                $scope.vtsvesselposloninput = position.coords.longitude;
                $scope.vtsvesselposlatinput = position.coords.latitude;
            }
            getLocation();
        }







    //END input validation area








    //clicking on dropdown menu to open the VTS form reates the input html according to VTSCenterData available
    $scope.selectVTSCenterChange = function (selectedItem) {
        var html = "",
            vtsID=0;

        for(var i=0;i!=VTSData.length;i++){
            if(selectedItem == VTSData[i].shortname){
                vtsID = VTSData[i].id;
            }
        }
        $scope.VTSSelectedTrafficCenterName = VTSData[vtsID].name;

        //create contact information for VTS center in cmpact form
        html += "<span style='min-width:180px;max-width:180px;display: inline-block; text-align: left;'>Call sign: &#34;"+VTSData[vtsID].callsign+"&#34;</span>";
        if(VTSData[vtsID].vhfchannel1!="") html += "<span style='min-width:60px;max-width:60px;display: inline-block; text-align: center;'>-</span><span style='min-width:140px;max-width:140px;display: inline-block;'>VHF channel "+VTSData[vtsID].vhfchannel1+"</span>";
        if(VTSData[vtsID].vhfchannel2!="") html += "<span style='min-width:60px;max-width:60px;display: inline-block; text-align: center;'>-</span><span style='min-width:140px;max-width:140px;display: inline-block;'>VHF channel "+VTSData[vtsID].vhfchannel2+"</span>";

        //radio channels VHF
        //if more than 2 channels, add them to their own div - very rare that happens.
        if(VTSData[vtsID].vhfchannel3!="" || VTSData[vtsID].vhfchannel4!="") html += "<div><span style='min-width:180px;max-width:180px;display: inline-block; text-align: left;'>&nbsp;</span>";
        if(VTSData[vtsID].vhfchannel3!="") html += "<span style='min-width:60px;max-width:60px;display: inline-block; text-align: center;'>&nbsp;</span><span style='min-width:140px;max-width:140px;display: inline-block;'>VHF channel "+VTSData[vtsID].vhfchannel3+"</span>";
        if(VTSData[vtsID].vhfchannel4!="") html += "<span style='min-width:60px;max-width:60px;display: inline-block; text-align: center;'>-</span><span style='min-width:140px;max-width:140px;display: inline-block;'>VHF channel "+VTSData[vtsID].vhfchannel4+"</span>";
        if(VTSData[vtsID].vhfchannel3!="" || VTSData[vtsID].vhfchannel4!="") html += "</div>";

        //There is always a reserve channel or two
        if(VTSData[vtsID].vhfreservechannel1!="" || VTSData[vtsID].vhfreservechannel2!="") html += "<div><span style='min-width:180px;max-width:180px;display: inline-block; text-align: left;font-style:italic;color:#999999'>Reserve channels:</span>";
        if(VTSData[vtsID].vhfreservechannel1!="") html += "<span style='min-width:60px;max-width:60px;display: inline-block; text-align: center;'>&nbsp;</span><span style='min-width:140px;max-width:140px;display: inline-block;font-style:italic;color:#999999'>VHF "+VTSData[vtsID].vhfreservechannel1+"</span>";
        if(VTSData[vtsID].vhfreservechannel2!="") html += "<span style='min-width:60px;max-width:60px;display: inline-block; text-align: center;'>-</span><span style='min-width:140px;max-width:140px;display: inline-block;font-style:italic;color:#999999'>VHF "+VTSData[vtsID].vhfreservechannel2+"</span>";
        if(VTSData[vtsID].vhfreservechannel1!="" || VTSData[vtsID].vhfreservechannel2!="") html += "</div>";

        //Email and telephone
        if(VTSData[vtsID].email!="" || VTSData[vtsID].telephone!="") html += "<div>";
        if(VTSData[vtsID].email!="") html += "<span style='min-width:180px;max-width:180px;display: inline-block;'>Email: "+VTSData[vtsID].email+"</span>";
        if(VTSData[vtsID].telephone!="") html += "<span style='min-width:60px;max-width:60px;display: inline-block; text-align: center;'>-</span><span style='min-width:200px;max-width:200px;display: inline-block;'>Telephone: "+VTSData[vtsID].telephone+"</span>";
        if(VTSData[vtsID].email!="" || VTSData[vtsID].telephone!="") html += "</div>";

        $scope.VTSSelected = true;


        $scope.VTSSelectedTrafficCenterData = $sce.trustAsHtml(html);
        $scope.VTSSelectedTrafficCenterShortname = $sce.trustAsHtml(VTSData[vtsID].shortname);

    };


        $scope.sendVTSForm = function () {
            alert("Sending VTS form now..");
            $uibModalInstance.close();
        };

        $scope.hideVTSForm = function () {
            $uibModalInstance.close();
        };





}]);
