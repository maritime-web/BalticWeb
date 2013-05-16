/**
 * Panel object
 * 
 * @param panelName
 *			The name of the panel. Remember capital first letter.
 * @param maxHeight
 * 			The maximum height of the panel
 * @param position
 *          Either "left" or "right"
 * @returns Panel object
 */
function Panel(panelName, handler, maxHeight, position) {
	this.panelName = panelName;
	this.maxHeight = maxHeight;
	this.position = position;
	this.handler = handler;
	this.open = false;
	this.opening = false;
	this.ready = true;
	
	this.setupListeners = function(){
		$("#" + panelName.toLowerCase() + "").click(function() {
			if (this.open){
				$("#" + panelName.toLowerCase() + "").slideUp(
					{
						complete:function(){
							$("#" + panelName.toLowerCase() + "Header").html(panelName);
							this.open = false;
							$("#" + panelName.toLowerCase() + "Panel").removeClass("arrowUp");
							$("#" + panelName.toLowerCase() + "Panel").addClass("arrowDown");
							handler.checkForOverflow();
						}
					}
				);
			} else if($("#vesselDetailsContainer").html() != ""){
				// TODO: Only if Container is empty
				$("#" + panelName.toLowerCase() + "Header").append("<hr>");
				$("#" + panelName.toLowerCase() + "Container").slideDown(
					{
						complete:function(){
							this.open = true;
							$("#" + panelName.toLowerCase() + "Panel").removeClass("arrowDown");
							$("#" + panelName.toLowerCase() + "Panel").addClass("arrowUp");
							handler.checkForOverflow();
						}
					}
				);
			}
		});	
	}
}
