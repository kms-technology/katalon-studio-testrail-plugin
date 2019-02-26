package com.katalon.plugin.testrail;

import com.gurock.testrail.APIClient;
import com.gurock.testrail.APIException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TestRailConnector {
    private String url;
    private String username;
    private String password;

    private APIClient apiClient;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public TestRailConnector(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public JSONArray connect() throws IOException, APIException {
        this.apiClient = new APIClient(this.url);
        apiClient.setUser(this.username);
        apiClient.setPassword(this.password);
        System.out.println(this.url);
        System.out.println(this.username);
        System.out.println(this.password);

        return (JSONArray) this.apiClient.sendGet("get_projects");
    }

    public JSONArray getTest(String id) throws IOException, APIException {
        return (JSONArray) this.apiClient.sendGet("get_tests/" + id);
    }

    public void updateRun(String id, String array) throws IOException, APIException {
        String postURL = "update_run" + id;
        String body = "{\"include_all\": false,\"case_ids\": " + array + "}";

        this.apiClient.sendPost(postURL, body);
    }

    public JSONObject addResultForTestCase(String testRunId, String testCaseId, int status) throws IOException, APIException {
        Map data = new HashMap();
        data.put("status_id", status);
        String requestURL = String.format("add_result_for_case/%s/%s", testRunId, testCaseId);
        return (JSONObject) this.apiClient.sendPost(requestURL, data);
    }

    public JSONObject addRun(String projectId) throws IOException, APIException {
        Map data = new HashMap();
        data.put("suite_id", 1);
        data.put("name", "New Run");
        String requestURL = String.format("add_run/%s", projectId);
        return (JSONObject) this.apiClient.sendPost(requestURL, data);

    }


}
