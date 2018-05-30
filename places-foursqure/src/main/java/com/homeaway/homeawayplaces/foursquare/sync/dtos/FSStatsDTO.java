package com.homeaway.homeawayplaces.foursquare.sync.dtos;

import com.jvanila.core.io.DataObject;

/**
 * Created by pavan on 27/05/18.
 *
 */
public class FSStatsDTO extends DataObject {

    public static final String CLASS_NAME = FSVenueHoursDTO.class.getName();
    private static final long serialVersionUID = 1L;

    public String CRUDOperation;

    public int checkinsCount;
    public int usersCount;
    public int tipCount;

    public FSStatsDTO() {
        CRUDOperation = "U";
    }
}
