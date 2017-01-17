package JenkinsPluginForA4C.A4Cplugin.utils;


public class ApplicationDoesNotExistException extends Exception{

    ApplicationDoesNotExistException(String appName){
        super("the application "+appName+" does not exist");
    }

}
