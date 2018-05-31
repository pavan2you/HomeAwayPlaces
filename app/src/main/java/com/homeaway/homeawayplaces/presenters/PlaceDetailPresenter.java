package com.homeaway.homeawayplaces.presenters;

import com.homeaway.homeawayplaces.binders.PlaceListItemDataBinder;
import com.homeaway.homeawayplaces.domain.Place;
import com.homeaway.homeawayplaces.domain.PlaceModifiedEvent;
import com.homeaway.homeawayplaces.domain.PlacesApi;
import com.homeaway.homeawayplaces.domain.PlacesProvider;
import com.homeaway.homeawayplaces.sync.dtos.StaticMapDTO;
import com.homeaway.homeawayplaces.sync.dtos.StaticMapMarkerDTO;
import com.homeaway.homeawayplaces.sync.dtos.StaticMapRequestDTO;
import com.homeaway.homeawayplaces.sync.gateways.StaticMapGateway;
import com.homeaway.homeawayplaces.views.IPlaceDetailView;
import com.jvanila.core.eventbus.IEvent;
import com.jvanila.core.exception.VanilaException;
import com.jvanila.mobile.MobilePlatformInfo;
import com.jvanila.mobile.location.ILatLngBounds;
import com.jvanila.mobile.location.VanilaLocation;
import com.jvanila.mobile.mvp.Presenter;
import com.jvanila.mobile.sync.dtos.FailureResponseDTO;
import com.jvanila.mobile.sync.events.NetResponseProcessingCompletedEvent;
import com.jvanila.mobile.util.BinderUtil;

/**
 * Created by pavan on 27/05/18.
 *
 * A multi-language (Android, iOS, Windows) MVP / MVC / MVVM / MVI in short MVX implementation.
 *
 */
public class PlaceDetailPresenter extends Presenter<IPlaceDetailView> {

    private Place mPlace;
    private VanilaLocation mPoi;

    public PlaceDetailPresenter(IPlaceDetailView view) {
        super(view);
    }

    public void loadWith(Place place, VanilaLocation center) {
        mPlace = place;
        mPlace.showWebsite = true;
        mPoi = center;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        subscribeToEvents();
    }

    @Override
    public void onDependenciesResolved() {
        super.onDependenciesResolved();

        getView().setTitleWith(mPlace.name);
        loadModel();
    }

    private void loadModel() {
        loadStaticMap();
        bindModel();
    }

    private void loadStaticMap() {
        MobilePlatformInfo mobilePlatformInfo = mMobilePlatformFactory.getPlatformInfo();
        int width = mobilePlatformInfo.deviceWidthInPx;
        int height = mobilePlatformInfo.deviceHeightInPx >> 1;

        getView().setStaticMapHeight(height);
        getView().showProgress(true);

        fireStaticMapRequest(width, height);
    }

    private void fireStaticMapRequest(int width, int height) {
        StaticMapRequestDTO dto = new StaticMapRequestDTO();
        dto.entityName = Place.CLASS_NAME;

        //Size
        dto.width = width;
        dto.height = height;

        dto.zoomLevel = resolveZoomLevel(mPlace.distanceToInterest);

        //Markers
        String zerothCharCenter = "" + mStringUtils.getCharAt(mPoi.label, 0);
        StaticMapMarkerDTO centerMarker = newStaticMapMarker("blue", mPoi.latitude,
                mPoi.longitude, zerothCharCenter);

        String zerothCharPlace = "" + mStringUtils.getCharAt(mPlace.name, 0);
        StaticMapMarkerDTO placeMarker = newStaticMapMarker("green", mPlace.latLng.latitude,
                mPlace.latLng.longitude, zerothCharPlace);

        dto.markerList = mMobilePlatformFactory.newList();
        dto.markerList.add(centerMarker);
        dto.markerList.add(placeMarker);

        //Center
        ILatLngBounds bounds = mMobilePlatformFactory.getLocationApi().newLatLngBounds();
        bounds.include(mPoi);
        bounds.include(mPlace.latLng);
        bounds.build();
        try {
            VanilaLocation center = bounds.getCenter();
            dto.centerLatitude = center.latitude;
            dto.centerLongitude = center.longitude;
        } catch (VanilaException e) {
            e.printStackTrace();
        }

        StaticMapGateway gateway = mSyncAdapter.getSyncGateway(StaticMapGateway.CLASS_NAME);
        gateway.fireLoadStaticMapRequest(dto);
    }

    private int resolveZoomLevel(int distanceToInterest) {
        int zoomLevel = 12;

        if (distanceToInterest < 1000) {
            zoomLevel = 16;
        }
        else if (distanceToInterest <= 2000) {
            zoomLevel = 15;
        }
        else if (distanceToInterest <= 3500) {
            zoomLevel = 14;
        }
        else if (distanceToInterest <= 7000) {
            zoomLevel = 13;
        }

        return zoomLevel;
    }

    private StaticMapMarkerDTO newStaticMapMarker(String color, double lat, double lng,
            String name) {

        StaticMapMarkerDTO staticMarker = new StaticMapMarkerDTO();
        staticMarker.color = color;
        staticMarker.latitude = lat;
        staticMarker.longitude = lng;
        staticMarker.label = name;

        return staticMarker;
    }

    private void bindModel() {
        PlaceListItemDataBinder binder = (PlaceListItemDataBinder) getView().getPlaceListItemView()
                .getDataBinder();
        binder.onBind(mPlace);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////  USER INTERACTION  ///////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void onPlaceItemUrlClick(Place place) {
        getView().showBrowserView(place.websiteUrl);
    }

    public void onPlaceItemFavIconClick(Place place) {
        place.isFavorite = !place.isFavorite;

        /*
         * A platform agnostic way of model->view binding.
         */
        BinderUtil.refreshItemViews(place);

        syncChangeToDatabase(place);

        //broadcast same
        mEventBus.publish(new PlaceModifiedEvent(place));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////  DATABASE ACCESS  ///////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void syncChangeToDatabase(Place place) {
        PlacesProvider provider = PlacesApi.provider();
        if (place.isFavorite) {
            provider.updateAsync(place);
        }
        else {
            provider.deleteAsync(place);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////// ASYNC or EVENT PROCESSING //////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void onStaticMapResultSuccessEvent(StaticMapDTO staticMap) {
        getView().setStaticMap(staticMap.mapRawData);
        getView().showProgress(false);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////  EVENT LIFE CYCLE  ///////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void subscribeToEvents() {
        mEventBus.subscribe(NetResponseProcessingCompletedEvent.CLASS_NAME, this);
        mEventBus.subscribe(FailureResponseDTO.CLASS_NAME, this);
    }

    @Override
    public void onEvent(IEvent event) {
        super.onEvent(event);

        if (event.isInstanceOf(NetResponseProcessingCompletedEvent.class)) {
            NetResponseProcessingCompletedEvent nrpce = (NetResponseProcessingCompletedEvent) event;
            if (mStringUtils.areXAndYEqual(nrpce.mRequestType, Place.CLASS_NAME)) {
                onStaticMapResultSuccessEvent((StaticMapDTO) nrpce.mProcessedResultDTO);
            }
        }
    }

    private void unsubscribeToEvents() {
        mEventBus.unsubscribe(NetResponseProcessingCompletedEvent.CLASS_NAME, this);
        mEventBus.unsubscribe(FailureResponseDTO.CLASS_NAME, this);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////  VIEW WILL MOVE OUT  //////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onBackPressed() {
        if (getView() == null) {
            return;
        }
        unsubscribeToEvents();
        getView().showCallerView();
        super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        unsubscribeToEvents();

        mPlace = null;
        mPoi = null;

        super.onDestroy();
    }
}
