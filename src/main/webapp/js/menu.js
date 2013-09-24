embryo.eventbus.GroupChangedEvent = function(id) {
    var event = jQuery.Event("GroupChangedEvent");
    event.groupId = id;
    return event;
};

embryo.eventbus.registerShorthand(embryo.eventbus.GroupChangedEvent, "groupChanged");

(function() {
    "use strict";
    
    var menuModule = angular.module('embryo.menu',[]);
    
    embryo.MenuCtrl = function($scope, $location, $element, $timeout) {
        
        $scope.initialLoad = true;
        
        $scope.$watch(function() {
            return $location.absUrl();
        }, function (url) {
            $(".navtabs a", $element).each(function(k, v) {
                var href = $(v).attr("href");
                if (url.indexOf(href, url.length - href.length) >= 0) {
                    $(v).parent("li").addClass("active");
                } else {
                    $(v).parent("li").removeClass("active");
                }
            });

            // hack to the fact, that this is loaded before included controllers
            if($scope.initialLoad){
                $scope.initialLoad = false;
                $timeout(function(){
                    embryo.eventbus.fireEvent(embryo.eventbus.GroupChangedEvent(url.substring(url.lastIndexOf("/")+1)));
                }, 100);
            }else{
                embryo.eventbus.fireEvent(embryo.eventbus.GroupChangedEvent(url.substring(url.lastIndexOf("/")+1)));
            }
        });
    };
})();
