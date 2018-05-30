package com.homeaway.homeawayplaces.foursquare.sync.daos;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import com.homeaway.homeawayplaces.domain.sync.daos.AssociationDAO;
import com.homeaway.homeawayplaces.foursquare.sync.FSPlacesDAOFactory;
import com.homeaway.homeawayplaces.domain.sync.dtos.AssociationDTO;
import com.homeaway.homeawayplaces.foursquare.sync.dtos.FSCategoryDTO;
import com.homeaway.homeawayplaces.foursquare.sync.dtos.FSHearNowDTO;
import com.homeaway.homeawayplaces.foursquare.sync.dtos.FSPriceDTO;
import com.homeaway.homeawayplaces.foursquare.sync.dtos.FSStatsDTO;
import com.homeaway.homeawayplaces.foursquare.sync.dtos.FSVenueDTO;
import com.homeaway.homeawayplaces.foursquare.sync.dtos.FSVenueHoursDTO;
import com.jvanila.core.PlatformFactoryLocator;
import com.jvanila.core.collection.IGenericList;
import com.jvanila.droid.collections.GenericList;
import com.jvanila.droid.sync.daos.SQLiteTableInfo;
import com.jvanila.droid.sync.daos.TypedDAO;
import com.jvanila.mobile.MobilePlatformFactoryLocator;
import com.jvanila.mobile.storage.ISqliteAccessor;

/**
 * Created by pavan on 28/05/18.
 *
 * Performs CRUD on Foursquare's Venues
 *
 */
public class FSVenueDAO extends TypedDAO<FSVenueDTO> {

    private static final String VENUE_CATEGORY_ASSOCIATION = "VenueCategoryAssociation";

    private static class FSVenueDTOTableInfo extends SQLiteTableInfo<FSVenueDTO> {

        FSVenueDTOTableInfo(String name, String primaryKey, String foreignKey,
                String[] compositePrimaryKey, String[] compositeForeignKey) {

            super(name, primaryKey, foreignKey, compositePrimaryKey, compositeForeignKey);
        }

        @Override
        public String getValueOf(FSVenueDTO dto, String key) {
            if (key.equals(primaryKey)) {
                return dto.id;
            }
            return null;
        }
    }

    private FSPlacesDAOFactory mDaoFactory;
    
    public FSVenueDAO(ISqliteAccessor database) {
        super(new FSVenueDTOTableInfo("FSVenue", "venue_id", null,
                        null, null), database);

        mDaoFactory = MobilePlatformFactoryLocator.getMobilePlatformFactory().getDAOFactory();
    }

    @Override
    public int createRecord(FSVenueDTO dto) {
        if (isDebug()) {
            System.out.println("DAOThreadDebug : FSVenueDAO -> createRecord" +
                    " from " + Thread.currentThread().getName());
        }

        if (dto.CRUDOperation.equals("D")) {
            return -1;
        }

        boolean isAtLeastOneChildTransactionSuccessful = false;

        if (dto.location != null) {
            dto.location.entity_uuid = dto.id;
            isAtLeastOneChildTransactionSuccessful = mDaoFactory.getFSLocationDAO()
                    .updateRecord(dto.location) > -1;
        }

        if (dto.categories != null) {

            FSCategoryDAO categoryDAO = mDaoFactory.getFSCategoryDAO();
            AssociationDAO associationDAO = mDaoFactory.getAssociationDAO(
                    VENUE_CATEGORY_ASSOCIATION);

            for (FSCategoryDTO category : dto.categories) {
                categoryDAO.updateRecord(category);

                AssociationDTO association = new AssociationDTO();
                association.lhs_uuid = dto.id;
                association.rhs_uuid = category.id;
                isAtLeastOneChildTransactionSuccessful |=
                        associationDAO.updateRecord(association) > -1;
            }
        }

        if (!isAtLeastOneChildTransactionSuccessful) {
            System.out.println("Child transaction is failed for FSValueDTO having id = " + dto.id);
        }

        String query = "INSERT INTO FSVenue (venue_id, name, url, rating, stats_checkinsCount, " +
                "stats_usersCount, stats_tipsCount, price_tier, price_message, price_currency, " +
                "hours_status, hours_isOpen, hours_isLocalHoliday, hearNow_count, " +
                "hearNow_summary, isFavorite) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

        SQLiteStatement statement = mDatabase.compileStatement(query);

        if (dto.id != null) {
            statement.bindString(1, dto.id);
        } else {
            statement.bindNull(1);
        }
        if (dto.name != null) {
            statement.bindString(2, dto.name);
        } else {
            statement.bindNull(2);
        }
        if (dto.url != null) {
            statement.bindString(3, dto.url);
        } else {
            statement.bindNull(3);
        }

        statement.bindDouble(4, dto.rating);

        if (dto.stats != null) {
            statement.bindLong(5, dto.stats.checkinsCount);
            statement.bindLong(6, dto.stats.usersCount);
            statement.bindLong(7, dto.stats.tipCount);
        }

        if (dto.price != null) {

            statement.bindLong(8, dto.price.tier);

            if (dto.price.message != null) {
                statement.bindString(9, dto.price.message);
            }
            else {
                statement.bindNull(9);
            }
            if (dto.price.currency != null) {
                statement.bindString(10, dto.price.currency);
            }
            else {
                statement.bindNull(10);
            }
        }

        if (dto.hours != null) {
            if (dto.hours.status != null) {
                statement.bindString(11, dto.hours.status);
            }
            else {
                statement.bindNull(11);
            }

            statement.bindLong(12, dto.hours.isOpen ? 1 : 0);
            statement.bindLong(13, dto.hours.isLocalHoliday ? 1: 0);
        }

        if (dto.hearNow != null) {
            statement.bindLong(14, dto.hearNow.count);

            if (dto.hearNow.summary != null) {
                statement.bindString(15, dto.hearNow.summary);
            }
            else {
                statement.bindNull(15);
            }
        }

        statement.bindLong(16, dto.isFavorite ? 1 : 0);

        int id = -1;
        try {
            id = (int) statement.executeInsert();
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            statement.clearBindings();
            statement.close();
        }

        if (id > 0) {
            PlatformFactoryLocator.getPlatformFactory().getEventBus().publish(dto);
        }

        return id;
    }

    @Override
    protected IGenericList<TypedDAO<?>> loadDependableDaoList() {
        IGenericList<TypedDAO<?>> daos = new GenericList<>();
        daos.add(mDaoFactory.getAssociationDAO(VENUE_CATEGORY_ASSOCIATION));
        daos.add(mDaoFactory.getFSLocationDAO());
        return daos;
    }

    @Override
    public FSVenueDTO toDataObject(Cursor cursor) {
        FSVenueDTO dto = new FSVenueDTO();
        dto.id = cursor.getString(cursor.getColumnIndex("venue_id"));
        dto.name = cursor.getString(cursor.getColumnIndex("name"));
        dto.url = cursor.getString(cursor.getColumnIndex("url"));
        dto.rating = cursor.getFloat(cursor.getColumnIndex("rating"));

        int stats_checkinsCount = cursor.getInt(cursor.getColumnIndex(
                "stats_checkinsCount"));
        int stats_usersCount = cursor.getInt(cursor.getColumnIndex("stats_usersCount"));
        int stats_tipsCount = cursor.getInt(cursor.getColumnIndex("stats_tipsCount"));

        if (stats_checkinsCount != 0 || stats_usersCount != 0 || stats_tipsCount != 0) {
            dto.stats = new FSStatsDTO();
            dto.stats.checkinsCount = stats_checkinsCount;
            dto.stats.usersCount = stats_usersCount;
            dto.stats.tipCount = stats_tipsCount;
        }

        int price_tier = cursor.getInt(cursor.getColumnIndex("price_tier"));
        String price_message = cursor.getString(cursor.getColumnIndex("price_message"));
        String price_currency = cursor.getString(cursor.getColumnIndex(
                "price_currency"));

        if (price_tier != 0 || price_message != null || price_currency != null) {
            dto.price = new FSPriceDTO();
            dto.price.tier = price_tier;
            dto.price.message = price_message;
            dto.price.currency = price_currency;
        }

        String hours_status = cursor.getString(cursor.getColumnIndex("hours_status"));
        boolean hours_isOpen = cursor.getInt(cursor.getColumnIndex(
                "hours_isOpen")) == 1;
        boolean hours_isLocalHoliday = cursor.getInt(cursor.getColumnIndex(
                "hours_isLocalHoliday")) == 1;

        dto.hours = new FSVenueHoursDTO();
        dto.hours.status = hours_status;
        dto.hours.isOpen = hours_isOpen;
        dto.hours.isLocalHoliday = hours_isLocalHoliday;

        int hearNow_count = cursor.getInt(cursor.getColumnIndex("hearNow_count"));
        String hearNow_summary = cursor.getString(cursor.getColumnIndex(
                "hearNow_summary"));

        if (hearNow_count != 0 || hearNow_summary != null) {
            dto.hearNow = new FSHearNowDTO();
            dto.hearNow.count = hearNow_count;
            dto.hearNow.summary = hearNow_summary;
        }

        dto.isFavorite = cursor.getInt(cursor.getColumnIndex("isFavorite")) == 1;

        dto.location = mDaoFactory.getFSLocationDAO().getRecordByColumn("entity_uuid",
                dto.id);

        IGenericList<String> categoryIdList = mDaoFactory.getAssociationDAO(
                VENUE_CATEGORY_ASSOCIATION).getColumnListWhereColumnIn("rhs_uuid",
                "lhs_uuid", dto.id);
        if (categoryIdList != null) {
            dto.categories = mDaoFactory.getFSCategoryDAO().getRecordListByColumnIn(
                    "category_id", categoryIdList);
        }

        return dto;
    }
}
