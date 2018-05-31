package com.homeaway.homeawayplaces.presenters;

import com.homeaway.homeawayplaces.domain.Place;
import com.homeaway.homeawayplaces.domain.PlaceModifiedEvent;
import com.homeaway.homeawayplaces.domain.PlaceSearchResultEvent;
import com.homeaway.homeawayplaces.domain.PlacesApi;
import com.homeaway.homeawayplaces.domain.PlacesContext;
import com.homeaway.homeawayplaces.domain.PlacesProvider;
import com.homeaway.homeawayplaces.locale.Strings;
import com.homeaway.homeawayplaces.views.IPlaceListView;
import com.jvanila.core.collection.IGenericList;
import com.jvanila.core.eventbus.IEvent;
import com.jvanila.core.exception.VanilaException;
import com.jvanila.mobile.job.AsyncToken;
import com.jvanila.mobile.job.IAsyncJob;
import com.jvanila.mobile.location.VanilaLocation;
import com.jvanila.mobile.mvp.Presenter;
import com.jvanila.mobile.sync.dtos.FailureResponseDTO;
import com.jvanila.mobile.sync.events.NetworkAvailabilityEvent;
import com.jvanila.mobile.util.BinderUtil;

/**
 * Created by pavan on 27/05/18.
 *
 * A multi-language (Android, iOS, Windows) MVP / MVC / MVVM / MVI in short MVX implementation.
 *
 * Any presenter more or less will be having a same life cycle and same life time with respect to
 * the associated platform specific components (IView implementers) -
 * (Activity / Fragment / ViewController / CustomClass).
 *
 * Creating of Presenter, is handled at the jVanila framework for standard components mentioned
 * above, for custom classes need to be addressed by developer.
 *
 * Memory leaks caused by strong references of platform specific components (IView implementers)
 * are addressed by enclosing them as IWeakReference<IView> wrapper. Other declarations are
 * released in onDestroy().
 *
 * As entire presenters layer is written in POJO, these classes are convertible to iOS Cocoa by
 * using tools like j2Objc.
 *
 * Presenters used in this project does many things few notably tasks are
 * 1. Dictates the view rendering,
 * 2. Interacts with database,
 * 3. Interacts with network. etc.
 *
 */
public class PlaceListPresenter extends Presenter<IPlaceListView> {

    /**
     * The Place Context. Having a way to change Poi is good to have feature.
     */
    private VanilaLocation mCurrentPointOfInterest;
    private int mFetchLimit;

    /**
     * The data model
     */
    private IGenericList<Place> mSearchedPlaceList;
    private IGenericList<Place> mFavoritePlaceList;
    private IGenericList<Place> mCurrentPlaceList;

    /**
     * Derived, Formatted title
     */
    private String mTitle;
    private String mSearchFieldHint;

    /**
     * Contextual empty data messages
     */
    private String mSearchPlacesTip;
    private String mNoFavPlacesMessage;
    private String mNoResultsMessage;
    private String mNoInternetMessage;
    private String mUnrecoverableMessage;
    private String mCurrentNoDataMessage;

    /**
     * A token cached, when the data is ready the data provider will broad cast an AsyncToken
     * event.
     */
    private AsyncToken<IGenericList<Place>> mReadFavoritesToken;

    /**
     * Search watcher
     */
    private boolean mIsSearchInProgress;
    private String mCurrentSearchPhrase;
    private String mRetriableSearchPhrase;

    private boolean mShowFavorites;
    private boolean mFetchFavorites;

    /**
     * This is to handle configuration changes in android and view will move out / move in flows
     * in iOS.
     */
    private String mLoadedSearchPhrase;

    public PlaceListPresenter(IPlaceListView view) {
        super(view);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        loadPlaceContext();

        mSearchFieldHint = getString(Strings.place_list_search_nearby);
        mSearchPlacesTip = getString(Strings.place_list_search_places);
        mNoFavPlacesMessage = getString(Strings.place_list_no_favs);
        mNoResultsMessage = getString(Strings.place_list_no_results);
        mNoInternetMessage = getString(Strings.place_list_no_internet);
        mUnrecoverableMessage = getString(Strings.place_list_unrecoverable_nw_error);

        mCurrentNoDataMessage = mSearchPlacesTip;
        mFetchFavorites = mShowFavorites = true; // by default show favourites if present

        /*
         * Let the presenter be event-aware
         */
        subscribeToEvents();
    }

    public void loadWith(String searchPhrase) {
        mLoadedSearchPhrase = searchPhrase;
    }

    /**
     * For CRUD based mobile applications, mostly they are tied up with network and database
     * When we use libraries like SQLCipher and to initialise HandlerThread, there is a delay.
     *
     * To perform any DB or SQL we need to have the necessary modules loaded. For every Activity/
     * Fragment/ViewController in iOS this check happens in onStart / viewDidLoad,
     * once the dependencies are loaded this method will be called.
     *
     * By using jVanila framework, the right place to start presentation logic is here.
     */
    @Override
    public void onDependenciesResolved() {
        super.onDependenciesResolved();

        getView().setTitleWith(mTitle);
        getView().showProgress(mIsSearchInProgress);

        /*
         * The view is ready to consume the content that is
         * 1. The view is visible state
         * 2. Dependencies like sql and network modules are ready to serve
         */
        loadModel();
    }

    private void loadPlaceContext() {
        PlacesContext context = PlacesApi.provider().getContext();
        mCurrentPointOfInterest = context.poi;
        mFetchLimit = context.fetchLimit;

        mTitle = getString(Strings.place_list_search_nearby);
    }

    private void loadModel() {

        showListOrInfo(mCurrentPlaceList);
        loadFavourites();

        if (mLoadedSearchPhrase != null) {
            getView().setSearchFieldText(mLoadedSearchPhrase);
        }
    }

    private void loadFavourites() {
        /*
         * An asynchronous db read call
         */
        mReadFavoritesToken = PlacesApi.provider().fetchFavoritesAsync();
    }

    private void showListOrInfo(IGenericList<Place> list) {
        if (list == null || list.size() == 0) {
            getView().setSearchFieldHint(mSearchFieldHint);
            getView().hideListView();
            getView().setAndShowNoDataLabel(mCurrentNoDataMessage);
            getView().hideFabIcon();
        }
        else {
            getView().hideNoDataLabel();
            getView().setAndShowDataModel(list);
            getView().showFabIcon();
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////  USER INTERACTION  ///////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void onChangePoiMenuItemClick() {
        //temporary - so leaving as hardcoded string
        getView().showInfoMessage("TODO : It will let to change Point Of Interest");
    }

    public void onUseMyLocationAsPoiMenuItemClick() {
        //temporary - so leaving as hardcoded string
        getView().showInfoMessage("TODO : Allows user location as Point of Interest");
    }

    public void onMyFavPlacesMenuItemClick() {
        getView().hideVirtualKeyboard();
        mShowFavorites = true;

        mCurrentNoDataMessage = mNoFavPlacesMessage;
        getView().setSearchFieldText("");

        if (mFavoritePlaceList == null || mFavoritePlaceList.size() == 0) {
            mFetchFavorites = true;
            getView().showProgress(true);
            loadFavourites();
        }
        else {
            showFavoritesList();
        }
    }

    public void onUseFoursquarePlacesMenuItemClick() {
        getView().showInfoMessage("TODO : trigger to use Foursquare PlacesApi");
    }

    public void onUseGooglePlacesMenuItemClick() {
        getView().showInfoMessage("TODO : trigger to use Google's PlacesApi");
    }

    public void onPointOfInterestChange(PlacesContext modifiedContext) {
        /*
         * Expecting the flow is built for the same then finally it will map to here
         */
        PlacesApi.provider().updateContext(modifiedContext);
    }

    public void onUserLocationAsPointOfInterest(VanilaLocation userLocation) {
        /*
         * Expecting the flow is built for the same then finally it will map to here
         */
        PlacesApi.provider().getContext().poi = userLocation;
        /*
         * It would be nice retrieving the geo address if userLocation doesn't have the same.
         */
    }

    public void onSwitchPlacesProvider(PlacesProvider provider) {
        /*
         * Expecting the flow is built for the same then finally it will map to here
         */
        PlacesApi.withProvider(provider);
    }

    public void onSearchPlaces(String searchPhrase) {

        mCurrentSearchPhrase = searchPhrase;
        mRetriableSearchPhrase = null;

        if (mStringUtils.isNullOrEmpty(searchPhrase)) {
            mIsSearchInProgress = false;

            //reset progress
            mCurrentPlaceList = mSearchedPlaceList = null;
            mCurrentNoDataMessage = mSearchPlacesTip;

            PlacesApi.provider().setKeepQuite(true);
            getView().showProgress(false);
            showListOrInfo(null);
        }
        else {

            mFetchFavorites = mShowFavorites = false;
            mIsSearchInProgress = true;
            getView().showProgress(true);

            PlacesProvider.SearchOptions options = new PlacesProvider.SearchOptions();
            options.nearbyFreeFormText = mCurrentPointOfInterest.label;
            options.limit = mFetchLimit;

            try {
                PlacesApi.provider().search(new PlacesProvider.SearchQuery(searchPhrase, options));
            } catch (VanilaException e) {
                e.printStackTrace();
            }
        }
    }

    public void onCancelSearchIconClick() {
        getView().setSearchFieldText("");
    }

    public void onPlaceItemClick(Place item) {
        getView().showPlaceDetailView(item, mCurrentPointOfInterest);
    }

    public void onPlaceItemFavIconClick(Place place) {
        place.isFavorite = !place.isFavorite;

        /*
         * Reflect the change in local cache
         */
        if (mFavoritePlaceList == null) {
            mFavoritePlaceList = mMobilePlatformFactory.newList();
        }
        if (place.isFavorite) {
            mFavoritePlaceList.add(place);
        }
        else {
            mFavoritePlaceList.remove(place);
        }

        /*
         * A platform agnostic way of view-model binding.
         */
        BinderUtil.refreshItemViews(place);

        syncChangeToDatabase(place);

        switchToSearchResultsIfFavoritesAreEmpty();
    }

    public void onMapIconClick() {
        /*
         * Let the map display either searched list or favorite list
         */
        getView().showMapView(mCurrentPlaceList, mCurrentPointOfInterest);
    }

    private void showFavoritesList() {
        mCurrentPlaceList = mMobilePlatformFactory.newList();
        mCurrentPlaceList.addAll(mFavoritePlaceList);
        showListOrInfo(mCurrentPlaceList);
    }

    private void switchToSearchResultsIfFavoritesAreEmpty() {
        if (mFavoritePlaceList.size() == 0 && mShowFavorites) {
            mShowFavorites = false;

            mCurrentNoDataMessage = mSearchPlacesTip;
            mCurrentPlaceList = mSearchedPlaceList;

            showListOrInfo(null);
        }
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

    private boolean isReadFavoritesToken(AsyncToken asyncToken) {
        return mReadFavoritesToken != null && mStringUtils.areXAndYEqual(asyncToken.tokenUuid,
                mReadFavoritesToken.tokenUuid);
    }

    private void onFetchingFavouritePlacesComplete(IGenericList<Place> result) {
        mFavoritePlaceList = result;
        mReadFavoritesToken = null;

        if (mFetchFavorites) {
            mCurrentNoDataMessage = mSearchedPlaceList == null ?
                    mSearchPlacesTip : mNoFavPlacesMessage;
            getView().showProgress(false);
            showFavoritesList();
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////// ASYNC or EVENT PROCESSING //////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void onPlaceSearchResultEvent(PlaceSearchResultEvent event) {

        mIsSearchInProgress = false;

        if (event.failureResponse != null) {
            onPlaceSearchFailure(event);
        }
        else {
            onPlaceSearchSuccess(event);
        }

        getView().showProgress(false);
    }

    private void onPlaceSearchSuccess(PlaceSearchResultEvent event) {
        mCurrentSearchPhrase = null;
        mCurrentPlaceList = mSearchedPlaceList = event.placeList;
        mCurrentNoDataMessage = mNoResultsMessage;

        showListOrInfo(mCurrentPlaceList);
    }

    private void onPlaceSearchFailure(PlaceSearchResultEvent event) {
        FailureResponseDTO failureEvent = event.failureResponse;

        String messageToPrompt;

        if (failureEvent.type == FailureResponseDTO.ERROR_TYPE_NO_NETWORK ||
                failureEvent.type == FailureResponseDTO.ERROR_TYPE_API_EXCEPTION) {

            String failedQuery = event.query;

            if (mStringUtils.areXAndYEqual(failedQuery, mCurrentSearchPhrase)) {
                mRetriableSearchPhrase = mCurrentSearchPhrase;
            }

            messageToPrompt = mNoInternetMessage;
        }
        else {
            mCurrentSearchPhrase = null;
            messageToPrompt = mUnrecoverableMessage;
        }

        if (mSearchedPlaceList == null) {
            mCurrentNoDataMessage = messageToPrompt;
            showListOrInfo(null);
        }
        else {
            getView().showInfoMessage(messageToPrompt);
        }
    }

    @SuppressWarnings("unchecked")
    private void onAsyncTokenJobComplete(AsyncToken asyncToken) {

        if (isReadFavoritesToken(asyncToken)) {
            onFetchingFavouritePlacesComplete(mReadFavoritesToken.result);
        }
    }

    private void cancelIfReadFavoritesJobIsWaiting() {
        if (mReadFavoritesToken != null && mReadFavoritesToken.hook != null) {
            IAsyncJob job = ((AsyncToken) mReadFavoritesToken.hook).job;
            job.cancel();
            mReadFavoritesToken = null;
        }
    }

    private void onPlaceModifiedEvent(PlaceModifiedEvent event) {

        Place modifiedPlace = event.place;

        if (mSearchedPlaceList != null && mSearchedPlaceList.contains(modifiedPlace)) {
            updateItemInList(modifiedPlace, mSearchedPlaceList,
                    mCurrentPlaceList == mSearchedPlaceList);
        }

        if (mFavoritePlaceList != null) {

            if (!modifiedPlace.isFavorite) {
                mFavoritePlaceList.remove(modifiedPlace);
            }
            else {
                mFavoritePlaceList.add(modifiedPlace);
            }

            if (mShowFavorites) {
                if (mFavoritePlaceList.size() == 0) {
                    mShowFavorites = false;

                    mCurrentNoDataMessage = mSearchPlacesTip;
                    mCurrentPlaceList = mSearchedPlaceList;

                    showListOrInfo(null);
                }
                else {
                    showFavoritesList();
                }
            }
        }
    }

    private void updateItemInList(Place modifiedPlace, IGenericList<Place> list,
            boolean refreshList) {

        //Replace
        int indexOfPlace = list.indexOf(modifiedPlace);
        Place existingPlace = list.get(indexOfPlace);

        existingPlace.isFavorite = modifiedPlace.isFavorite;
        /*
         * Refresh only cell
         */
        if (refreshList) {
            BinderUtil.refreshItemViews(modifiedPlace);
        }
    }

    private void onNetworkAvailabilityEvent(NetworkAvailabilityEvent event) {
        if (event.mReachableState == NetworkAvailabilityEvent.REACHABLE) {

            if (mRetriableSearchPhrase != null) {
                getView().showInfoMessage(getString(Strings.place_list_search_retry,
                        mRetriableSearchPhrase));

                getView().setSearchFieldText(mRetriableSearchPhrase);
            }
            else {
                mCurrentNoDataMessage = mSearchPlacesTip;
                if (mCurrentPlaceList == null || mCurrentPlaceList.size() == 0) {
                    showListOrInfo(null);
                }
            }
        }
        else {
            mCurrentNoDataMessage = mNoInternetMessage;
            if (mCurrentPlaceList == null || mCurrentPlaceList.size() == 0) {
                showListOrInfo(null);
            }
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////  EVENT LIFE CYCLE  ///////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void subscribeToEvents() {
        /*
         * To listen network changes
         */
        mEventBus.subscribe(PlaceSearchResultEvent.CLASS_NAME, this);

        /*
         * To listen database or in general any async job completions
         */
        mEventBus.subscribe(AsyncToken.CLASS_NAME, this);

        /*
         * To listen place's CUD changes
         */
        mEventBus.subscribe(PlaceModifiedEvent.CLASS_NAME, this);

        /*
         * To handle network connectivity scenarios
         */
        mEventBus.subscribe(NetworkAvailabilityEvent.CLASS_NAME, this);
    }

    @Override
    public void onEvent(IEvent event) {
        super.onEvent(event);

        if (event.isInstanceOf(PlaceSearchResultEvent.class)) {
            onPlaceSearchResultEvent((PlaceSearchResultEvent) event);
        }
        else if (event.isInstanceOf(AsyncToken.class)) {
            onAsyncTokenJobComplete((AsyncToken) event);
        }
        else if (event.isInstanceOf(PlaceModifiedEvent.class)) {
            onPlaceModifiedEvent((PlaceModifiedEvent) event);
        }
        else if (event.isInstanceOf(NetworkAvailabilityEvent.class)) {
            onNetworkAvailabilityEvent((NetworkAvailabilityEvent) event);
        }
    }

    private void unsubscribeToEvents() {
        mEventBus.unsubscribe(PlaceSearchResultEvent.CLASS_NAME, this);
        mEventBus.unsubscribe(AsyncToken.CLASS_NAME, this);
        mEventBus.unsubscribe(PlaceModifiedEvent.CLASS_NAME, this);
        mEventBus.unsubscribe(NetworkAvailabilityEvent.CLASS_NAME, this);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////  VIEW WILL MOVE OUT  ///////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onBackPressed() {
        if (getView() == null) {
            return;
        }

        cancelIfReadFavoritesJobIsWaiting();
        unsubscribeToEvents();

        getView().exitApplication();
        super.onBackPressed();
    }

    @Override
    public void onDestroy() {

        cancelIfReadFavoritesJobIsWaiting();
        unsubscribeToEvents();

        mCurrentPointOfInterest = null;

        mSearchedPlaceList = null;
        mFavoritePlaceList = null;

        mCurrentPlaceList = null;

        mTitle = null;
        mSearchPlacesTip = null;
        mNoFavPlacesMessage = null;
        mNoResultsMessage = null;
        mNoInternetMessage = null;
        mUnrecoverableMessage = null;
        mCurrentNoDataMessage = null;

        super.onDestroy();
    }
}
