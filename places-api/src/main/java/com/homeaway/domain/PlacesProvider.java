package com.homeaway.places;

import com.jvanila.core.IPlatformFactory;
import com.jvanila.core.PlatformFactoryLocator;
import com.jvanila.core.collection.IGenericList;
import com.jvanila.mobile.location.ILatLngBounds;
import com.jvanila.mobile.location.VanilaLocation;

public abstract class PlacesProvider {

    public interface Callback {

        void onSearchComplete(String query, IGenericList<Place> result);
    }

    private IPlatformFactory mPlatformFactory;
    private IGenericList<Callback> mCallbackList;

    private PlacesProvider() {
        mPlatformFactory = PlatformFactoryLocator.getPlatformFactory();
        mCallbackList = mPlatformFactory.newList();
    }

    public void addCallback(Callback callback) {
        if (!mCallbackList.contains(callback)) {
            mCallbackList.add(callback);
        }
    }

    public void removeCallback(Callback callback) {
        mCallbackList.remove(callback);
    }

    public abstract void search(String query);

    protected void searchComplete(String query, IGenericList<Place> result) {
        for (Callback callback : mCallbackList) {
            callback.onSearchComplete(query, result);
        }

        PlaceSearchResultEvent event = new PlaceSearchResultEvent(query, result);
        mPlatformFactory.getEventBus().publish(event);
    }

    public class SearchOptions {

        public VanilaLocation nearbyLocation;
        public String nearbyFreeFormText;
        public int radius;
        public int limit;
        public ILatLngBounds withInBounds;
    }
}
