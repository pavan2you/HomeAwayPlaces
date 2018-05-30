package com.homeaway.homeawayplaces.foursquare.sync.dtos;

import com.jvanila.core.io.DataObject;

/**
 * Created by pavan on 27/05/18.
 *
 */
public class FSMetaDTO extends DataObject {

    public static final String CLASS_NAME = FSMetaDTO.class.getName();
    private static final long serialVersionUID = 1L;

    public String CRUDOperation;

    public int code;
    public String requestId;

    public FSMetaDTO() {
        CRUDOperation = "U";
    }

}
