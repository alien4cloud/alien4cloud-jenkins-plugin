package JenkinsPluginForA4C.A4Cplugin;


public class MockTrigger {

    public static void main(String[] args) {
        String login = "admin";
        String password = "admin";
        String port = "8088";
        String a4cEndpoint = "localhost";
        String topoName = "MyAppTest";
        String version = "0.1.0-SNAPSHOT";
        String environmentName = "Environment";

        A4CPluginBuilder myBuilder=  new A4CPluginBuilder(login,password,port,a4cEndpoint,topoName,version,environmentName);

        myBuilder.mockMethod();
    }

}
