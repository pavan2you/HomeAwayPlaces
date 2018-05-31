package com.homeaway.homeawayplaces.foursquare.sync;

import com.homeaway.homeawayplaces.domain.PlacesContext;
import com.homeaway.homeawayplaces.domain.PlacesProvider;
import com.homeaway.homeawayplaces.foursquare.sync.dtos.FSRequestDTO;
import com.jvanila.core.INumberUtils;
import com.jvanila.core.IStringUtils;
import com.jvanila.core.PlatformFactoryLocator;
import com.jvanila.core.collection.IGenericMap;
import com.jvanila.core.exception.VanilaException;
import com.jvanila.mobile.location.VanilaLocation;

/**
 * Created by pavan on 27/05/18.
 *
 * It maintains the Foursquare related context like Foursquare specific credentials and default
 * Foursquare fetch limit etc.
 *
 */
public class FSContext extends PlacesContext {

    public static final String CLASS_NAME = FSContext.class.getName();

    private static final int DEFAULT_FETCH_LIMIT = 20;
    private static final int DEFAULT_CACHE_LIMIT = 10;

    private INumberUtils mNumberUtils;
    private IStringUtils mStringUtils;

    private String mClientId;
    private String mClientSecret;
    private String mVersion;

    public FSContext(String clientId, String clientSecret, String version) {
        mClientId = clientId;
        mClientSecret = clientSecret;
        mVersion = version;

        mNumberUtils = PlatformFactoryLocator.getPlatformFactory().getNumberUtils();
        mStringUtils = PlatformFactoryLocator.getPlatformFactory().getStringUtils();

        fetchLimit = DEFAULT_FETCH_LIMIT;
        cacheLimit = DEFAULT_CACHE_LIMIT;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////  Foursquare Request Builder  //////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public FSRequestDTO buildVenueRequest(String query, PlacesProvider.SearchOptions options)
            throws VanilaException {

        FSRequestDTO request = null;

        if (!mStringUtils.isNullOrEmpty(options.nearbyFreeFormText)) {
            request = buildVenueRequest(options.nearbyFreeFormText, query, options.limit);
        }
        else if (options.nearbyLocation != null) {
            request = buildVenueRequest(options.nearbyLocation, query, options.limit);
        }

        if (request == null) {
            throw new VanilaException("Insufficient parameters");
        }

        return request;
    }

    private FSRequestDTO buildVenueRequest(VanilaLocation nearby, String query, int limit) {
        FSRequestDTO venueRequest = new FSRequestDTO();

        IGenericMap<String, String> params = newParamsWithClientAndVersion();
        params.put("query", query);
        params.put("ll", nearby.latitude + "," + nearby.longitude);
        params.put("limit", mNumberUtils.stringifyInt(limit));

        venueRequest.params = params;

        return venueRequest;
    }

    private FSRequestDTO buildVenueRequest(String nearby, String query, int limit) {
        FSRequestDTO venueRequest = new FSRequestDTO();

        IGenericMap<String, String> params = newParamsWithClientAndVersion();
        params.put("query", query);
        params.put("near", nearby);
        params.put("limit", mNumberUtils.stringifyInt(limit));

        venueRequest.params = params;

        return venueRequest;
    }

    private IGenericMap<String, String> newParamsWithClientAndVersion() {
        IGenericMap<String, String> params = PlatformFactoryLocator.getPlatformFactory().newMap();
        params.put("client_id", mClientId);
        params.put("client_secret", mClientSecret);
        params.put("v", mVersion);
        return params;
    }
}
