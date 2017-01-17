package JenkinsPluginForA4C.A4Cplugin.utils.exceptions;


public class ApplicationDoesNotExistException extends Exception{

    public ApplicationDoesNotExistException(String appName){
        super("the application "+appName+" does not exist");
    }

}
