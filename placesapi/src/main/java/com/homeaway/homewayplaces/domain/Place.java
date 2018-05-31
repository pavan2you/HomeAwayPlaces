package com.homeaway.homewayplaces.domain;

import com.jvanila.core.io.DataObject;
import com.jvanila.core.objectflavor.IComparableObject;
import com.jvanila.mobile.location.VanilaLocation;

/**
 * Created by pavan on 28/05/18.
 *
 * 1. Lightweight
 * It is a simple value or data object, contains just state.
 *
 * 2. Portable
 * The purpose of keeping it Zero-Behavior (no methods) is to make it generatable to across all
 * solution technologies.
 *
 * 3. Loose couple (work flows / business logic)
 * The business logic or work-flows are loosely coupled from state. If Place wants to perform some
 * business logic, it could be done by lets say a PlaceModel class. Which could be a disadvantage
 * for some apps, its a trade off approach.
 *
 * 4. Infra free
 * A library (GooglePlaces / Foursquare / ...) independent place entity representation.
 *
 */
public class Place extends DataObject implements IComparableObject {

    public static final String CLASS_NAME = Place.class.getName();

    public String CRUDOperation;

    public String placeId;
    public String name;
    public String address;
    public VanilaLocation latLng;

    public String categoryId;
    public String category;

    public String websiteUrl;
    public String imageUrl;

    public int distanceToInterest;
    public float rating;

    public boolean isFavorite;

    public transient boolean showWebsite;

    public Place() {
        CRUDOperation = "U";
    }

    @Override
    public boolean isEqualsTo(IComparableObject o) {
        Place that = (Place) o;
        return placeId != null ? placeId.equals(that.placeId) : that.placeId == null;
    }

    @Override
    public int hashOfObject() {
        return placeId != null ? placeId.hashCode() : 0;
    }

    @Override
    public boolean equals(Object o) {
        return (this == o || o != null) && getClass() == o.getClass() &&
                this.isEqualsTo((IComparableObject) o);

    }

    @Override
    public int hashCode() {
        return hashOfObject();
    }
}
