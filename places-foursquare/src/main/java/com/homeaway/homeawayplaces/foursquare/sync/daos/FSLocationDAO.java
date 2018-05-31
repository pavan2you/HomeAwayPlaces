package com.homeaway.homeawayplaces.foursquare.sync.daos;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import com.homeaway.homeawayplaces.foursquare.sync.dtos.FSLocationDTO;
import com.jvanila.core.PlatformFactoryLocator;
import com.jvanila.droid.sync.daos.SQLiteTableInfo;
import com.jvanila.droid.sync.daos.TypedDAO;
import com.jvanila.mobile.storage.ISqliteAccessor;

/**
 * Created by pavan on 27/05/18.
 *
 * Performs CRUD on Foursquare's Locations
 *
 */
public class FSLocationDAO extends TypedDAO<FSLocationDTO> {

    private static class FSLocationTableInfo extends SQLiteTableInfo<FSLocationDTO> {

        FSLocationTableInfo(String name, String primaryKey, String foreignKey,
            String[] compositePrimaryKey, String[] compositeForeignKey) {

            super(name, primaryKey, foreignKey, compositePrimaryKey, compositeForeignKey);
        }

        @Override
        public String getValueOf(FSLocationDTO dto, String key) {
            if (key.equals(foreignKey)) {
                return dto.entity_uuid;
            }
            return null;
        }
    }

    public FSLocationDAO(ISqliteAccessor database) {
        super(new FSLocationTableInfo("FSLocation",
                null, "entity_uuid",
                null, null), database);
    }

    @Override
    public int createRecord(FSLocationDTO dto) {
        if (isDebug()) {
            System.out.println("DAOThreadDebug : FSLocationDAO -> createRecord from "
                    + Thread.currentThread().getName());
        }

        if (dto.CRUDOperation.equals("D")) {
            return -1;
        }

        String query = "INSERT INTO " + mTableInfo.name + " (entity_uuid, address, crossStreet, " +
                "lat, lng, distance, postalCode, cc, city, state, country, formattedAddress) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

        SQLiteStatement statement = mDatabase.compileStatement(query);

        if (dto.entity_uuid != null) {
            statement.bindString(1, dto.entity_uuid);
        } else {
            statement.bindNull(1);
        }

        if (dto.address != null) {
            statement.bindString(2, dto.address);
        } else {
            statement.bindNull(2);
        }

        if (dto.crossStreet != null) {
            statement.bindString(3, dto.crossStreet);
        } else {
            statement.bindNull(3);
        }

        statement.bindDouble(4, dto.lat);
        statement.bindDouble(5, dto.lng);
        statement.bindLong(6, dto.distance);
        statement.bindLong(7, dto.postalCode);

        if (dto.cc != null) {
            statement.bindString(8, dto.cc);
        } else {
            statement.bindNull(8);
        }

        if (dto.city != null) {
            statement.bindString(9, dto.city);
        } else {
            statement.bindNull(9);
        }

        if (dto.state != null) {
            statement.bindString(10, dto.state);
        } else {
            statement.bindNull(10);
        }

        if (dto.country != null) {
            statement.bindString(11, dto.country);
        } else {
            statement.bindNull(11);
        }

        if (dto.formattedAddress != null && dto.formattedAddress.size() > 0) {
            statement.bindString(12,
                    dto.getCSVRepresentationOfformattedAddress("!~*~!"));
        }
        else {
            statement.bindNull(12);
        }

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
    public int updateRecord(FSLocationDTO dto) {
        deleteRecordByColumn("entity_uuid", dto.entity_uuid);
        return createRecord(dto);
    }

    @Override
    public FSLocationDTO toDataObject(Cursor cursor) {
        FSLocationDTO dto = new FSLocationDTO();
        dto.entity_uuid = cursor.getString(cursor.getColumnIndex("entity_uuid"));
        dto.address = cursor.getString(cursor.getColumnIndex("address"));
        dto.crossStreet = cursor.getString(cursor.getColumnIndex("crossStreet"));
        dto.lat = cursor.getDouble(cursor.getColumnIndex("lat"));
        dto.lng = cursor.getDouble(cursor.getColumnIndex("lng"));
        dto.distance = cursor.getInt(cursor.getColumnIndex("distance"));
        dto.postalCode = cursor.getInt(cursor.getColumnIndex("postalCode"));
        dto.cc = cursor.getString(cursor.getColumnIndex("cc"));
        dto.city = cursor.getString(cursor.getColumnIndex("city"));
        dto.state = cursor.getString(cursor.getColumnIndex("state"));
        dto.country = cursor.getString(cursor.getColumnIndex("country"));
        dto.setCSVRepresentationOfformattedAddress(cursor.getString(cursor
                .getColumnIndex("formattedAddress")), "!~*~!");
        return dto;
    }

    /***********************************************************************************************
     * Overriding for ios tool conversion purpose, as it is failing to detect super class methods
     **********************************************************************************************/

    @Override
    public void setSQLiteAccessor(ISqliteAccessor iSqliteAccessor) {
        super.setSQLiteAccessor(iSqliteAccessor);
    }

    @Override
    public boolean isSQLiteDatabaseOpen() {
        return super.isSQLiteDatabaseOpen();
    }
}
