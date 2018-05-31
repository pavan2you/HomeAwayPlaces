package com.homeaway.homeawayplaces.foursquare.sync.dtos;

import com.jvanila.core.collection.IGenericMap;
import com.jvanila.core.io.DataObject;

/**
 * Created by pavan on 27/05/18.
 *
 */
public class FSRequestDTO extends DataObject {

    public static final String CLASS_NAME = FSRequestDTO.class.getName();
    private static final long serialVersionUID = 1L;

    public String CRUDOperation;

    public String query;
    public IGenericMap<String, String> params;

    public FSRequestDTO() {
        CRUDOperation = "U";
    }
}
