package JenkinsPluginForA4C.A4Cplugin;
import JenkinsPluginForA4C.A4Cplugin.utils.AlienDriver;
import JenkinsPluginForA4C.A4Cplugin.utils.TopologyDoesNotExistException;
import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.util.FormValidation;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Sample {@link Builder}.
 *
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
 * and a new {@link A4CPluginBuilder} is created. The created
 * instance is persisted to the project configuration XML by using
 * XStream, so this allows you to use instance fields (like {@link #login})
 * to remember the configuration.
 *
 * <p>
 * When a build is performed, the {@link #perform} method will be invoked. 
 *
 * @author Kohsuke Kawaguchi
 */
public class A4CPluginBuilder extends Builder implements SimpleBuildStep {

    private final String login;
    private final String password;
    private final int port;
    private final String a4cEndpoint;
    private final String topoName;
    private final String version;
    private final String environmentName;

    private AlienDriver alienDriver;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public A4CPluginBuilder(String login, String password, String port, String a4cEndpoint, String topoName,String version, String environmentName) {

        if(login==null){
            this.login="admin";
        }else{
            this.login=login;
        }

        if(password==null){
            this.password="admin";
        }else{
            this.password=password;
        }

        if(topoName==null){
            this.topoName="myApp";
        }else{
            this.topoName=topoName;
        }

        if(a4cEndpoint==null){
            this.a4cEndpoint="127.0.0.1";
        }else{
            this.a4cEndpoint=a4cEndpoint;
        }

        if(version==null){
            this.version="0.1.0-SNAPSHOT";
        }else{
            this.version=version;
        }

        if(environmentName==null){
            this.environmentName="Environment";
        }else{
            this.environmentName=environmentName;
        }

        int portValue = 8088;
        try {
            portValue = Integer.parseInt(port);
        } catch(NumberFormatException e) {
            //TODO error log
        }
        this.port = portValue;

        this.alienDriver = new AlienDriver(login,password,a4cEndpoint,this.port);
        //TODO: remove when checkConnection will be ok
        this.alienDriver.connect();
    }

    /*
     * We'll use this from the {@code config.jelly}.
     */

    public String getLogin() {
        return login;
    }
    public String getPassword() {
        return password;
    }
    public int getPort() {
        return port;
    }
    public String getA4cEndpoint() {
        return a4cEndpoint;
    }
    public String getTopoName() {
        return topoName;
    }
    public String getVersion() {
        return version;
    }
    public String getEnvironmentName() {
        return environmentName;
    }

    @Override
    public void perform(Run<?,?> build, FilePath workspace, Launcher launcher, TaskListener listener) {
        // This is where you 'build' the project.
        // Since this is a dummy, we just say 'hello world' and call that a build.

        listener.getLogger().println("login, "+login+"!");
        listener.getLogger().println("password, "+password+"!");
        listener.getLogger().println("port, "+port+"!");
        listener.getLogger().println("a4cEndpoint, "+a4cEndpoint+"!");
        listener.getLogger().println("topoName, "+topoName+"!");
        listener.getLogger().println("version, "+version+"!");
        listener.getLogger().println("environmentName, "+environmentName+"!");
        listener.getLogger().println("workspace : , "+workspace+"!");

        // This also shows how you can consult the global configuration of the builder
        //AlienDriver.
    }

    public void mockMethod(){
        //>LOAD CSAR
        //String path = System.getProperty("user.dir")+"\\testComponent\\beat-types.zip";
        //System.out.println("file to load : "+path);
        //alienDriver.loadCSAR(path);

        //>RECOVER TOPOLOGY
        //String toponame = "MyAppTest";
        //String version = "0.1.0-SNAPSHOT";
        //alienDriver.recoverTopology(toponame,version);

        //>GET INFO APP OR TEMP
        String toponame = "MyApp1";
        String version = "0.1.0-SNAPSHOT";
        String environmentName = "Environment";

        String environmentId = alienDriver.getEnvId(toponame,environmentName);

        boolean res = false;
        try {
            res = alienDriver.topologyIsFromApp(toponame,version);
        } catch (TopologyDoesNotExistException e) {
            e.printStackTrace();
        }
        System.out.println("is an app : "+res);

        //CHECK IF APP IS DEPLOYED
        String mydepId = alienDriver.appIsDeployed(toponame,environmentId);

        //UNDEPLOYING
        if(mydepId!=null) {
            String status = alienDriver.getDeploymentStatus(mydepId);
            System.out.println("App status : " + status);
            if(!status.equals("UNDEPLOYED")) {
                System.out.println("undeploying ...");
                alienDriver.undeployApplication(mydepId);
                while (!status.equals("UNDEPLOYED")) {
                    System.out.println("application is still deployed");
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    status = alienDriver.getDeploymentStatus(mydepId);
                }
                System.out.println("application is undeployed");
            }
        }

        //DEPLOYING
        alienDriver.deployApplication(toponame,environmentId);
    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    /*
     * Descriptor for {@link A4CPluginBuilder}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See {@code src/main/resources/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly}
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        /**
         * To persist global configuration information,
         * simply store it in a field and call save().
         *
         * <p>
         * If you don't want fields to be persisted, use {@code transient}.
         */
        //private boolean useFrench;

        /**
         * In order to load the persisted global configuration, you have to 
         * call load() in the constructor.
         */
        public DescriptorImpl() {
            load();
        }

        /**
         * Performs on-the-fly validation of the form field 'name'.
         *
         * @param value
         *      This parameter receives the value that the user has typed.
         * @return
         *      Indicates the outcome of the validation. This is sent to the browser.
         *      <p>
         *      Note that returning {@link FormValidation#error(String)} does not
         *      prevent the form from being saved. It just means that a message
         *      will be displayed to the user.
         *
         *
         */
        public FormValidation doCheckLogin(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set a login");
            return FormValidation.ok();
        }

        public FormValidation doCheckPort(@QueryParameter String value)
                throws IOException, ServletException {
            try {
                Integer.parseInt(value);
            } catch(NumberFormatException e) {
                return FormValidation.error("Incorrect value for port");
            } catch(NullPointerException e) {
                return FormValidation.error("Incorrect value for port");
            }
            return FormValidation.ok();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "update A4C app";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // To persist global configuration information,
            // set that to properties and call save().
            //useFrench = formData.getBoolean("useFrench");
            // ^Can also use req.bindJSON(this, formData);
            //  (easier when there are many fields; need set* methods for this, like setUseFrench)
            save();
            return super.configure(req,formData);
        }

        /**
         * This method returns true if the global configuration says we should speak French.
         *
         * The method name is bit awkward because global.jelly calls this method to determine
         * the initial state of the checkbox by the naming convention.
         */
        /*public boolean getUseFrench() {
            return useFrench;
        }*/
    }
}

