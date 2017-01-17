package JenkinsPluginForA4C.A4Cplugin.utils;

import JenkinsPluginForA4C.A4Cplugin.utils.exceptions.ConnectionFailedException;
import org.apache.commons.io.IOUtils;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.apache.http.client.methods.*;

import java.io.IOException;
import java.io.InputStream;

import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:application-test-context.xml")
public class AlienDriverTest {

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

    @Mock
    private HttpClient defaultHttpClient;

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Before
    public void init(){
        mockRestTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(mockRestTemplate);
    }

    @Test
    public void connectionNotOKTest() {
        //given:
        HttpClient httpClient = mock(HttpClient.class);
        InputStream is = mock(InputStream.class);
        HttpEntity he =  mock(HttpEntity.class);
        HttpResponse httpResponse = mock(HttpResponse.class);
        StatusLine statusLine = mock(StatusLine.class);
        boolean testPassed=false;

        try {
            when(statusLine.getStatusCode()).thenReturn(404);
            when(httpResponse.getStatusLine()).thenReturn(statusLine);
            when(is.toString()).thenReturn("test object");
            when(he.getContent()).thenReturn(is);
            when(httpResponse.getEntity()).thenReturn(he);
            when(statusLine.getReasonPhrase()).thenReturn("test object");
            when(httpClient.execute(Matchers.any(HttpHost.class),Matchers.any(HttpGet.class),Matchers.any(HttpContext.class))).thenReturn(httpResponse);

            AlienDriver ad= new AlienDriver();
            ad.setHttpclient(httpClient);
            ad.ensureConnection();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ConnectionFailedException e) {
            testPassed=true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println("test passed : "+testPassed);
        assert(testPassed);
    }

    @Test
    public void connectionOKTest() {
        //given:
        HttpClient httpClient = mock(HttpClient.class);
        InputStream is = IOUtils.toInputStream("{\"data\":{\"isLogged\":true}}");
        HttpEntity he =  mock(HttpEntity.class);
        HttpResponse httpResponse = mock(HttpResponse.class);
        StatusLine statusLine = mock(StatusLine.class);
        boolean testPassed=true;

        try {
            when(statusLine.getStatusCode()).thenReturn(200);
            when(httpResponse.getStatusLine()).thenReturn(statusLine);
            when(he.getContent()).thenReturn(is);
            when(httpResponse.getEntity()).thenReturn(he);
            when(statusLine.getReasonPhrase()).thenReturn("test object");
            when(httpClient.execute(Matchers.any(HttpHost.class),httpGetURIEq("/rest/v1/auth/status"),Matchers.any(HttpContext.class))).thenReturn(httpResponse);

            AlienDriver ad= new AlienDriver();
            ad.setHttpclient(httpClient);
            ad.ensureConnection();

        } catch (IOException e) {
            e.printStackTrace();
            testPassed=false;
        } catch (ConnectionFailedException e) {
            testPassed=false;
        } catch (Exception e) {
            e.printStackTrace();
            testPassed=false;
        }
        assert(testPassed);
    }

    static HttpGet httpGetURIEq(String url) {
        return argThat(new HttpGetURIMatcher(url));
    }

    static class HttpGetURIMatcher extends ArgumentMatcher<HttpGet>{

        private final String url;

        public HttpGetURIMatcher(String url) {
            this.url = url;
        }

        @Override
        public boolean matches(Object actual) {
            // could improve with null checks
            System.out.println("test : "+((HttpGet) actual).getURI().toString().equals(url));
            return ((HttpGet) actual).getURI().toString().equals(url);
        }
    }
}
