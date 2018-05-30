package com.homeaway.homeawayplaces.presenters;

import com.homeaway.homeawayplaces.domain.MarkerModel;
import com.homeaway.homeawayplaces.domain.Place;
import com.homeaway.homeawayplaces.locale.Strings;
import com.homeaway.homeawayplaces.views.IMapView;
import com.jvanila.core.collection.IGenericList;
import com.jvanila.core.exception.VanilaException;
import com.jvanila.mobile.MobilePlatformInfo;
import com.jvanila.mobile.location.ILatLngBounds;
import com.jvanila.mobile.location.ILocationApi;
import com.jvanila.mobile.location.IMarker;
import com.jvanila.mobile.location.IMarkerOptions;
import com.jvanila.mobile.location.IVanilaMap;
import com.jvanila.mobile.location.IVanilaMapCallbacks;
import com.jvanila.mobile.location.VanilaLocation;
import com.jvanila.mobile.mvp.Presenter;

/**
 * Created by pavan on 27/05/18.
 *
 * A multi-language (Android, iOS, Windows) MVP / MVC / MVVM / MVI in short MVX implementation.
 *
 * Maps, in general are deeply tied up with platform / technology. Extracting commonality or
 * developing as a technology agnostic component is very challenging when compared to other domain-
 * drive views.
 *
 * I had worked earlier on similar requirements, hence developed a neat and handy wrapper for both
 * the technologies (Android, iOS).
 *
 */
public class MapPresenter extends Presenter<IMapView> implements IVanilaMapCallbacks {

    private static int DEFAULT_MAP_BOUNDS_PADDING = 100;
    private static final float DEFAULT_MAP_ZOOM_LEVEL = 13.0f;

    private ILocationApi mLocationApi;

    /**
     * Helper flags, comes handy to handle scenarios based on map loading states.
     */
    private boolean mIsMapLoading;
    private boolean mVeryFirstLoading;
    private boolean mMapAdjusted;

    /**
     * The model
     */
    private IGenericList<MarkerModel> mMarkerModelList;
    private VanilaLocation mCenter;

    /**
     * Map-friendly model representation
     */
    private boolean mWaitingToRenderModel;
    private IGenericList<VanilaLocation> mVisibleRegionLocations;
    private IGenericList<IMarker> mMarkers;

    /**
     * Camera watchers
     */
    private VanilaLocation mPendingCameraZoomUpdateLatLng;
    private boolean mCameraOperationInProgress;
    private float mCurrentZoomLevel;

    /**
     * Any map update is handle as a command named as MapTransaction, if there are several updates
     * everything will be queued and updated one by one.
     */
    private MapTransaction mCurrentTransaction;

    /**
     * A message to convey to user, if he/she clicks on Poi marker.
     */
    private String mPoiClickMessage;

    public MapPresenter(IMapView view) {
        super(view);
    }

    public void loadWith(IGenericList<Place> places, VanilaLocation center) {

        mCenter = center;
        mMarkerModelList = mMobilePlatformFactory.newList();

        mMarkerModelList.add(new MarkerModel(center));

        if (places != null && places.size() > 0) {
            for (Place place : places) {
                mMarkerModelList.add(new MarkerModel(place));
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mVisibleRegionLocations = mMobilePlatformFactory.newList();
        mMarkers = mMobilePlatformFactory.newList();
        mVeryFirstLoading = true;

        mIsMapLoading = true;
        mLocationApi = mMobilePlatformFactory.getLocationApi();

        mWaitingToRenderModel = true;
        mCurrentZoomLevel = DEFAULT_MAP_ZOOM_LEVEL;

        getView().setCallbacksListener(this);

        MobilePlatformInfo mpi = mMobilePlatformFactory.getPlatformInfo();
        DEFAULT_MAP_BOUNDS_PADDING = (int) (100 * mpi.deviceResolutionDpi);

        mPoiClickMessage = getString(Strings.map_poi_click_message);
    }

    @Override
    public void onDependenciesResolved() {
        super.onDependenciesResolved();

        if (!mIsMapLoading) {
            showMapOrInfo();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////// IVanilaMapCallback methods //////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onMapNotAvailable() {
        getView().showInfoMessage("please get google play services!");
    }

    @Override
    public void onMapAvailable() {
        mIsMapLoading = false;

        if (mCurrentTransaction != null) {
            return;
        }

        setUpMap();

        if (mWaitingToRenderModel) {
            mWaitingToRenderModel = false;

            if (!mVeryFirstLoading) {
                bindDataModel(mMarkerModelList);
            }
        }
    }

    public boolean isMapLoading() {
        return mIsMapLoading;
    }

    private void setUpMap() {
        getView().setUpMapWithType(IVanilaMap.MAP_TYPE_NORMAL);
        getView().clearMap();
        getView().animateCamera(mCenter, DEFAULT_MAP_ZOOM_LEVEL);
    }

    @Override
    public void onMapLoaded() {
        moveTo(mVisibleRegionLocations);
    }

    @Override
    public void onAnimationComplete() {
        if (mPendingCameraZoomUpdateLatLng != null) {
            getView().animateCameraToLocation(mPendingCameraZoomUpdateLatLng);
            mPendingCameraZoomUpdateLatLng = null;
        }
    }

    public void onCameraMove() {
        //NA
    }

    @Override
    public void onAnimationCancel() {
        getView().handleAnyNativeChangesDueToAnimationCancel();
    }

    @Override
    public void onCameraMoveStarted() {
        mCameraOperationInProgress = true;
    }

    @Override
    public void onCameraPositionChange(float zoom) {
        mCurrentZoomLevel = zoom;

        if (mCurrentTransaction != null) {
            mCurrentTransaction.commit();
        }
        else if (mVeryFirstLoading) {
            mVeryFirstLoading = false;
            mMapAdjusted = false;
        }

        mCameraOperationInProgress = false;
    }

    private void resetMap() {
        getView().stopAnimation();
        getView().clearMap();
        if (mMarkers == null) {
            mMarkers = mMobilePlatformFactory.newList();
        }
        else {
            mVisibleRegionLocations.clear();

            for (IMarker m : mMarkers) {
                m.remove();
            }
            mMarkers.clear();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////  BIND MODEL  /////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void showMapOrInfo() {
        if (mMarkerModelList.isEmpty()) {
            getView().showInfoMessage("No places available");
        }
        else {
            bindDataModel(mMarkerModelList);
        }
    }

    private void bindDataModel(IGenericList<MarkerModel> list) {

        if (mIsMapLoading) {
            mWaitingToRenderModel = true;
            return;
        }

        if (list == null || list.size() == 0) {
            return;
        }

        mCurrentTransaction = new MapTransaction(list);
        mCurrentTransaction.animateCamera = true;
        mMapAdjusted = false;

        for (MarkerModel model : list) {
            bindMarker(model, mCurrentTransaction);
        }

        fixMapBounds(mCurrentTransaction);
    }

    private void bindMarker(MarkerModel model, MapTransaction transaction) {

        IMarkerOptions markerOptions = model.newMarkerOptions();
        if (markerOptions == null) {
            return;
        }

        //update marker
        IMarker memberMarker = getView().addNewOrCachedMarker(model, markerOptions);
        memberMarker.setVisible(true);
        if (!mMarkers.contains(memberMarker)) {
            mMarkers.add(memberMarker);
            memberMarker.setTag(model);
        }

        if (!transaction.placeMarkers.contains(memberMarker)) {
            transaction.placeMarkers.add(memberMarker);
            transaction.visiblePlaceLocations.add(model.getLocation());
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////  FIX MAP BOUNDS  ///////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void fixMapBounds(MapTransaction transaction) {

        mVisibleRegionLocations = transaction.visiblePlaceLocations;
        if (!mMapAdjusted || transaction.animateCamera) {
            moveTo(mVisibleRegionLocations);
        }
        else {
            transaction.commit();
        }

        //notify rendering is completed
        getView().notifyModelRefreshComplete();
    }

    private void moveTo(IGenericList<VanilaLocation> modelLocations) {
        if (modelLocations == null) {
            return;
        }

        try {
            if (modelLocations.size() > 1) {
                ILatLngBounds latLngBounds = mLocationApi.newLatLngBounds();

                for (VanilaLocation latLng : modelLocations) {
                    latLngBounds.include(latLng);
                }

                latLngBounds.build();
                moveTo(latLngBounds.getCenter());
            }
            else if (modelLocations.size() == 1) {
                moveTo(modelLocations.get(0));
            }
            else {
                moveTo((VanilaLocation) null);
            }
        }
        catch (VanilaException e) {
            e.printStackTrace();
        }
    }

    private void moveTo(VanilaLocation latLng) throws VanilaException {
        if (!mMapAdjusted) {
            fixMapToVisibleModelBounds(latLng);
            mMapAdjusted = true;
        }
        else if (latLng != null) {
            getView().animateCamera(latLng, mCurrentZoomLevel);
        }
    }

    private boolean fixMapToVisibleModelBounds(VanilaLocation latLng) throws VanilaException {
        if (mMarkers == null || mMarkers.size() == 0) {
            return false;
        }

        ILatLngBounds bounds = mLocationApi.newLatLngBounds();

        int pointsCounter = 0;
        IGenericList<IMarker> markers = mMarkers;
        for (IMarker marker : markers) {
            VanilaLocation current = marker.getPosition();
            if (current == null) {
                continue;
            }
            pointsCounter++;
            bounds.include(current);
        }

        if (pointsCounter == 0) {
            return false;
        }

        bounds.build();

        if (latLng == null) {
            latLng = bounds.getCenter();
            mPendingCameraZoomUpdateLatLng = latLng;
        }

        getView().animateCameraBounds(bounds, DEFAULT_MAP_BOUNDS_PADDING);

        return true;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////  USER INTERACTION  ///////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void onMarkerClick(MarkerModel markerModel) {
        getView().showInfoWindowFor(markerModel);
    }

    public void onMarkerInfoWindowClick(MarkerModel markerModel) {
        Place place = markerModel.getPlace();
        if (isPointOfInterest(place)) {
            getView().showInfoMessage(mPoiClickMessage);
        }
        else {
            getView().showPlaceDetailView(markerModel.getPlace(), mCenter);
        }
    }

    private boolean isPointOfInterest(Place place) {
        return place.placeId == null;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////  VIEW WILL MOVE OUT  //////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onBackPressed() {
        if (getView() == null) {
            return;
        }

        resetMap();
        getView().showCallerView();
        super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        resetMap();

        mMarkerModelList = null;
        mLocationApi = null;
        mPendingCameraZoomUpdateLatLng = null;
        mCenter = null;
        mMarkers = null;
        mCurrentTransaction = null;
        mVisibleRegionLocations = null;

        super.onDestroy();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////  HELPER CLASSES  ////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Any model change which triggers change to the map, be it marker update or path update
     * handled as a MapTransaction, all transactions are processed sequentially.
     */
    private class MapTransaction {

        IGenericList<MarkerModel> list;
        IGenericList<IMarker> placeMarkers;
        IGenericList<VanilaLocation> visiblePlaceLocations;
        boolean animateCamera;
        boolean isProcessing;

        MapTransaction(IGenericList<MarkerModel> list) {
            this.list = list;
            animateCamera = false;
            placeMarkers = mMobilePlatformFactory.newList();
            visiblePlaceLocations = mMobilePlatformFactory.newList();
            isProcessing = true;
        }

        void commit() {
            isProcessing = false;
        }
    }
}
