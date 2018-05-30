package com.homeaway.homeawayplaces.domain;

/**
 * Created by pavan on 28/05/18.
 *
 * The entry point or an easy accessible component, which lets the developer not to worry the
 * complexities involved to deal with various Places Apis (Google or Foursquare etc..).
 *
 * User can configure or switch based on widget option from one PlacesProvider to other by just
 * calling PlacesApi.withProvider(...) and rest works like a magic.
 *
 * 1. Interoperability
 *
 * The greatest advantage of keeping it away from GooglePlaces or Foursquare and yet interoperable
 * with respective provider, is avoiding VENDOR LOCK. The impact of changing Places APIs should not
 * have impact or should limit to very less impact on entire solution. This is meant for the same.
 *
 */
public class PlacesApi {

    private static PlacesProvider sProvider;

    public static void withProvider(PlacesProvider provider) {
        sProvider = provider;
    }

    public static PlacesProvider provider() {
        return sProvider;
    }
}
