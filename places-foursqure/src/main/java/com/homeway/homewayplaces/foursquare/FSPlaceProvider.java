package com.homeaway.foursqureplaces;

import com.homeaway.domain.Place;
import com.homeaway.domain.PlacesContext;
import com.homeaway.domain.PlacesProvider;
import com.homeaway.foursqureplaces.sync.FSContext;
import com.homeaway.foursqureplaces.sync.FSPlacesDAOFactory;
import com.homeaway.foursqureplaces.sync.dtos.FSCategoryDTO;
import com.homeaway.foursqureplaces.sync.dtos.FSRequestDTO;
import com.homeaway.foursqureplaces.sync.dtos.FSVenueDTO;
import com.homeaway.foursqureplaces.sync.dtos.FSVenueListDTO;
import com.homeaway.foursqureplaces.sync.gateways.FSVenueSearchGateway;
import com.jvanila.core.IStringUtils;
import com.jvanila.core.PlatformFactoryLocator;
import com.jvanila.core.collection.IGenericList;
import com.jvanila.core.collection.IGenericMap;
import com.jvanila.core.eventbus.IEvent;
import com.jvanila.core.eventbus.IEventSubscriber;
import com.jvanila.core.exception.VanilaException;
import com.jvanila.mobile.IMobilePlatformFactory;
import com.jvanila.mobile.MobilePlatformFactoryLocator;
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

    private IMobilePlatformFactory mMobilePlatformFactory;
    private IStringUtils mStringUtils;

    private IGenericMap<String, FSVenueDTO> mVenueCache;

    private FSContext mFSContext;

    public FSPlaceProvider(FSContext fsContext) {
        mFSContext = fsContext;
        mMobilePlatformFactory = MobilePlatformFactoryLocator.getMobilePlatformFactory();
        mStringUtils = mMobilePlatformFactory.getStringUtils();
        mVenueCache = mMobilePlatformFactory.newMap();
    }

    @Override
    public PlacesContext getContext() {
        return mFSContext;
    }

    @Override
    public AsyncToken<Place> fetchByIdAsync(String id) {
        AsyncToken<Place> placeToken = new AsyncToken<>();

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
        AsyncToken<IGenericList<Place>> placeToken = new AsyncToken<>();

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
        AsyncToken<IGenericList<Place>> placeToken = new AsyncToken<>();

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
            FSPlacesDAOFactory daoFactory = mMobilePlatformFactory.getDAOFactory();
            venueToken = daoFactory.getFSVenueDAO().deleteRecordByIdAsync(venue.id);
        }

        return venueToken;
    }

    public void subscribe() {
        mMobilePlatformFactory.getEventBus().subscribe(
                NetResponseProcessingCompletedEvent.CLASS_NAME, this);
        mMobilePlatformFactory.getEventBus().subscribe(
                FailureResponseDTO.CLASS_NAME, this);
    }

    public void unsubscribe() {
        mMobilePlatformFactory.getEventBus().subscribe(
                NetResponseProcessingCompletedEvent.CLASS_NAME, this);
        mMobilePlatformFactory.getEventBus().subscribe(
                FailureResponseDTO.CLASS_NAME, this);
    }

    @Override
    public void search(String query, SearchOptions options) throws VanilaException {
        FSVenueSearchGateway gateway = mMobilePlatformFactory.getSyncAdapter().getSyncGateway(
                FSVenueSearchGateway.CLASS_NAME);

        FSRequestDTO request = mFSContext.buildVenueRequest(query, options);
        gateway.fireReadVenueListRequest(request);
    }

    @Override
    public void onEvent(IEvent event) {
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
    }

    private void onVenueSearchResultSuccessEvent(String query, FSVenueListDTO venueList) {
        IGenericList<Place> placeList = toPlaceList(venueList.venus, true);
        searchComplete(query, placeList);
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
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private class ListTokenCallback implements IAsyncTokenResultCallback<IGenericList<FSVenueDTO>> {

        @Override
        @SuppressWarnings("unchecked")
        public void onTokenResult(AsyncToken<IGenericList<FSVenueDTO>> token,
                IGenericList<FSVenueDTO> venueList) {

            AsyncToken<IGenericList<Place>> redirectToken =
                    ((AsyncToken<IGenericList<Place>>) token.hook);

            redirectToken.setResult(toPlaceList(venueList, false));
            PlatformFactoryLocator.getPlatformFactory().getEventBus().publish(redirectToken);
        }
    }

    private class ItemTokenCallback implements IAsyncTokenResultCallback<FSVenueDTO> {

        @Override
        @SuppressWarnings("unchecked")
        public void onTokenResult(AsyncToken<FSVenueDTO> token, FSVenueDTO venue) {

            AsyncToken<Place> redirectToken = ((AsyncToken<Place>) token.hook);
            redirectToken.setResult(toPlaceAndCacheVenue(venue, false));
            PlatformFactoryLocator.getPlatformFactory().getEventBus().publish(redirectToken);
        }
    }
}
