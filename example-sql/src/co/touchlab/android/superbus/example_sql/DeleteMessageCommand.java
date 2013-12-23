package co.touchlab.android.superbus.example_sql;

import android.content.Context;
import co.touchlab.android.superbus.Command;
import co.touchlab.android.superbus.errorcontrol.PermanentException;
import co.touchlab.android.superbus.errorcontrol.StorageException;
import co.touchlab.android.superbus.errorcontrol.TransientException;
import co.touchlab.android.superbus.http.BusHttpClient;
import com.turbomanage.httpclient.HttpResponse;
import com.turbomanage.httpclient.ParameterMap;

/**
 * Created with IntelliJ IDEA.
 * User: touchlab
 * Date: 10/19/12
 * Time: 4:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class DeleteMessageCommand extends Command
{
    Long serverId;

    public DeleteMessageCommand(){}

    public DeleteMessageCommand(long id)
    {
        this.serverId = id;
    }

    @Override
    public String logSummary() {
        return "DeleteMessageCommand["+ serverId +"]";    }

    @Override
    public boolean same(Command command) {
        if(!(command instanceof DeleteMessageCommand))
            return false;

        DeleteMessageCommand deleteMessageCommand = (DeleteMessageCommand) command;
        return serverId.equals(deleteMessageCommand.serverId);
    }

    @Override
    public void callCommand(Context context) throws TransientException, PermanentException
    {
        BusHttpClient httpClient = new BusHttpClient("http://wejit.herokuapp.com");

        ParameterMap params = httpClient.newParams().add("id", serverId.toString());

        httpClient.setConnectionTimeout(10000);
        HttpResponse httpResponse = httpClient.post("/device/deleteExamplePost", params);

        //Check if anything went south
        httpClient.checkAndThrowError();

        try
        {
            ((MyApplication)context.getApplicationContext()).getProvider().put(context, new GetMessageCommand());
        }
        catch (StorageException e)
        {
            //Optional
        }
    }

    @Override
    public void onPermanentError(Context context, PermanentException exception)
    {
        CommandErrorReceiver.showMessage(context, "Permanent Error");
    }

    @Override
    public void onTransientError(Context context, TransientException exception)
    {
        CommandErrorReceiver.showMessage(context, "Transient Error");
    }

    @Override
    public void onSuccess(Context context)
    {
        CommandErrorReceiver.showMessage(context, "Success");
    }
}
