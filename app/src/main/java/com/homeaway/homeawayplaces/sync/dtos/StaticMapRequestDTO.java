package com.homeaway.homeawayplaces.sync.dtos;

import com.jvanila.core.collection.IGenericList;
import com.jvanila.core.io.DataObject;

/**
 * Created by pavan on 27/05/18.
 *
 */
public class StaticMapRequestDTO extends DataObject {

    public static final String CLASS_NAME = StaticMapRequestDTO.class.getName();

    private static final long serialVersionUID = 1L;

    public String CRUDOperation;

    public double centerLatitude;
    public double centerLongitude;
    public int zoomLevel;
    public int width;
    public int height;
    public String entityName;
    public String locationPoints;
    public double radius;
    public IGenericList<StaticMapMarkerDTO> markerList;

    public StaticMapRequestDTO() {
        CRUDOperation = "U";
    }
}
