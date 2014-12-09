package dk.dma.embryo.vessel.job.filter;

import java.io.Serializable;

import dk.dma.embryo.vessel.json.VesselOverview;


public interface UserSelectionGroupsFilter extends Serializable {
	
	boolean isVesselInActiveUserSelectionGroups(VesselOverview vessel);

	boolean loggedOnUserHasSelectionGroups();
}