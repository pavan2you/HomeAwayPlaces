package com.homeaway.homeawayplaces.foursquare.sync.dtos;

import com.jvanila.core.collection.IGenericList;
import com.jvanila.core.io.DataObject;

/**
 * Created by pavan on 27/05/18.
 *
 */
public class FSItemDTO extends DataObject {

    public static final String CLASS_NAME = FSItemDTO.class.getName();
    private static final long serialVersionUID = 1L;

    public String CRUDOperation;

    public String referralId;
    public FSVenueDTO venue;
    public IGenericList<FSTipDTO> tips;

    public FSItemDTO() {
        CRUDOperation = "U";
    }
}
