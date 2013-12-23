package co.touchlab.android.superbus.provider.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import co.touchlab.android.superbus.StorageException;
import co.touchlab.android.superbus.provider.CommandPersistenceProvider;

/**
 * Created with IntelliJ IDEA.
 * User: kgalligan
 * Date: 12/22/13
 * Time: 5:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class SimpleDatabaseHelper extends SQLiteOpenHelper
{
    public final static String DATABASE_FILE_NAME = "superbusstorage";

    private final static int VERSION = 1;

    private static SimpleDatabaseHelper INSTANCE;

    public static synchronized SimpleDatabaseHelper getInstance(Context context)
    {
        if (INSTANCE == null)
        {
            INSTANCE = new SimpleDatabaseHelper(context.getApplicationContext());
        }

        return INSTANCE;
    }

    private SimpleDatabaseHelper(Context context)
    {
        super(context, DATABASE_FILE_NAME, null, VERSION);
    }

    @Override
    public void onOpen(SQLiteDatabase db)
    {
        super.onOpen(db);
        // @reminder Must enable foreign keys as it's off by default
        db.execSQL("PRAGMA foreign_keys=ON;");
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        try
        {
            CommandPersistenceProvider.createTables(new ClearSQLiteDatabase(db));
        }
        catch (StorageException e)
        {
            throw new RuntimeException("Unable to create db", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {

    }
}
