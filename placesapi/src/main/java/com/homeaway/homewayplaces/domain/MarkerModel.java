package com.homeaway.homewayplaces.domain;

import com.jvanila.mobile.MobilePlatformFactoryLocator;
import com.jvanila.mobile.location.ILocationApi;
import com.jvanila.mobile.location.IMarkerOptions;
import com.jvanila.mobile.location.MarkerParams;
import com.jvanila.mobile.location.VanilaLocation;

/**
 * Created by pavan on 28/05/18.
 *
 * A convenient and an extendable model class to abstract the complexities of various marker types.
 *
 * It could be a simple stand-still marker(in this exercise) or a continuous or a period
 * of time moving objects (like in  case of Ola or Uber). Having a clear EntityMarkerModels is a
 * scalable approach here. But for this exercise it would be over solving, to just give a shot it
 * would be roughly like below.
 *
 * Roughly -- EntityMarkerModel
 *              -> (Movable + NonMovable)
 *                      -> (MovableA+MovableB..) + (NonMovableX+NonMovableY+NonMovableZ...)
 *
 *
 */
public class MarkerModel {

    private Place mPlace;
    private ILocationApi mLocationApi;

    public MarkerModel(Place place /*this exercise marker variation 1*/) {
        init(place);
    }

    public MarkerModel(VanilaLocation location /*this exercise marker variation 2*/) {
        Place place = new Place();
        place.latLng = location;
        place.name = location.label;
        init(place);
    }

    private void init(Place place) {
        mPlace = place;
        mLocationApi = MobilePlatformFactoryLocator.getMobilePlatformFactory().getLocationApi();
    }

    public Place getPlace() {
        return mPlace;
    }

    public VanilaLocation getLocation() {
        return mPlace.latLng;
    }

    public IMarkerOptions newMarkerOptions() {
        VanilaLocation location = mPlace.latLng;
        location.label = mPlace.name;
        return newStillMarkerOptions(location);
    }

    private IMarkerOptions newStillMarkerOptions(VanilaLocation location) {
        MarkerParams markerParams = new MarkerParams();
        markerParams.position = location;
        markerParams.draggable = false;
        markerParams.flat = false;
        markerParams.title = location.label;

        return mLocationApi.newMarkerOptions(markerParams);
    }

}
