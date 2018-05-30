package com.homeaway.homeawayplaces.foursquare.sync.dtos;

import com.jvanila.core.io.DataObject;

/**
 * Created by pavan on 27/05/18.
 *
 */
public class FSResultDTO extends DataObject {

    public static final String CLASS_NAME = FSResultDTO.class.getName();
    private static final long serialVersionUID = 1L;

    public String CRUDOperation;

    public FSMetaDTO meta;
    public FSResponseDTO response;

    public FSResultDTO() {
        CRUDOperation = "U";
    }

}
