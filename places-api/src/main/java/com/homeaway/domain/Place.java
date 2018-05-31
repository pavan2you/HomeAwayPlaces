package com.homeaway.places;

import com.jvanila.core.io.DataObject;
import com.jvanila.mobile.location.VanilaLocation;

public class Place extends DataObject {

    public static final String CLASS_NAME = Place.class.getName();

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

    public boolean isFavourite;
}
