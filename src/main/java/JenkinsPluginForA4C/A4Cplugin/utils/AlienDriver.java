package JenkinsPluginForA4C.A4Cplugin.utils;


import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

public class AlienDriver {

    private String login = "admin";
    private String password = "admin";
    private String domain = "localhost";
    private int port = 8088;
    private CookieStore cookieStore;
    private HttpClientContext localContext;
    private HttpClient httpclient;
    private HttpHost target;

    private void init(){
        this.cookieStore = new BasicCookieStore();
        this.localContext = new HttpClientContext();
        this.target = new HttpHost(this.domain, port, "http");
        this.localContext.setAttribute(HttpClientContext.COOKIE_STORE,cookieStore);
        this.httpclient = HttpClientBuilder.create().build();
    }

    public AlienDriver(){
        init();
    }

    public AlienDriver(String login, String password, String domain,int port){
        this.login=login;
        this.password=password;
        this.domain=domain;
        this.port=port;
        init();

    }

    private boolean checkIsConnected(){
        //TODO
        //get /rest/v1/auth/status
        return true;
    }

    public void connect(){
        //TODO make private
        try {
            HttpPost postRequest = new HttpPost("/login?username="+this.login+"&password="+this.password+"&submit=Login");
            HttpResponse httpResponse = httpclient.execute(target,postRequest,localContext);// execute(hostConfig,getLoginMethod);//localContext, getRequest);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // When HttpClient instance is no longer needed,
            // shut down the connection manager to ensure
            // immediate deallocation of all system resources
            httpclient.getConnectionManager().shutdown();
        }
    }

    private void ensureConnection(){
        if(!checkIsConnected()){
            connect();
        }
    }

    public void loadCSAR(String csarPath){
        //TODO make private
        try {
            HttpPost postRequest = new HttpPost("/rest/csar/");

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("filecomment", "loaded via Jenkins"));
            params.add(new BasicNameValuePair("file", csarPath));
            postRequest.setEntity(new UrlEncodedFormEntity(params));

            HttpResponse httpResponse = httpclient.execute(target,postRequest,localContext);// execute(hostConfig,getLoginMethod);//localContext, getRequest);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // When HttpClient instance is no longer needed,
            // shut down the connection manager to ensure
            // immediate deallocation of all system resources
            httpclient.getConnectionManager().shutdown();
        }
    }



}
