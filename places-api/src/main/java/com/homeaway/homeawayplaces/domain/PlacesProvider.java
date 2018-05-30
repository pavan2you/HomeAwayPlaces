package com.homeaway.homeawayplaces.domain;

import com.jvanila.core.collection.IGenericList;
import com.jvanila.core.collection.IGenericMap;
import com.jvanila.core.eventbus.IEventBus;
import com.jvanila.core.exception.VanilaException;
import com.jvanila.core.objectflavor.VanilaObject;
import com.jvanila.mobile.IMobilePlatformFactory;
import com.jvanila.mobile.MobilePlatformFactoryLocator;
import com.jvanila.mobile.job.AsyncToken;
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
     * A callback to be called once search completes, provided the calling component registers
     * some callback.
     */
    public interface Callback {

        void onSearchComplete(String query, IGenericList<Place> result,
            FailureResponseDTO failureResponse);
    }

    protected IMobilePlatformFactory mMobilePlatformFactory;
    protected IEventBus mEventBus;

    private IGenericList<Callback> mCallbackList;

    protected IGenericMap<String, IGenericList<Place>> mRecentSearchResultsMap;
    protected IGenericList<String> mOldestSearchPhraseList;

    protected PlacesProvider() {
        mMobilePlatformFactory = MobilePlatformFactoryLocator.getMobilePlatformFactory();
        mEventBus = mMobilePlatformFactory.getEventBus();

        mCallbackList = mMobilePlatformFactory.newList();

        mOldestSearchPhraseList = mMobilePlatformFactory.newList();
        mRecentSearchResultsMap = mMobilePlatformFactory.newMap();
    }

    public abstract PlacesContext getContext();

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

    public abstract void search(String query, SearchOptions options) throws VanilaException;

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
}
