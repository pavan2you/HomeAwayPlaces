package com.homeaway.homeawayplaces.sync.dtos;

import com.jvanila.core.io.DataObject;

/**
 * Created by pavan on 27/05/18.
 *
 */
public class StaticMapDTO extends DataObject {

    public static final String CLASS_NAME = StaticMapDTO.class.getName();

    private static final long serialVersionUID = 1L;

    public String CRUDOperation;

    public byte[] mapRawData;

    public StaticMapDTO() {
        CRUDOperation = "U";
    }
}
