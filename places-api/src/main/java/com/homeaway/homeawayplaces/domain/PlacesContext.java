package com.homeaway.homeawayplaces.domain;

import com.jvanila.core.io.DataObject;
import com.jvanila.mobile.location.VanilaLocation;

/**
 * Created by pavan on 28/05/18.
 *
 * 1. Convenience (It is just an util or convenience class)
 *
 * Maintains the user interest on PlacesApi. Things like user's Point Of Interest, Credentials
 * etc and also provides convenient API usage helper methods like constructing requests etc.
 *
 */
public abstract class PlacesContext extends DataObject {

    public static final String CLASS_NAME = PlacesContext.class.getName();

    /**
     * The user's point of interest
     */
    public VanilaLocation poi;

    /**
     * The fetch limit per request
     */
    public int fetchLimit;

    /**
     * The cache limit of past searches
     */
    public int cacheLimit;

    protected PlacesContext() {
    }
}
