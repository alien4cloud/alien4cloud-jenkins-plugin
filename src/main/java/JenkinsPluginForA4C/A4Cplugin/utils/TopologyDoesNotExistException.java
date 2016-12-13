package JenkinsPluginForA4C.A4Cplugin.utils;


public class TopologyDoesNotExistException extends Exception{

    TopologyDoesNotExistException(){
        super("this topology does not exist");
    }

    TopologyDoesNotExistException(String str){
        super(str);
    }
}
