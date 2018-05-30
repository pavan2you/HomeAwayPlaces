package com.homeaway.homeawayplaces.sync.dtos;

import com.jvanila.core.io.DataObject;

/**
 * Created by pavan on 28/05/18.
 *
 */
public class StaticMapMarkerDTO extends DataObject {

    public static final String CLASS_NAME = StaticMapMarkerDTO.class.getName();

    public String CRUDOperation;

    public String color;
    public double latitude;
    public double longitude;
    public String label;

    public StaticMapMarkerDTO() {
        CRUDOperation = "U";
    }

}
