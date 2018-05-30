package com.homeaway.homeawayplaces.views;

import com.homeaway.homeawayplaces.domain.Place;
import com.jvanila.core.collection.IGenericList;
import com.jvanila.mobile.location.VanilaLocation;
import com.jvanila.mobile.mvp.IView;

/**
 * Created by pavan on 27/05/18.
 *
 */
public interface IPlaceListView extends IView {

    void setAndShowDataModel(IGenericList<Place> placeList);

    void showPlaceDetailView(Place place, VanilaLocation center);

    void showMapView(IGenericList<Place> placeList, VanilaLocation center);

    void exitApplication();

    void setAndShowNoDataLabel(String message);

    void hideNoDataLabel();

    void hideFabIcon();

    void showFabIcon();

    void setPoiLabel(String label);

    void setTitleWith(String title);

    void hideListView();

    void showInfoMessage(String message);

    void showProgress(boolean show);

    void setSearchFieldText(String text);
}
