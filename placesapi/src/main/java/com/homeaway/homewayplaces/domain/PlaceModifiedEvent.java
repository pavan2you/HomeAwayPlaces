package com.homeaway.homewayplaces.domain;

import com.jvanila.core.io.DataObject;

/**
 * Created by pavan on 29/05/18.
 *
 * An Observable event.
 *
 * If any component updates a Place, then the source will fire an event.
 */
public class PlaceModifiedEvent extends DataObject {

    public static final String CLASS_NAME = PlaceModifiedEvent.class.getName();

    public Place place;

    public PlaceModifiedEvent(Place place) {
        this.place = place;
    }
}
