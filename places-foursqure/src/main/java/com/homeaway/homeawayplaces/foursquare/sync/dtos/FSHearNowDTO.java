package com.homeaway.homeawayplaces.foursquare.sync.dtos;

import com.jvanila.core.io.DataObject;

/**
 * Created by pavan on 27/05/18.
 *
 */
public class FSHearNowDTO extends DataObject {

    public static final String CLASS_NAME = FSHearNowDTO.class.getName();
    private static final long serialVersionUID = 1L;

    public String CRUDOperation;

    public int count;
    public String summary;

    public FSHearNowDTO() {
        CRUDOperation = "U";
    }
}
