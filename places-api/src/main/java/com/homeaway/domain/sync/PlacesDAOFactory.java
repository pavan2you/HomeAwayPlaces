package com.homeaway.foursqureplaces.sync;

import com.homeaway.foursqureplaces.sync.daos.AssociationDAO;
import com.homeaway.foursqureplaces.sync.daos.FSCategoryDAO;
import com.homeaway.foursqureplaces.sync.daos.FSLocationDAO;
import com.homeaway.foursqureplaces.sync.daos.FSVenueDAO;
import com.jvanila.core.PlatformFactoryLocator;
import com.jvanila.core.collection.IGenericList;
import com.jvanila.core.collection.IGenericMap;
import com.jvanila.mobile.storage.DAOFactory;
import com.jvanila.mobile.storage.ISqliteAccessor;

/**
 * Created by pavan on 28/05/18.
 *
 */
public class FSPlacesDAOFactory extends DAOFactory {

    private FSVenueDAO mFSVenueDAO;
    private FSLocationDAO mFSLocationDAO;
    private FSCategoryDAO mFSCategoryDAO;
//    private EntityJsonDAO mEntityJsonDAO;
    private IGenericMap<String, AssociationDAO> mAssociationDAOMap;

    private final Object mLock = new Object();

    public FSPlacesDAOFactory() {
        mAssociationDAOMap = PlatformFactoryLocator.getPlatformFactory().newMap();
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

//    public EntityJsonDAO getEntityJsonDAO() {
//        synchronized (mLock) {
//            return getEntityJsonDAOInternal();
//        }
//    }
//
//    private EntityJsonDAO getEntityJsonDAOInternal() {
//        if (mEntityJsonDAO == null) {
//            mEntityJsonDAO = new EntityJsonDAO(mSqliteAccessor);
//        }
//        return mEntityJsonDAO;
//    }
//
    public AssociationDAO getAssociationDAO(String associationName) {
        synchronized (mLock) {
            return getAssociationDAOInternal(associationName);
        }
    }

    private AssociationDAO getAssociationDAOInternal(String lctName) {
        AssociationDAO listColumnDAO = mAssociationDAOMap.get(lctName);
        if (listColumnDAO == null) {
            listColumnDAO = new AssociationDAO(mSqliteAccessor, lctName);
            mAssociationDAOMap.put(lctName, listColumnDAO);
        }
        return listColumnDAO;
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
//            getEntityJsonDAOInternal().setSQLiteAccessor(sqliteAccessor);

//            IGenericList<AssociationDAO> associationDaos = mAssociationDAOMap.getValues();
//            if (associationDaos != null) {
//                for (AssociationDAO associationDAO : associationDaos) {
//                    associationDAO.setSQLiteAccessor(sqliteAccessor);
//                }
//            }

			mLock.notifyAll();
        }
    }

    @Override
    public boolean isDAOIntegrityOK() {
        synchronized (mLock) {
            boolean check = super.isDAOIntegrityOK() &&
                    getFSVenueDAOInternal().isSQLiteDatabaseOpen() &&
                    getFSLocationDAODAOInternal().isSQLiteDatabaseOpen() &&
                    getFSCategoryDAOInternal().isSQLiteDatabaseOpen() /*&&
                    getEntityJsonDAOInternal().isSQLiteDatabaseOpen()*/;

            IGenericList<AssociationDAO> associationDaos = mAssociationDAOMap.getValues();
            if (associationDaos != null) {

                boolean assCheck = true;
                for (AssociationDAO associationDAO : associationDaos) {
                    assCheck = associationDAO.isSQLiteDatabaseOpen();
                    if (!assCheck) {
                        break;
                    }
                }

                check &= assCheck;
            }

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
//            mEntityJsonDAO = null;

            mAssociationDAOMap.clear();

			mLock.notifyAll();
        }
    }
}
