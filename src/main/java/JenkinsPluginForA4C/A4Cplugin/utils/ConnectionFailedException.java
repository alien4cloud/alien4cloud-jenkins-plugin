package JenkinsPluginForA4C.A4Cplugin.utils;


public class ConnectionFailedException extends Exception{

    ConnectionFailedException(String domain,String port){
        super("can't connect to A4C on http://"+domain+":"+port);
    }

}
