package com.homeaway.homeawayplaces.domain.sync;

import com.homeaway.homeawayplaces.domain.sync.daos.AssociationDAO;
import com.homeaway.homeawayplaces.domain.sync.daos.EntityJsonDAO;
import com.jvanila.core.PlatformFactoryLocator;
import com.jvanila.core.collection.IGenericList;
import com.jvanila.core.collection.IGenericMap;
import com.jvanila.mobile.storage.DAOFactory;
import com.jvanila.mobile.storage.ISqliteAccessor;

/**
 * Created by pavan on 28/05/18.
 *
 * A DataAccessObject factory, supports accessing any framework independent DAOs.
 *
 */
public class PlacesDAOFactory extends DAOFactory {

    private EntityJsonDAO mEntityJsonDAO;
    private IGenericMap<String, AssociationDAO> mAssociationDAOMap;

    private final Object mLock = new Object();

    public PlacesDAOFactory() {
        mAssociationDAOMap = PlatformFactoryLocator.getPlatformFactory().newMap();
    }

    public EntityJsonDAO getEntityJsonDAO() {
        synchronized (mLock) {
            return getEntityJsonDAOInternal();
        }
    }

    private EntityJsonDAO getEntityJsonDAOInternal() {
        if (mEntityJsonDAO == null) {
            mEntityJsonDAO = new EntityJsonDAO(mSqliteAccessor);
        }
        return mEntityJsonDAO;
    }

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
            getEntityJsonDAOInternal().setSQLiteAccessor(sqliteAccessor);

            IGenericList<AssociationDAO> associationDaos = mAssociationDAOMap.getValues();
            if (associationDaos != null) {
                for (AssociationDAO associationDAO : associationDaos) {
                    associationDAO.setSQLiteAccessor(sqliteAccessor);
                }
            }

			mLock.notifyAll();
        }
    }

    @Override
    public boolean isDAOIntegrityOK() {
        synchronized (mLock) {
            boolean check = super.isDAOIntegrityOK()
                    && getEntityJsonDAOInternal().isSQLiteDatabaseOpen();

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

            mEntityJsonDAO = null;
            mAssociationDAOMap.clear();

			mLock.notifyAll();
        }
    }
}
