package com.homeaway.homeawayplaces.foursquare.sync.dtos;

import com.jvanila.core.collection.IGenericList;
import com.jvanila.core.io.DataObject;

/**
 * Created by pavan on 27/05/18.
 *
 */
public class FSGroupDTO extends DataObject {

    public static final String CLASS_NAME = FSGroupDTO.class.getName();
    private static final long serialVersionUID = 1L;

    public String CRUDOperation;

    public IGenericList<FSItemDTO> items;

    public FSGroupDTO() {
        CRUDOperation = "U";
    }
}
