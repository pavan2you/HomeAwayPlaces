package com.homeaway.homeawayplaces.foursquare;

import com.homeaway.homeawayplaces.domain.Place;
import com.homeaway.homeawayplaces.domain.PlacesContext;
import com.homeaway.homeawayplaces.domain.PlacesProvider;
import com.homeaway.homeawayplaces.foursquare.sync.FSContext;
import com.homeaway.homeawayplaces.foursquare.sync.FSPlacesDAOFactory;
import com.homeaway.homeawayplaces.foursquare.sync.dtos.FSCategoryDTO;
import com.homeaway.homeawayplaces.foursquare.sync.dtos.FSRequestDTO;
import com.homeaway.homeawayplaces.foursquare.sync.dtos.FSVenueDTO;
import com.homeaway.homeawayplaces.foursquare.sync.dtos.FSVenueListDTO;
import com.homeaway.homeawayplaces.foursquare.sync.gateways.FSVenueSearchGateway;
import com.jvanila.core.IStringUtils;
import com.jvanila.core.PlatformFactoryLocator;
import com.jvanila.core.collection.IGenericList;
import com.jvanila.core.collection.IGenericMap;
import com.jvanila.core.eventbus.IEvent;
import com.jvanila.core.eventbus.IEventSubscriber;
import com.jvanila.core.exception.VanilaException;
import com.jvanila.mobile.job.AsyncToken;
import com.jvanila.mobile.job.IAsyncTokenResultCallback;
import com.jvanila.mobile.location.ILocationApi;
import com.jvanila.mobile.location.VanilaLocation;
import com.jvanila.mobile.sync.dtos.FailureResponseDTO;
import com.jvanila.mobile.sync.events.NetResponseProcessingCompletedEvent;

/**
 * Created by pavan on 28/05/18.
 *
 * Foursquare Places Provider implementation.
 *
 * It deals the all complexities involved in fetching Foursquare specific end points or from network
 * or from database via Foursquare model-specific local DataAccessObjects.
 *
 */
public class FSPlaceProvider extends PlacesProvider implements IEventSubscriber {

    private IStringUtils mStringUtils;

    private IGenericMap<String, FSVenueDTO> mVenueCache;

    private FSContext mFSContext;

    public FSPlaceProvider(FSContext fsContext) {
        mFSContext = fsContext;

        mStringUtils = mMobilePlatformFactory.getStringUtils();
        mVenueCache = mMobilePlatformFactory.newMap();
    }

    @Override
    public PlacesContext getContext() {
        return mFSContext;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////  DB CRUD PROCESSING  //////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public AsyncToken<Place> fetchByIdAsync(String id) {
        AsyncToken<Place> placeToken = new AsyncToken<Place>();

        FSPlacesDAOFactory daoFactory = mMobilePlatformFactory.getDAOFactory();
        AsyncToken<FSVenueDTO> venueToken = daoFactory.getFSVenueDAO().getRecordByColumnAsync(
                "venue_id", id);
        venueToken.hook = placeToken;
        placeToken.hook = venueToken;

        venueToken.addCallback(new ItemTokenCallback());

        return placeToken;
    }

    @Override
    public AsyncToken<IGenericList<Place>> fetchAllAsync() {
        AsyncToken<IGenericList<Place>> placeToken = new AsyncToken<IGenericList<Place>>();

        FSPlacesDAOFactory daoFactory = mMobilePlatformFactory.getDAOFactory();
        AsyncToken<IGenericList<FSVenueDTO>> venueToken = daoFactory.getFSVenueDAO()
                .getAllRecordsAsync();
        venueToken.hook = placeToken;
        placeToken.hook = venueToken;

        venueToken.addCallback(new ListTokenCallback());

        return placeToken;
    }

    @Override
    public AsyncToken<IGenericList<Place>> fetchFavoritesAsync() {
        AsyncToken<IGenericList<Place>> placeToken = new AsyncToken<IGenericList<Place>>();

        FSPlacesDAOFactory daoFactory = mMobilePlatformFactory.getDAOFactory();
        AsyncToken<IGenericList<FSVenueDTO>> venueToken = daoFactory.getFSVenueDAO()
                .getRecordListByColumnAsync("isFavorite", "1");
        venueToken.hook = placeToken;
        placeToken.hook = venueToken;

        venueToken.addCallback(new ListTokenCallback());

        return placeToken;
    }

    @Override
    public AsyncToken<Integer> updateAsync(Place place) {

        FSVenueDTO venue = mVenueCache.get(place.placeId);
        if (venue == null) {
            //this is futuristic to add any level of supported properties change
            venue = new FSVenueDTO();
            venue.id = place.placeId;
        }

        venue.isFavorite = place.isFavorite;

        //similarly we can handle any level of supported properties change

        FSPlacesDAOFactory daoFactory = mMobilePlatformFactory.getDAOFactory();
        return daoFactory.getFSVenueDAO().updateRecordAsync(venue);
    }

    @Override
    public AsyncToken<Boolean> deleteAsync(Place place) {

        AsyncToken<Boolean> venueToken = null;
        FSVenueDTO venue = mVenueCache.get(place.placeId);

        if (venue != null) {
            venue.isFavorite = place.isFavorite;

            FSPlacesDAOFactory daoFactory = mMobilePlatformFactory.getDAOFactory();
            venueToken = daoFactory.getFSVenueDAO().deleteRecordByIdAsync(venue.id);
        }

        return venueToken;
    }

    @SuppressWarnings("unchecked")
    private void notifyFetchVenueListResult(AsyncToken<IGenericList<FSVenueDTO>> token,
            IGenericList<FSVenueDTO> venueList) {

        AsyncToken<IGenericList<Place>> redirectToken =
                ((AsyncToken<IGenericList<Place>>) token.hook);

        redirectToken.setResult(toPlaceList(venueList, false));
        PlatformFactoryLocator.getPlatformFactory().getEventBus().publish(redirectToken);
    }

    @SuppressWarnings("unchecked")
    private void notifyFetchVenueResult(AsyncToken<FSVenueDTO> token, FSVenueDTO venue) {
        AsyncToken<Place> redirectToken = ((AsyncToken<Place>) token.hook);
        redirectToken.setResult(toPlaceAndCacheVenue(venue, false));
        PlatformFactoryLocator.getPlatformFactory().getEventBus().publish(redirectToken);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////// NW SEARCH PROCESSING  //////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void search(SearchQuery searchQuery) throws VanilaException {

        setKeepQuite(false);

        String query = searchQuery.query;
        SearchOptions options = searchQuery.options;

        if (isCacheHavingResults(query)) {
            searchComplete(query, mRecentSearchResultsMap.get(query), null);
        }
        else {
            if (mIsBusyInSearch) {
                mMostRecentAwaitingQuery = searchQuery;
            }
            else {

                mMostRecentAwaitingQuery = null;
                mIsBusyInSearch = true;

                FSRequestDTO request = mFSContext.buildVenueRequest(query, options);

                FSVenueSearchGateway gateway = mMobilePlatformFactory.getSyncAdapter()
                        .getSyncGateway(FSVenueSearchGateway.CLASS_NAME);
                gateway.fireReadVenueListRequest(request);
            }
        }
    }

    private void onVenueSearchResultFailureEvent(String query, FailureResponseDTO failureResponse) {
        mIsBusyInSearch = false;
        mMostRecentAwaitingQuery = null;
        searchComplete(query, null, failureResponse);
    }

    private void onVenueSearchResultSuccessEvent(String query, FSVenueListDTO venueList) {

        mIsBusyInSearch = false;

        if (mMostRecentAwaitingQuery != null) {
            try {
                search(mMostRecentAwaitingQuery);
            }
            catch (VanilaException e) {
                e.printStackTrace();
            }
        }

        IGenericList<Place> placeList = toPlaceList(venueList.venus, true);
        searchComplete(query, placeList, null);
    }

    private IGenericList<Place> toPlaceList(IGenericList<FSVenueDTO> venueList,
            boolean applyLocalState) {

        IGenericList<Place> placeList = mMobilePlatformFactory.newList();

        if (venueList != null) {
            for (FSVenueDTO venue : venueList) {
                placeList.add(toPlaceAndCacheVenue(venue, applyLocalState));
            }
        }

        return placeList;
    }

    private Place toPlaceAndCacheVenue(FSVenueDTO venue, boolean applyLocalState) {

        if (applyLocalState) {
            FSVenueDTO cachedVenue = mVenueCache.get(venue.id);
            if (cachedVenue != null) {
                venue.isFavorite = cachedVenue.isFavorite;
            }
        }

        Place place = new Place();
        place.placeId = venue.id;
        place.name = venue.name;

        place.address = venue.location.address;

        place.latLng = new VanilaLocation();
        place.latLng.latitude = venue.location.lat;
        place.latLng.longitude = venue.location.lng;

        for (FSCategoryDTO category : venue.categories) {
            if (category.primary) {
                place.categoryId = category.id;
                place.category = category.name;
                if (category.icon != null) {
                    place.imageUrl = category.icon.prefix + "32" + category.icon.suffix;
                }
            }
        }

        place.websiteUrl = venue.url;

        if (venue.location.distance == 0) {
            ILocationApi locationApi = mMobilePlatformFactory.getLocationApi();
            venue.location.distance = (int) locationApi.distanceBetween(venue.location.lat,
                    venue.location.lng, mFSContext.poi.latitude, mFSContext.poi.longitude);
        }

        place.distanceToInterest = venue.location.distance;

        place.rating = venue.rating;

        place.isFavorite = venue.isFavorite;

        mVenueCache.put(venue.id, venue);

        return place;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////  EVENT LIFE CYCLE  ///////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void subscribe() {
        mEventBus.subscribe(NetResponseProcessingCompletedEvent.CLASS_NAME, this);
        mEventBus.subscribe(FailureResponseDTO.CLASS_NAME, this);
    }

    public void unsubscribe() {
        mEventBus.subscribe(NetResponseProcessingCompletedEvent.CLASS_NAME, this);
        mEventBus.subscribe(FailureResponseDTO.CLASS_NAME, this);
    }

    @Override
    public void onEvent(IEvent event) {

        if (mKeepQuite) {
            return;
        }

        if (event.isInstanceOf(NetResponseProcessingCompletedEvent.class)) {

            NetResponseProcessingCompletedEvent nrpce = (NetResponseProcessingCompletedEvent) event;
            if (nrpce.processingStats != null) {
                nrpce.processingStats.printStats();
            }

            if (mStringUtils.areXAndYEqual(nrpce.mRequestType, FSVenueDTO.CLASS_NAME)) {
                onVenueSearchResultSuccessEvent(nrpce.mContainedObjectUniqueId,
                        (FSVenueListDTO) nrpce.mProcessedResultDTO);
            }
        }
        else if (event.isInstanceOf(FailureResponseDTO.class)) {

            FailureResponseDTO failureEvent = (FailureResponseDTO) event;
            if (failureEvent.processingStats != null) {
                failureEvent.processingStats.printStats();
            }

            if (mStringUtils.areXAndYEqual(failureEvent.RequestType, FSVenueDTO.CLASS_NAME)) {
                onVenueSearchResultFailureEvent(failureEvent.ContainedObjectUniqueId, failureEvent);
            }
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////  ASYNC DB PROCESSING CALLBACKS  /////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private class ListTokenCallback implements IAsyncTokenResultCallback<IGenericList<FSVenueDTO>> {

        @Override
        public void onTokenResult(AsyncToken<IGenericList<FSVenueDTO>> token,
                IGenericList<FSVenueDTO> venueList) {

            notifyFetchVenueListResult(token, venueList);
        }
    }

    private class ItemTokenCallback implements IAsyncTokenResultCallback<FSVenueDTO> {

        @Override
        public void onTokenResult(AsyncToken<FSVenueDTO> token, FSVenueDTO venue) {
            notifyFetchVenueResult(token, venue);
        }
    }
}
