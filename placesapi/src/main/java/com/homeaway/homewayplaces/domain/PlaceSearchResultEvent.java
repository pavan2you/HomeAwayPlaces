package com.homeaway.homewayplaces.domain;

import com.jvanila.core.collection.IGenericList;
import com.jvanila.core.io.DataObject;

/**
 * Created by pavan on 28/05/18.
 *
 * An Observable event.
 *
 * This will be fired from the source, once it fetches the results from data source be it network
 * or database.
 *
 */
public class PlaceSearchResultEvent extends DataObject {

    public static final String CLASS_NAME = PlaceSearchResultEvent.class.getName();

    public String query;
    public IGenericList<Place> placeList;

    public PlaceSearchResultEvent(String query, IGenericList<Place> placeList) {
        this.query = query;
        this.placeList = placeList;
    }
}
