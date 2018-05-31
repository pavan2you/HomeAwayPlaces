package com.homeaway.foursqureplaces.sync.dtos;

import com.jvanila.core.io.DataObject;

/**
 * Created by pavan on 27/05/18.
 *
 */
public class FSIconDTO extends DataObject {

    public static final String CLASS_NAME = FSCategoryDTO.class.getName();
    private static final long serialVersionUID = 1L;

    public String CRUDOperation;

    public String prefix;
    public String suffix;

    public FSIconDTO() {
        CRUDOperation = "U";
    }
}
