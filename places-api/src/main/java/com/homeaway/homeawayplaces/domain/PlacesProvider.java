package com.homeaway.homeawayplaces.domain;

import com.homeaway.homeawayplaces.domain.sync.PlacesDAOFactory;
import com.homeaway.homeawayplaces.domain.sync.daos.EntityJsonDAO;
import com.homeaway.homeawayplaces.domain.sync.dtos.EntityJsonDTO;
import com.jvanila.core.collection.IGenericList;
import com.jvanila.core.collection.IGenericMap;
import com.jvanila.core.eventbus.IEventBus;
import com.jvanila.core.exception.VanilaException;
import com.jvanila.core.objectflavor.VanilaObject;
import com.jvanila.mobile.IMobilePlatformFactory;
import com.jvanila.mobile.MobilePlatformFactoryLocator;
import com.jvanila.mobile.job.AsyncToken;
import com.jvanila.mobile.json.JSONUtils;
import com.jvanila.mobile.location.ILatLngBounds;
import com.jvanila.mobile.location.VanilaLocation;
import com.jvanila.mobile.sync.dtos.FailureResponseDTO;

/**
 * Created by pavan on 28/05/18.
 *
 * 1. Abstraction
 *
 * An abstraction which deals Place retrieval from any Places Provider be it Foursquare or
 * Google. In simple it abstracts the data source.
 *
 * 2. Convenience
 *
 * To control the IAsyncJob life cycle of the AsyncToken returning methods, use AsyncToken.hook
 * at the consumer side.
 *
 * 3. Richness
 *
 * The class offers various ways of places retrieval.
 *
 * 4. Performance friendly / Async
 *
 * The methods are async by nature, so it wont block the UI or main thread. Implementations should
 * make sure it handles either way of notifying 1. as callback 2. as an event.
 *
 */
public abstract class PlacesProvider extends VanilaObject {

    /**
     * A carrier of possible place search variations.
     */
    public static class SearchOptions {

        public VanilaLocation nearbyLocation;
        public String nearbyFreeFormText;
        public int radius;
        public int limit;
        public ILatLngBounds withInBounds;
    }

    /**
     * Represents a search request
     */
    public static class SearchQuery {

        public String query;
        public SearchOptions options;

        public SearchQuery(String query, SearchOptions options) {
            this.query = query;
            this.options = options;
        }
    }

    /**
     * A callback to be called once search completes, provided the calling component registers
     * some callback.
     */
    public interface Callback {

        void onSearchComplete(String query, IGenericList<Place> result,
            FailureResponseDTO failureResponse);
    }

    protected PlacesContext mPlaceContext;

    protected IMobilePlatformFactory mMobilePlatformFactory;
    protected IEventBus mEventBus;

    private IGenericList<Callback> mCallbackList;

    /**
     * The built up cache, subjected to PlacesContext.cacheLimit
     */
    protected IGenericMap<String, IGenericList<Place>> mRecentSearchResultsMap;
    protected IGenericList<String> mOldestSearchPhraseList;

    /**
     * If keep quite is true, then any received responses will be ignored, this will auto turn to
     * false on next search query request.
     *
     */
    protected boolean mKeepQuite;

    /**
     * Search monitor
     */
    protected boolean mIsBusyInSearch;

    /**
     * Always maintain the most recent one to serve next, which reduces unnecessary searches.
     */
    protected SearchQuery mMostRecentAwaitingQuery;


    protected PlacesProvider(PlacesContext context) {
        mPlaceContext = context;

        mMobilePlatformFactory = MobilePlatformFactoryLocator.getMobilePlatformFactory();
        mEventBus = mMobilePlatformFactory.getEventBus();

        mCallbackList = mMobilePlatformFactory.newList();

        mOldestSearchPhraseList = mMobilePlatformFactory.newList();
        mRecentSearchResultsMap = mMobilePlatformFactory.newMap();
    }

    public void addCallback(Callback callback) {
        if (!mCallbackList.contains(callback)) {
            mCallbackList.add(callback);
        }
    }

    public void removeCallback(Callback callback) {
        mCallbackList.remove(callback);
    }

    public abstract AsyncToken<Place> fetchByIdAsync(String id);

    public abstract AsyncToken<IGenericList<Place>> fetchAllAsync();

    public abstract AsyncToken<IGenericList<Place>> fetchFavoritesAsync();

    public abstract AsyncToken<Integer> updateAsync(Place place);

    public abstract AsyncToken<Boolean> deleteAsync(Place place);

    public void setKeepQuite(boolean keepQuite) {
        mKeepQuite = keepQuite;

        /*
         * If provider is keeping quite then no need to have any watchers related to processing.
         * So reset them.
         */
        if (keepQuite) {
            mIsBusyInSearch = false;
            mMostRecentAwaitingQuery = null;
        }
    }

    public abstract void search(SearchQuery searchQuery) throws VanilaException;

    protected void searchComplete(String query, IGenericList<Place> result,
            FailureResponseDTO failureResponse) {

        if (result != null) {
            PlacesContext context = getContext();

            while (mOldestSearchPhraseList.size() >= context.cacheLimit) {
                String oldestSearchPhrase = mOldestSearchPhraseList.get(0);
                mRecentSearchResultsMap.remove(oldestSearchPhrase);
            }

            if (!mOldestSearchPhraseList.contains(query)) {
                mOldestSearchPhraseList.add(query);
                mRecentSearchResultsMap.put(query, result);
            }
        }

        /*
         * Notify callbacks
         */
        for (Callback callback : mCallbackList) {
            callback.onSearchComplete(query, result, failureResponse);
        }

        /*
         * Broadcast as an event
         */
        PlaceSearchResultEvent event = new PlaceSearchResultEvent(query, result, failureResponse);
        mEventBus.publish(event);
    }

    protected boolean isCacheHavingResults(String query) {
        return mRecentSearchResultsMap.containsKey(query);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public PlacesContext getContext() {
        return mPlaceContext;
    }

    public void updateContext(PlacesContext context) {
        if (context != null) {
            mPlaceContext = context;
        }
    }

    protected void persistContext(String entityClass, String entityUuid,
            PlacesContext placesContext) {

        PlacesDAOFactory daoFactory = mMobilePlatformFactory.getDAOFactory();
        EntityJsonDAO jsonDAO = daoFactory.getEntityJsonDAO();
        EntityJsonDTO entityJson = new EntityJsonDTO();
        entityJson.entity_class = entityClass;
        entityJson.entity_uuid = entityUuid;
        entityJson.entity_json = JSONUtils.toJson(placesContext);
        entityJson.entity_group = PlacesContext.class.getSimpleName();
        entityJson.entity = placesContext;

        jsonDAO.updateRecordAsync(entityJson);
    }

    public PlacesContext fetchPlaceContext(String entityUuid) {
        PlacesDAOFactory daoFactory = mMobilePlatformFactory.getDAOFactory();
        EntityJsonDAO jsonDAO = daoFactory.getEntityJsonDAO();
        EntityJsonDTO entityJson = jsonDAO.getRecord(entityUuid);

        PlacesContext result = null;
        try {
            result = (PlacesContext) JSONUtils.toDataObject(entityJson.entity_json,
                    entityJson.entity_class);

            if (result != null) {
                mPlaceContext = result;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
