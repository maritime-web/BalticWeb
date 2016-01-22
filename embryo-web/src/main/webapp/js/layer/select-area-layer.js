function SelectAreaLayer() {

	var PROJECTION_BACKEND = new OpenLayers.Projection("EPSG:4326");
	var PROJECTION_FRONTEND = new OpenLayers.Projection("EPSG:900913");

    this.init = function () {
    	this.zoomLevels = [8, 10];

        var that = this;

	    var noTransparency = browser.isChrome() && parseFloat(browser.chromeVersion())== 34;
        this.context = {
            transparency: function() {
        		if(noTransparency){
        		    return 1.0;	
        		}
                return that.active ? 0.8 : 0.4;
            },
            vesselSize: function() {
                return [0.5, 0.75, 1.0][that.zoomLevel];
            }
        };

        this.layers.selection = new OpenLayers.Layer.Vector("SelectArea");

        this.controls.regular = new OpenLayers.Control.DrawFeature(this.layers.selection, OpenLayers.Handler.RegularPolygon, {handlerOptions: {sides: 4}})
        this.controls.modify = new OpenLayers.Control.ModifyFeature(this.layers.selection, { mode : OpenLayers.Control.ModifyFeature.DRAG});
        this.controls.regular.handler.irregular = true;
        
//        console.log("Init finished......");
      
    };
    
    this.activateModify = function(){
    	this.activateSelectable();
    	this.activateControls();
    };
    
    this.deactivateModify = function(){
    	this.deactivateSelectable();
    	this.deactivateControls();
    };
    
    this.clearFeatures = function(){
    	this.layers.selection.removeAllFeatures();
    };
    

    this.getPolygonsBySelectionGroup = function() {
        return this.layers.selection.features;
    };
    
    this.getSquareBounds = function() {
    	
    	var squareBounds = [];
        for (var key in this.layers.selection.features) {
            var feature = this.layers.selection.features[key];
//			console.log("bounds -> " + feature.geometry.getBounds());
            var bounds = feature.geometry.getBounds();
//            console.log("before bounds -> " + bounds);
            bounds.transform(PROJECTION_FRONTEND, PROJECTION_BACKEND);
//            console.log("after bounds -> " + bounds);
			squareBounds.push(bounds);
    	}
    	
    	return squareBounds;
    };
    
    this.draw = function(selectionGroup) {
    	
//    	console.log("selectionGroup to draw -> " + selectionGroup.name);
    	
    	this.layers.selection.removeAllFeatures();
    	
    	var polygonFeature = {};
    	for(key in selectionGroup.squares) {
    		
    		var selectionSquare = selectionGroup.squares[key];
    		
//    		console.log("new draw -> " + JSON.stringify(selectionSquare));
    		
    		var bounds = new OpenLayers.Bounds(selectionSquare.left, selectionSquare.bottom, selectionSquare.right, selectionSquare.top);
    		bounds.transform(PROJECTION_BACKEND, PROJECTION_FRONTEND);
    		
    		//var frontendBounds = backendBounds.transform(projectionBackend, projectionFrontend);
//    		console.log("yeeeees -> " + JSON.stringify(bounds));
    		polygonFeature = new OpenLayers.Feature.Vector(bounds.toGeometry());
    		this.layers.selection.addFeatures([polygonFeature]);
    	}
    };
}

SelectAreaLayer.prototype = new EmbryoLayer();