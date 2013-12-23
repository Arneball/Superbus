package co.touchlab.android.superbus;

import android.database.sqlite.SQLiteDatabase;
import co.touchlab.android.superbus.provider.sqlite.SimpleDatabaseHelper;

/**
 * Created with IntelliJ IDEA.
 * User: kgalligan
 * Date: 12/22/13
 * Time: 5:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class SimplePersistedApplication extends AbstractPersistedApplication
{
    @Override
    protected SQLiteDatabase getWritableDatabase()
    {
        return SimpleDatabaseHelper.getInstance(SimplePersistedApplication.this).getWritableDatabase();
    }
}
