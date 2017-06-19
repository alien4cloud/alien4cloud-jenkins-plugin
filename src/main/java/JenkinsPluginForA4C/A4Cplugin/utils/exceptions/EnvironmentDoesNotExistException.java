package JenkinsPluginForA4C.A4Cplugin.utils.exceptions;


public class EnvironmentDoesNotExistException extends Exception{

    public EnvironmentDoesNotExistException(String app, String envName){
        super("Environment "+envName+" for application "+app+" does not exist");
    }

}
