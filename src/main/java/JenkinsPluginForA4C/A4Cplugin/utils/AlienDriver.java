package JenkinsPluginForA4C.A4Cplugin.utils;


//import hidden.jth.org.apache.http.HttpEntity;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class AlienDriver {

    //Logger LOGGER = LogManager.getLogger(this.getClass());
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

    //make tests easy to run
    public void setHttpclient(HttpClient newHttpClient){
        this.httpclient=newHttpClient;
    }

    public void waitForApplicationStatus(String deploymentId,String status){
        String currentStatus;
        try{
            currentStatus = getDeploymentStatus(deploymentId);

            while (!currentStatus.equals(status)) {
                try {
                    //TODO : configurable ?
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                currentStatus = getDeploymentStatus(deploymentId);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private boolean checkIsConnected(){
        HttpGet getRequest = new HttpGet("/rest/v1/auth/status");
        HttpResponse response;
        try {
            response = httpclient.execute(target,getRequest,localContext);
            if(response.getStatusLine().getStatusCode() == 200){
                JSONObject test = new JSONObject(EntityUtils.toString(response.getEntity()));
                JSONObject test2 = test.getJSONObject("data");
                boolean isLogged = test2.getBoolean("isLogged");

                //TODO : this does not work with mocked object, don't know why
                //boolean isLogged = (new JSONObject(EntityUtils.toString(response.getEntity())))
                //        .getJSONObject("data").getBoolean("isLogged");


                if(isLogged) {
                    System.out.println("still connected to A4C ...");
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("not Connected to A4C");
        return false;
    }

    private void connect() throws ConnectionFailedException{
        HttpPost postRequest = new HttpPost("/login?username="+this.login+"&password="+this.password+"&submit=Login");
        HttpResponse httpResponse = null;
        boolean connectOk = false;
        try {
            httpResponse = httpclient.execute(target,postRequest,localContext);
            printResponse(httpResponse);
            connectOk = httpResponse.getStatusLine().getStatusCode() == 200;
            EntityUtils.consume(httpResponse.getEntity());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(!connectOk)
            throw new ConnectionFailedException(target.getHostName(),Integer.toString(target.getPort()));

    }

    public void ensureConnection() throws ConnectionFailedException{
            int retry = 3;
            while ((!checkIsConnected())&&(retry > 0)) {
                System.out.println("Try to connect to Alien4Cloud");
                connect();
                retry --;
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
    }

    public void loadCSAR(String csarPath) throws ConnectionFailedException{
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

    //TODO : throws TopologyDoesNotExistException
    public void recoverTopology(String topologyName,String version) throws ConnectionFailedException,TopologyDoesNotExistException{
        ensureConnection();
        try {
            HttpPut putRequest = new HttpPut("/rest/latest/editor/" + topologyName + ":"+version+"/recover?lastOperationId=null");
            HttpResponse httpResponse = httpclient.execute(target, putRequest, localContext);
            if(httpResponse.getStatusLine().getStatusCode() == 404) throw new TopologyDoesNotExistException();
            printResponse(httpResponse);
            EntityUtils.consume(httpResponse.getEntity());
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Boolean topologyIsFromApp(String topologyName,String version) throws ConnectionFailedException,TopologyDoesNotExistException{
        ensureConnection();
        boolean res = false;
        try {
            HttpGet getRequest = new HttpGet("/rest/catalog/topologies/" + topologyName + ":"+version);
            HttpResponse httpResponse = httpclient.execute(target, getRequest, localContext);
            printResponse(httpResponse);

            //if(httpResponse.getStatusLine().getStatusCode() == 404)
                //throw new TopologyDoesNotExistException("Topology "+topologyName+" with version "+version+" does not exist");

            String json_string = EntityUtils.toString(httpResponse.getEntity());
            JSONObject temp1 = new JSONObject(json_string);
            String workspace = temp1.getJSONObject("data").getString("workspace");
            if(workspace.substring(0,4).equals("app:"))
                res=true;

            EntityUtils.consume(httpResponse.getEntity());

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    public void undeployApplication(String deploymentId) throws ConnectionFailedException{
        //GET /rest/deployments/{deploymentId}/undeploy
        ensureConnection();
        try {
            HttpGet getRequest = new HttpGet("/rest/deployments/"+deploymentId+"/undeploy");

            HttpResponse httpResponse = httpclient.execute(target, getRequest, localContext);
            printResponse(httpResponse);
            EntityUtils.consume(httpResponse.getEntity());
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deployApplication(String applicationId,String applicationEnvironmentId) throws ConnectionFailedException{

        ensureConnection();
        try {
            HttpPost postRequest = new HttpPost("/rest/applications/deployment");
            StringEntity entity =new StringEntity("{\"applicationId\":\""+applicationId+"\",\"applicationEnvironmentId\":\""+applicationEnvironmentId+"\"}");
            postRequest.addHeader("content-type", "application/json");
            postRequest.setEntity(entity);
            HttpResponse httpResponse = httpclient.execute(target, postRequest, localContext);

            printResponse(httpResponse);
            EntityUtils.consume(httpResponse.getEntity());
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String  getDeploymentStatus(String deploymentId) throws Exception{
        ensureConnection();
        String status="unknown";
        try {
            HttpGet getRequest = new HttpGet("/rest/deployments/"+deploymentId+"/status");
            HttpResponse httpResponse = httpclient.execute(target, getRequest, localContext);
            printResponse(httpResponse);
            String json_string = EntityUtils.toString(httpResponse.getEntity());
            EntityUtils.consume(httpResponse.getEntity());
            JSONObject temp1 = new JSONObject(json_string);
            if(temp1.isNull("data")){
                throw new Exception(temp1.getString("message"));
            }
            status = temp1.getString("data");
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return status;
    }

    public String getEnvId(String applicationName,String environmentName) throws ApplicationDoesNotExistException,ConnectionFailedException {
        ensureConnection();
        String environmentId = null;

        try {
            //get environment ID from applicationName and applicationName
            HttpPost postRequest = new HttpPost("/rest/applications/" + applicationName + "/environments/search");

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("from", "0"));
            params.add(new BasicNameValuePair("size", "100"));
            StringEntity entity = new StringEntity("{\"from\":0,\"size\":10}");
            postRequest.addHeader("content-type", "application/json");
            postRequest.setEntity(entity);
            HttpResponse httpResponse = httpclient.execute(target, postRequest, localContext);
            printResponse(httpResponse);
            //TODO throws app does not exist
            if(httpResponse.getStatusLine().getStatusCode() ==404){
                throw new ApplicationDoesNotExistException(applicationName);
            }

            //get the one with correct env name :
            String json_string = EntityUtils.toString(httpResponse.getEntity());
            EntityUtils.consume(httpResponse.getEntity());
            JSONObject temp1 = new JSONObject(json_string);
            JSONArray envArray = temp1.getJSONObject("data").getJSONArray("data");
            for (Object env : envArray) {
                //System.out.println(((JSONObject) env).getString("name"));
                if (((JSONObject) env).getString("name").equals(environmentName)) {
                    environmentId = ((JSONObject) env).getString("id");
                    break;
                }
            }
            if (environmentId == null) {
                //TODO throws env does not exist
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return environmentId;
    }

    public String appIsDeployed(String applicationName,String environmentId) throws ConnectionFailedException{
        ensureConnection();
        String deploymentId=null;
        try {
            //HttpGet getRequest = new HttpGet("/rest/applications/"+applicationName+"/environments/"+envId+"/active-deployment");
            HttpGet getRequest = new HttpGet("/rest/deployments?sourceId="+applicationName);
            HttpResponse httpResponse = httpclient.execute(target, getRequest, localContext);
            String json_string = EntityUtils.toString(httpResponse.getEntity());
            EntityUtils.consume(httpResponse.getEntity());
            JSONObject temp1 = new JSONObject(json_string);
            JSONArray envArray = temp1.getJSONArray("data");

            for(Object deployment : envArray){
                if(((JSONObject) deployment).getJSONObject("deployment").getString("environmentId").equals(environmentId)){
                    if(!((JSONObject) deployment).getJSONObject("deployment").has("endDate")){
                        deploymentId=((JSONObject) deployment).getJSONObject("deployment").getString("id");
                        break;
                    }
                }
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return deploymentId;
    }

    private void printResponse(HttpResponse httpResponse){
        try {
            System.out.println("----------------------------------------------------");
            System.out.println(httpResponse.getEntity().getContent());
            System.out.println(httpResponse.getStatusLine().getStatusCode());
            System.out.println(httpResponse.getStatusLine().getReasonPhrase());
            System.out.println("----------------------------------------------------");
           /* LOGGER.info("----------------------------------------------------");
            LOGGER.info(httpResponse.getEntity().getContent());
            LOGGER.info(httpResponse.getStatusLine().getStatusCode());
            LOGGER.info(httpResponse.getStatusLine().getReasonPhrase());
            LOGGER.info("----------------------------------------------------");*/
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
