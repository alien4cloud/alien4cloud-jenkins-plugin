package JenkinsPluginForA4C.A4Cplugin;


import JenkinsPluginForA4C.A4Cplugin.utils.AlienDriver;

public class MockTrigger {

    public static void main(String[] args) throws Exception{
        String login = "admin";
        String password = "admin";
        String port = "8088";
        String a4cEndpoint = "localhost";
        String topoName = "TestJenkins2";
        String version = "0.1.0-SNAPSHOT";
        String environmentName = "Environment";

        System.out.println("I'm not useless");
        //A4CPluginBuilder myBuilder=  new A4CPluginBuilder(login,password,port,a4cEndpoint,topoName,version,environmentName);
        //Boolean b= true;
        //A4CDeployAppStep myStep = new A4CDeployAppStep(login,password,port,a4cEndpoint,topoName,environmentName,b);
        //myStep.perform();
        //myBuilder.mockMethod();


        AlienDriver alienDriver = new AlienDriver();
        //driver.getEnvId("TestJenkins2","Environment");


        //CHECK IF APP IS DEPLOYED
        String environmentId = alienDriver.getEnvId(topoName,environmentName);
       //listener.getLogger().println("get Environment Id "+environmentId);
        String deploymentId = alienDriver.appIsDeployed(topoName,environmentId);

        //UNDEPLOYING
        if(deploymentId!=null) {
            String status = alienDriver.getDeploymentStatus(deploymentId);
            if(!status.equals("UNDEPLOYED")) {
               // listener.getLogger().println("Undeploying application "+this.topoName);
                alienDriver.undeployApplication(deploymentId);
                alienDriver.waitForApplicationStatus(deploymentId,"UNDEPLOYED");
            }
        }else{
           // listener.getLogger().println("Application is undeployed");
        }

       // listener.getLogger().println("Deploying application ...");
        //DEPLOYING
        alienDriver.deployApplication(topoName,environmentId);
        //if(waitForDeployEnd){
        //    alienDriver.waitForApplicationStatus(deploymentId,"DEPLOYED");
        //}
    }

}
