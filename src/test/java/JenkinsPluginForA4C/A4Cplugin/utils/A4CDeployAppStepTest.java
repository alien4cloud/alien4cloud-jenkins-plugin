package JenkinsPluginForA4C.A4Cplugin.utils;

import JenkinsPluginForA4C.A4Cplugin.A4CDeployAppStep;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.JenkinsRule;
import hudson.model.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

/**
 * Created by a457407 on 28/12/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:application-test-context.xml")
public class A4CDeployAppStepTest {

    //ResponseEntity<> mockResponseEntity;
    MockRestServiceServer mockServer;
    RestTemplate mockRestTemplate;

    String login = "admin";
    String password = "admin";
    String port = "8088";
    String a4cDomain = "localhost";
    String topoName = "TestJenkins3";
    String environmentName = "Environment";
    Boolean waitForDeployEnd = true;

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Before
    public void init(){

        mockRestTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(mockRestTemplate);
    }

    @Test
    public void deploymentTest() throws Exception{

        FreeStyleProject p = j.createFreeStyleProject("testA4C");
        A4CDeployAppStep A4CDeploy = new A4CDeployAppStep(login, password, port, a4cDomain, topoName, environmentName,waitForDeployEnd);
        p.getBuildersList().add(A4CDeploy);
        j.submit(j.createWebClient().getPage(p,"configure").getFormByName("config"));

        p.scheduleBuild2(2);
        Thread.sleep(10000);
    }

}
