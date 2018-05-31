package com.homeaway.foursqureplaces.sync.dtos;

import com.jvanila.core.io.DataObject;

/**
 * Created by pavan on 27/05/18.
 *
 */
public class FSPriceDTO extends DataObject {

    public static final String CLASS_NAME = FSPriceDTO.class.getName();
    private static final long serialVersionUID = 1L;

    public String CRUDOperation;

    public int tier;
    public String message;
    public String currency;

    public FSPriceDTO() {
        CRUDOperation = "U";
    }
}
