package com.homeaway.homeawayplaces.foursquare.sync.dtos;

import com.jvanila.core.collection.IGenericList;
import com.jvanila.core.io.DataObject;

/**
 * Created by pavan on 27/05/18.
 *
 */
public class FSResponseDTO extends DataObject {

    public static final String CLASS_NAME = FSResultDTO.class.getName();
    private static final long serialVersionUID = 1L;

    public String CRUDOperation;

    /*public String headerLocation;
    public String headerFullLocation;
    public String headerLocationGranularity;
    public String query;
    public int totalResults;
    public IGenericList<FSGroupDTO> groups;*/
    public IGenericList<FSVenueDTO> venues;

    public FSResponseDTO() {
        CRUDOperation = "U";
    }
}
