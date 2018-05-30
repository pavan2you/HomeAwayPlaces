package com.homeaway.homeawayplaces.foursquare.sync.dtos;

import com.jvanila.core.collection.IGenericList;
import com.jvanila.core.io.DataObject;

/**
 * Created by pavan on 27/05/18.
 *
 */
public class FSVenueDTO extends DataObject {

    public static final String CLASS_NAME = FSVenueHoursDTO.class.getName();
    private static final long serialVersionUID = 1L;

    public String CRUDOperation;

    public String id;
    public String name;
    public FSLocationDTO location;
    public IGenericList<FSCategoryDTO> categories;
    public FSStatsDTO stats;
    public String url;
    public FSPriceDTO price;
    public float rating;
    public FSVenueHoursDTO hours;
    public FSHearNowDTO hearNow;

    public boolean isFavorite;

    public transient boolean showWebsite;

    public FSVenueDTO() {
        CRUDOperation = "U";
    }
}
