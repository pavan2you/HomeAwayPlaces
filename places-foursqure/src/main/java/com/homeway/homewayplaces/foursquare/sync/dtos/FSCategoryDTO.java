package com.homeaway.foursqureplaces.sync.dtos;

import com.jvanila.core.io.DataObject;

/**
 * Created by pavan on 27/05/18.
 *
 */
public class FSCategoryDTO extends DataObject {

    public static final String CLASS_NAME = FSCategoryDTO.class.getName();
    private static final long serialVersionUID = 1L;

    public String CRUDOperation;

    public String id;
    public String name;
    public String pluralName;
    public String shortName;
    public FSIconDTO icon;
    public boolean primary;

    public FSCategoryDTO() {
        CRUDOperation = "U";
    }
}
