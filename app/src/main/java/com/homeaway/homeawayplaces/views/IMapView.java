package com.homeaway.homeawayplaces.views;

import com.homeaway.homeawayplaces.domain.MarkerModel;
import com.homeaway.homeawayplaces.domain.Place;
import com.jvanila.mobile.location.IMarker;
import com.jvanila.mobile.location.IMarkerOptions;
import com.jvanila.mobile.location.IVanilaMap;
import com.jvanila.mobile.location.VanilaLocation;
import com.jvanila.mobile.mvp.IView;

/**
 * Created by pavan on 27/05/18.
 *
 */
public interface IMapView extends IView, IVanilaMap {

    IMarker addNewOrCachedMarker(MarkerModel model, IMarkerOptions markerOptions);

    void showInfoMessage(String message);

    void notifyModelRefreshComplete();

    void showPlaceDetailView(Place place, VanilaLocation center);

    void showInfoWindowFor(MarkerModel markerModel);

    void showCallerView();
}
