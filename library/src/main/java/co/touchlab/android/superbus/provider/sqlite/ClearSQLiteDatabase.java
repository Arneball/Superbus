package co.touchlab.android.superbus.provider.sqlite;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import co.touchlab.android.superbus.StorageException;

/**
 * Created with IntelliJ IDEA.
 * User: kgalligan
 * Date: 11/18/12
 * Time: 11:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class ClearSQLiteDatabase implements SQLiteDatabaseIntf
{
    private SQLiteDatabase db;

    public static class ClearCursor implements CursorIntf
    {
        private Cursor cursor;

        public ClearCursor(Cursor cursor)
        {
            this.cursor = cursor;
        }

        @Override
        public boolean moveToNext()
        {
            return cursor.moveToNext();
        }

        @Override
        public void close()
        {
            cursor.close();
        }

        @Override
        public long getLong(int i)
        {
            return cursor.getLong(i);
        }

        @Override
        public String getString(int i)
        {
            return cursor.getString(i);
        }
    }

    public ClearSQLiteDatabase(SQLiteDatabase db)
    {
        this.db = db;
    }

    @Override
    public CursorIntf query(String tableName, String[] columnList)
    {
        return new ClearCursor(db.query(tableName, columnList, null, null, null, null, null));
    }

    @Override
    public void execSQL(String sql) throws StorageException
    {
        try
        {
            db.execSQL(sql);
        }
        catch (SQLException e)
        {
            throw new StorageException(e);
        }
    }

    @Override
    public int delete(String tableName, String query, String[] params)
    {
        return db.delete(tableName, query, params);
    }

    @Override
    public long insertOrThrow(String tableName, String nullColHack, ContentValues values) throws StorageException
    {
        try
        {
            return db.insertOrThrow(tableName, nullColHack, values);
        }
        catch (SQLException e)
        {
            throw new StorageException(e);
        }
    }

    @Override
    public int update(String tableName, ContentValues values, String whereClause, String[] whereArgs)
    {
        return db.update(tableName, values, whereClause, whereArgs);
    }
}
