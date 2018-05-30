package com.homeaway.homeawayplaces.droid;

import com.homeaway.homeawayplaces.domain.PlacesApi;
import com.homeaway.homeawayplaces.foursquare.FSPlaceProvider;
import com.homeaway.homeawayplaces.foursquare.sync.FSContext;
import com.homeaway.homeawayplaces.foursquare.sync.FSPlacesDAOFactory;
import com.jvanila.mobile.IMobilePlatformFactory;
import com.jvanila.mobile.location.VanilaLocation;

/**
 * Created by pavan on 28/05/18.
 *
 */
public class SetupApis {

    SetupApis() {
    }

    public void setupOnAppInit(IMobilePlatformFactory mpf) {
        setupPlacesApiToFoursquare(mpf);
    }

    private void setupPlacesApiToFoursquare(IMobilePlatformFactory mpf) {

        mpf.setDAOFactory(new FSPlacesDAOFactory());

        FSContext fsContext = new FSContext(
                "DKVQESIVYCTWRDDTXVMR52U5FEOJS025VUF0BCGA444XGLAH",
                "NWMANZ4W4AAYFBULW3224H53L5EL4A2MC05BUZHEDFHMKEYH",
                "20180527");

        VanilaLocation center = new VanilaLocation();
        center.latitude = 47.608013d;
        center.longitude = -122.335167d;
        center.label = "Seattle,+WA";

        fsContext.poi = center;

        FSPlaceProvider provider = new FSPlaceProvider(fsContext);
        PlacesApi.withProvider(provider);
        provider.subscribe();
    }

    private void setupPlacesApiToGooglePlaces(IMobilePlatformFactory mpf) {
        /*
         * PlacesApi.withProvider(new GooglePlaceProvider(fsContext));
         */
    }
}
