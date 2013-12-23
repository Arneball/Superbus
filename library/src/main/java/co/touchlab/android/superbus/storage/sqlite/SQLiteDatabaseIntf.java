package co.touchlab.android.superbus.storage.sqlite;

import android.content.ContentValues;
import co.touchlab.android.superbus.errorcontrol.StorageException;

/**
 * Created with IntelliJ IDEA.
 * User: kgalligan
 * Date: 11/18/12
 * Time: 11:01 PM
 * To change this template use File | Settings | File Templates.
 */
public interface SQLiteDatabaseIntf
{
    CursorIntf query(String tableName, String[] columnList);
    void execSQL(String sql)throws StorageException;
    int delete(String tableName, String query, String[] params);
    long insertOrThrow(String tableName, String nullColHack, ContentValues values)throws StorageException;
    int update(String tableName, ContentValues values, String whereClause, String[] whereArgs);
}
