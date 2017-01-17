package JenkinsPluginForA4C.A4Cplugin.utils.exceptions;


public class TopologyDoesNotExistException extends Exception{

    public TopologyDoesNotExistException(String topology,String version){
        super("the topology "+topology+":"+version+" does not exist");
    }

}
