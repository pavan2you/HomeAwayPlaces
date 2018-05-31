package com.homeaway.homeawayplaces.foursquare.sync.dtos;

import com.jvanila.core.io.DataObject;

/**
 * Created by pavan on 27/05/18.
 *
 */
public class FSVenueHoursDTO extends DataObject {

    public static final String CLASS_NAME = FSVenueHoursDTO.class.getName();
    private static final long serialVersionUID = 1L;

    public String CRUDOperation;

    public String status;
    public boolean isOpen;
    public boolean isLocalHoliday;

    public FSVenueHoursDTO() {
        CRUDOperation = "U";
    }
}
