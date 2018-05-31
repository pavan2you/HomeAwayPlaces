package com.homeaway.foursqureplaces.sync.dtos;

import com.jvanila.core.collection.IGenericList;
import com.jvanila.core.io.DataObject;

/**
 * Created by pavan on 27/05/18.
 *
 */
public class FSVenueListDTO extends DataObject {

    public static final String CLASS_NAME = FSVenueHoursDTO.class.getName();
    private static final long serialVersionUID = 1L;

    public String CRUDOperation;

    public IGenericList<FSVenueDTO> venus;

    public FSVenueListDTO() {
        CRUDOperation = "U";
    }
}
