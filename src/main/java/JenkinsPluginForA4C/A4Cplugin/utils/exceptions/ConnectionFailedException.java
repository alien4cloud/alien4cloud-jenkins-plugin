package JenkinsPluginForA4C.A4Cplugin.utils.exceptions;


public class ConnectionFailedException extends Exception{

    public ConnectionFailedException(String domain,String port){
        super("can't connect to A4C on http://"+domain+":"+port);
    }

}
