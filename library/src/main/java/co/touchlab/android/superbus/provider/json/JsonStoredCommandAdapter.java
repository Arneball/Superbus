package co.touchlab.android.superbus.provider.json;

import co.touchlab.android.superbus.Command;
import co.touchlab.android.superbus.StorageException;
import co.touchlab.android.superbus.provider.stringbased.StoredCommandAdapter;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Created with IntelliJ IDEA.
 * User: kgalligan
 * Date: 10/13/12
 * Time: 3:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class JsonStoredCommandAdapter implements StoredCommandAdapter
{
    @Override
    public Command inflateCommand(String data, String className) throws StorageException, ClassNotFoundException
    {
        /*try
        {
            JSONObject json= (JSONObject) new JSONTokener(data).nextValue();
            Object o = Class.forName(className).newInstance();
            JsonCommand jsonCommand = (JsonCommand) o;
            jsonCommand.inflate(json);
            return jsonCommand;
        }
        catch (ClassNotFoundException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new StorageException(e);
        }*/
        throw new UnsupportedOperationException("Need to figure this out");
    }

    @Override
    public String storeCommand(Command command) throws StorageException
    {
        /*JSONObject json = new JSONObject();
        ((JsonCommand)command).store(json);
        return json.toString();*/
        throw new UnsupportedOperationException("Need to figure this out");
    }
}
