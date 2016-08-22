angular.module('maritimeweb.route')
    .directive('rotateElement', function() {
        /*
        Directive for rotating the html compass needle when displaying COG (the actual course the vessel is sailing relative to the Earth)
        usage:  i.e.  <img rotate-element data-rotationdegree="67" src='/img/compass-needle.png' alt='needle'>
         */
    return function(scope, elem, attr) {
        elem.css({
            '-moz-transform': 'rotate(' + attr.rotationdegree +'deg)',
            '-webkit-transform': 'rotate(' + attr.rotationdegree +'deg)',
            '-o-transform': 'rotate(' + attr.rotationdegree +'deg)',
            '-ms-transform': 'rotate(' + attr.rotationdegree +'deg)',
            'transform': 'rotate(' + attr.rotationdegree +'deg)'
        });
    }
    });