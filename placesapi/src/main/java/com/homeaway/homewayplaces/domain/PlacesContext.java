package com.homeaway.homewayplaces.domain;

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
public abstract class PlacesContext {

    public VanilaLocation poi;
    public int fetchLimit;

    protected PlacesContext() {
    }
}
