package com.homeaway.foursqureplaces.sync;

import com.homeaway.homewayplaces.domain.sync.PlacesDAOFactory;
import com.homeaway.foursqureplaces.sync.daos.FSCategoryDAO;
import com.homeaway.foursqureplaces.sync.daos.FSLocationDAO;
import com.homeaway.foursqureplaces.sync.daos.FSVenueDAO;
import com.jvanila.mobile.storage.ISqliteAccessor;

/**
 * Created by pavan on 28/05/18.
 *
 * PlaceDAOFactory implementation for Foursquare specific data storing and retrieval.
 */
public class FSPlacesDAOFactory extends PlacesDAOFactory {

    private FSVenueDAO mFSVenueDAO;
    private FSLocationDAO mFSLocationDAO;
    private FSCategoryDAO mFSCategoryDAO;

    private final Object mLock = new Object();

    public FSPlacesDAOFactory() {
    }

    public FSVenueDAO getFSVenueDAO() {
        synchronized (mLock) {
            return getFSVenueDAOInternal();
        }
    }

    private FSVenueDAO getFSVenueDAOInternal() {
        if (mFSVenueDAO == null) {
            mFSVenueDAO = new FSVenueDAO(mSqliteAccessor);
        }
        return mFSVenueDAO;
    }

    public FSCategoryDAO getFSCategoryDAO() {
        synchronized (mLock) {
            return getFSCategoryDAOInternal();
        }
    }

    private FSCategoryDAO getFSCategoryDAOInternal() {
        if (mFSCategoryDAO == null) {
            mFSCategoryDAO = new FSCategoryDAO(mSqliteAccessor);
        }
        return mFSCategoryDAO;
    }

    public FSLocationDAO getFSLocationDAO() {
        synchronized (mLock) {
            return getFSLocationDAODAOInternal();
        }
    }

    private FSLocationDAO getFSLocationDAODAOInternal() {
        if (mFSLocationDAO == null) {
            mFSLocationDAO = new FSLocationDAO(mSqliteAccessor);
        }
        return mFSLocationDAO;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void onSQLiteDatabaseOpen(ISqliteAccessor sqliteAccessor) {
        super.onSQLiteDatabaseOpen(sqliteAccessor);

        synchronized (mLock) {
            getFSVenueDAOInternal().setSQLiteAccessor(sqliteAccessor);
            getFSLocationDAODAOInternal().setSQLiteAccessor(sqliteAccessor);
            getFSCategoryDAOInternal().setSQLiteAccessor(sqliteAccessor);

			mLock.notifyAll();
        }
    }

    @Override
    public boolean isDAOIntegrityOK() {
        synchronized (mLock) {
            boolean check = super.isDAOIntegrityOK() &&
                    getFSVenueDAOInternal().isSQLiteDatabaseOpen() &&
                    getFSLocationDAODAOInternal().isSQLiteDatabaseOpen() &&
                    getFSCategoryDAOInternal().isSQLiteDatabaseOpen();

			mLock.notifyAll();
            return check;
        }
    }

    @Override
    protected void nullify() {
        super.nullify();

        synchronized (mLock) {

            mFSVenueDAO = null;
            mFSLocationDAO = null;
            mFSCategoryDAO = null;

			mLock.notifyAll();
        }
    }
}
