package com.homeaway.foursqureplaces.sync.daos;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import com.homeaway.foursqureplaces.sync.dtos.FSCategoryDTO;
import com.homeaway.foursqureplaces.sync.dtos.FSIconDTO;
import com.jvanila.core.PlatformFactoryLocator;
import com.jvanila.droid.sync.daos.SQLiteTableInfo;
import com.jvanila.droid.sync.daos.TypedDAO;
import com.jvanila.mobile.storage.ISqliteAccessor;

/**
 * Created by pavan on 27/05/18.
 *
 * Performs CRUD on Foursquare's Categories
 *
 */
public class FSCategoryDAO extends TypedDAO<FSCategoryDTO> {

    private static class FSCategoryTableInfo extends SQLiteTableInfo<FSCategoryDTO> {

        FSCategoryTableInfo(String name, String primaryKey, String foreignKey,
            String[] compositePrimaryKey, String[] compositeForeignKey) {

            super(name, primaryKey, foreignKey, compositePrimaryKey, compositeForeignKey);
        }

        @Override
        public String getValueOf(FSCategoryDTO dto, String key) {
            if (key.equals(primaryKey)) {
                return dto.id;
            }
            return null;
        }
    }

    public FSCategoryDAO(ISqliteAccessor database) {
        super(new FSCategoryTableInfo("FSCategory",
                "category_id", null,
                null, null), database);
    }

    @Override
    public int createRecord(FSCategoryDTO dto) {
        if (isDebug()) {
            System.out.println("DAOThreadDebug : FSCategoryDAO -> createRecord from "
                    + Thread.currentThread().getName());
        }

        if (dto.CRUDOperation.equals("D")) {
            return -1;
        }

        String query = "INSERT INTO " + mTableInfo.name + " (category_id, name, pluralName, " +
                "shortName, icon_prefix, icon_suffix, isPrimary) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?);";

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

        if (dto.pluralName != null) {
            statement.bindString(3, dto.pluralName);
        } else {
            statement.bindNull(3);
        }

        if (dto.shortName != null) {
            statement.bindString(4, dto.shortName);
        } else {
            statement.bindNull(4);
        }

        if (dto.icon != null) {
            if (dto.icon.prefix != null) {
                statement.bindString(5, dto.icon.prefix);
            } else {
                statement.bindNull(5);
            }
            if (dto.icon.suffix != null) {
                statement.bindString(6, dto.icon.suffix);
            } else {
                statement.bindNull(6);
            }
        }

        statement.bindLong(7, dto.primary ? 1 : 0);

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
    public FSCategoryDTO toDataObject(Cursor cursor) {
        FSCategoryDTO dto = new FSCategoryDTO();
        dto.id = cursor.getString(cursor.getColumnIndex("category_id"));
        dto.name = cursor.getString(cursor.getColumnIndex("name"));
        dto.pluralName = cursor.getString(cursor.getColumnIndex("pluralName"));
        dto.shortName = cursor.getString(cursor.getColumnIndex("shortName"));

        String icon_prefix = cursor.getString(cursor.getColumnIndex("icon_prefix"));
        String icon_suffix = cursor.getString(cursor.getColumnIndex("icon_suffix"));
        if (icon_prefix != null) {
            dto.icon = new FSIconDTO();
            dto.icon.prefix = icon_prefix;
            dto.icon.suffix = icon_suffix;
        }

        dto.primary = cursor.getInt(cursor.getColumnIndex("isPrimary")) == 1;

        return dto;
    }
}
