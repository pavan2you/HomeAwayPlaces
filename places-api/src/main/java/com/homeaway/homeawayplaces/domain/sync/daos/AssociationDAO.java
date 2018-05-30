package com.homeaway.homeawayplaces.domain.sync.daos;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import com.homeaway.homeawayplaces.domain.sync.dtos.AssociationDTO;
import com.jvanila.core.PlatformFactoryLocator;
import com.jvanila.core.collection.IGenericList;
import com.jvanila.droid.sync.daos.SQLiteQuery;
import com.jvanila.droid.sync.daos.SQLiteTableInfo;
import com.jvanila.droid.sync.daos.TypedDAO;
import com.jvanila.mobile.storage.ISqliteAccessor;
import com.jvanila.mobile.storage.SqliteObjectLostEvent;

/**
 * Created by pavan on 28/05/18.
 *
 * A convenient way of performing CRUD on Entity to Entity associations.
 */
public class AssociationDAO extends TypedDAO<AssociationDTO> {

    private static class AssociationTableInfo extends SQLiteTableInfo<AssociationDTO> {

        AssociationTableInfo(String name, String primaryKey, String foreignKey,
            String[] compositePrimaryKey, String[] compositeForeignKey) {

            super(name, primaryKey, foreignKey, compositePrimaryKey, compositeForeignKey);
        }

        @Override
        public String getValueOf(AssociationDTO dto, String key) {
            if (key.equals(foreignKey)) {
                return dto.lhs_uuid;
            }
            return null;
        }
    }

    public AssociationDAO(ISqliteAccessor database, String associationTableName) {
        super(new AssociationTableInfo(associationTableName,
                null, "lhs_uuid",
                null, null), database);
    }

    @Override
    public int createRecord(AssociationDTO dto) {
        if (isDebug()) {
            System.out.println("DAOThreadDebug : AssociationDAO -> createRecord from "
                    + Thread.currentThread().getName());
        }

        if (dto.CRUDOperation.equals("D")) {
            return -1;
        }

        String query = "INSERT INTO " + mTableInfo.name + " (lhs_uuid, " +
                "rhs_uuid) VALUES (?, ?);";
        SQLiteStatement statement = mDatabase.compileStatement(query);

        if (dto.lhs_uuid != null) {
            statement.bindString(1, dto.lhs_uuid);
        } else {
            statement.bindNull(1);
        }

        if (dto.rhs_uuid != null) {
            statement.bindString(2, dto.rhs_uuid);
        } else {
            statement.bindNull(2);
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
    public int updateRecord(AssociationDTO dto) {
        String _lhs_uuid = dto.lhs_uuid;
        String _rhs_uuid = dto.rhs_uuid;
        deleteAssociationRecord(_lhs_uuid, _rhs_uuid);
        return createRecord(dto);
    }

    private boolean deleteAssociationRecord(String lhs_uuid, String rhs_uuid) {

        boolean fixed = fixSQLiteDatabaseLoss();
        if (!fixed) {
            PlatformFactoryLocator.getPlatformFactory().getEventBus().publish(
                    new SqliteObjectLostEvent());
            return false;
        }

        String query = "DELETE FROM " + mTableInfo.name + " WHERE lhs_uuid =? AND rhs_uuid =?";
        String[] params = new String[] {lhs_uuid, rhs_uuid};

        return new SQLiteQuery<>(mDatabase, query, params, this).execute();
    }

    @Override
    public AssociationDTO toDataObject(Cursor cursor) {
        AssociationDTO dto = new AssociationDTO();
        dto.lhs_uuid = cursor.getString(cursor.getColumnIndex("lhs_uuid"));
        dto.rhs_uuid = cursor.getString(cursor.getColumnIndex("rhs_uuid"));
        return dto;
    }

    public IGenericList<String> getColumnListWhereColumnIn(String columnName,
            String whereColumnName, String whereColumnValue) {

        String query = "SELECT " + columnName + " FROM " + this.mTableInfo.name + " WHERE "
                + whereColumnName + " =?";

        String[] params = new String[] {whereColumnValue};

        return new SQLiteQuery<>(this.mDatabase, query, params, this)
                .getColumnList(columnName);
    }
}
