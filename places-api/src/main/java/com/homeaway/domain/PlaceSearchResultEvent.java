package com.homeaway.places;

import com.jvanila.core.collection.IGenericList;
import com.jvanila.core.io.DataObject;

public class PlaceSearchResultEvent extends DataObject {

    public static final String CLASS_NAME = PlaceSearchResultEvent.class.getName();

    public String query;
    public IGenericList<Place> placeList;

    public PlaceSearchResultEvent(String query, IGenericList<Place> placeList) {
        this.query = query;
        this.placeList = placeList;
    }
}
