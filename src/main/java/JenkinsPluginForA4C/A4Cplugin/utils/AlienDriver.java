package JenkinsPluginForA4C.A4Cplugin.utils;


//import hidden.jth.org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.util.EntityUtils;
import org.json.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.*;

public class AlienDriver {

    //TODO get config from file ?
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
        //TODO make private ?
        try {
            HttpPost postRequest = new HttpPost("/login?username="+this.login+"&password="+this.password+"&submit=Login");
            HttpResponse httpResponse = httpclient.execute(target,postRequest,localContext);
            printResponse(httpResponse);
            EntityUtils.consume(httpResponse.getEntity());
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void ensureConnection(){
        if(!checkIsConnected()){
            connect();
        }
    }

    public void loadCSAR(String csarPath){
        ensureConnection();
        try {
            HttpPost postRequest = new HttpPost("/rest/csars");
            FileBody bin = new FileBody(new File(csarPath));
            HttpEntity entity = MultipartEntityBuilder
                    .create()
                    .addPart("file",bin)
                    .build();
            postRequest.setEntity(entity);
            HttpResponse httpResponse = httpclient.execute(target,postRequest,localContext);
            printResponse(httpResponse);
            EntityUtils.consume(httpResponse.getEntity());
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void recoverTopology(String topologyName){
        ensureConnection();
        try {
            HttpPut putRequest = new HttpPut("/rest/latest/editor/" + topologyName + ":0.1.0-SNAPSHOT/recover?lastOperationId=null");
            HttpResponse httpResponse = httpclient.execute(target, putRequest, localContext);
            printResponse(httpResponse);
            EntityUtils.consume(httpResponse.getEntity());
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printResponse(HttpResponse httpResponse){
        try {
            System.out.println("----------------------------------------------------");
            System.out.println(httpResponse.getEntity().getContent());
            System.out.println(httpResponse.getStatusLine().getStatusCode());
            System.out.println();
            System.out.println("----------------------------------------------------");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
