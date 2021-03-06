package co.touchlab.android.superbus.example_sql;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import co.touchlab.android.superbus.Command;
import co.touchlab.android.superbus.appsupport.CommandBusHelper;
import co.touchlab.android.superbus.errorcontrol.PermanentException;
import co.touchlab.android.superbus.errorcontrol.StorageException;
import co.touchlab.android.superbus.errorcontrol.TransientException;
import co.touchlab.android.superbus.http.BusHttpClient;
import com.turbomanage.httpclient.HttpResponse;
import org.json.JSONException;

/**
 * Created with IntelliJ IDEA.
 * User: kgalligan
 * Date: 10/14/12
 * Time: 2:48 AM
 * To change this template use File | Settings | File Templates.
 */
public class GetMessageCommand extends Command
{

    public static final String GET_MESSAGE_COMMAND_COMPLETE = "GetMessageCommand-complete";
    public static final String CANCEL_UPDATE = "CANCEL_UPDATE";
    private boolean cancelUpdate = false;

    public GetMessageCommand()
    {
        setPriority(LOWER_PRIORITY);
    }

    @Override
    public void onRuntimeMessage(Context context, String message)
    {
        if(message.equals(CANCEL_UPDATE))
            cancelUpdate = true;
        CommandBusHelper.submitCommandSync(context, this);
    }

    @Override
    public String logSummary()
    {
        return "Get the messages!";
    }

    @Override
    public boolean same(Command command)
    {
        return command instanceof GetMessageCommand;
    }

    @Override
    public void callCommand(Context context) throws TransientException, PermanentException
    {
        BusHttpClient httpClient = new BusHttpClient("http://wejit.herokuapp.com");

        httpClient.setConnectionTimeout(10000);
        HttpResponse httpResponse = httpClient.get("/device/getExamplePosts", null);

        httpClient.checkAndThrowError();

        String content = httpResponse.getBodyAsString();

        DatabaseHelper instance = DatabaseHelper.getInstance(context);
        SQLiteDatabase writableDatabase = instance.getWritableDatabase();

        try
        {
            writableDatabase.beginTransaction();
            if(!cancelUpdate)
                instance.saveToDb(context, content);
            writableDatabase.setTransactionSuccessful();
        }
        catch (JSONException e)
        {
            throw new PermanentException(e);
        }
        finally
        {
            writableDatabase.endTransaction();
        }

        sendUpdateBroadcast(context);
    }

    public static void sendUpdateBroadcast(Context context)
    {
        context.sendBroadcast(new Intent(GET_MESSAGE_COMMAND_COMPLETE));
    }
}
