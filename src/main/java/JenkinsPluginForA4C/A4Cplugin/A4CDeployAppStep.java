package JenkinsPluginForA4C.A4Cplugin;

import java.io.IOException;

import javax.servlet.ServletException;

import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import JenkinsPluginForA4C.A4Cplugin.utils.AlienDriver;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;

/**
 * Sample {@link Builder}.
 *
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
 * and a new {@link A4CDeployAppStep} is created. The created
 * instance is persisted to the project configuration XML by using
 * XStream, so this allows you to use instance fields (like {@link #login})
 * to remember the configuration.
 *
 * <p>
 * When a build is performed, the {@link #perform} method will be invoked. 
 *
 * @author Kohsuke Kawaguchi
 */
public class A4CDeployAppStep extends Builder implements SimpleBuildStep {

    private final String login;
    private final String password;
    private final int port;
    private final String a4cDomain;
    private final String topoName;
    private final String environmentName;
    private final Boolean waitForDeployEnd;

    private AlienDriver alienDriver;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public A4CDeployAppStep(String login, String password, String port, String a4cDomain, String topoName, String environmentName,Boolean waitForDeployEnd) {

        this.waitForDeployEnd=waitForDeployEnd;

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

        if(a4cDomain==null){
            this.a4cDomain="127.0.0.1";
        }else{
            this.a4cDomain=a4cDomain;
        }

        this.environmentName=environmentName;

        int portValue = 8088;
        try {
            portValue = Integer.parseInt(port);
        } catch(NumberFormatException e) {
            //TODO error log
        }
        this.port = portValue;

        //TODO: remove when checkConnection will be ok
        //this.alienDriver.ensureConnection();
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
    public String getA4cDomain() {
        return a4cDomain;
    }
    public String getTopoName() {
        return topoName;
    }
    public String getEnvironmentName() {
        return environmentName;
    }
    public Boolean getWaitForDeployEnd() {
        return waitForDeployEnd;
    }

    @Override
    public void perform(Run<?,?> build, FilePath workspace, Launcher launcher, TaskListener listener) {
        if(this.alienDriver == null) {
            this.alienDriver = new AlienDriver(login, password, a4cDomain, this.port, listener);
        }
            //TODO be sure a location placment policy has been defined
        //or allow user to configure one via Jenkins
        //CHECK IF APP IS DEPLOYED
        String environmentId = null;
        try{
            environmentId = alienDriver.getEnvId(this.topoName,this.environmentName);
            listener.getLogger().println("get Environment Id "+environmentId);
            String deploymentId = alienDriver.appIsDeployed(this.topoName,environmentId);

            //UNDEPLOYING
            if(deploymentId!=null) {
                String status = alienDriver.getDeploymentStatus(deploymentId);
                if(!status.equals("UNDEPLOYED")) {
                    listener.getLogger().println("Undeploying application "+this.topoName);
                    alienDriver.undeployApplication(deploymentId);
                    alienDriver.waitForApplicationStatus(deploymentId,"UNDEPLOYED");
                }
            }else{
                listener.getLogger().println("Application is undeployed");
            }

            listener.getLogger().println("Deploying application ...");
            //DEPLOYING
            deploymentId = alienDriver.deployApplication(this.topoName,environmentId);
            if(waitForDeployEnd){
                alienDriver.waitForApplicationStatus(deploymentId,"DEPLOYED");
            }
        }catch (Exception e){
            listener.getLogger().println(e.getMessage());
        }


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
            return "A4C - deploy an application";
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

