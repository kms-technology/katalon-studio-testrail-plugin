package com.katalon.plugin.testrail;

import com.gurock.testrail.APIClient;
import com.gurock.testrail.APIException;

import org.apache.http.MethodNotSupportedException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.util.*;

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

    public void setPassword(String password) {
        this.password = password;
    }

    public TestRailConnector(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;

        this.apiClient = new APIClient(this.url);
        apiClient.setUser(this.username);
        apiClient.setPassword(this.password);
    }

    private Object sendPost(String url, Map<String, Object> data) throws IOException, APIException, URISyntaxException,
            KeyManagementException, MethodNotSupportedException, GeneralSecurityException {
        System.out.println("Send post url: " + url + " data: " + data);
        Object response = this.apiClient.sendPost(url, data);
        System.out.println("Receive: " + response.toString());
        return response;
    }

    private Object sendGet(String url) throws IOException, APIException, URISyntaxException, KeyManagementException,
            MethodNotSupportedException, GeneralSecurityException {
        System.out.println("Send get url " + url);
        Object response = this.apiClient.sendGet(url);
        System.out.println("Receive: " + response.toString());
        return response;
    }

    public JSONObject getProject(String projectId) throws IOException, APIException, URISyntaxException,
            KeyManagementException, MethodNotSupportedException, GeneralSecurityException {
        return (JSONObject) sendGet("get_project/" + projectId);
    }

    public JSONArray getTest(String id) throws IOException, APIException, URISyntaxException, KeyManagementException,
            MethodNotSupportedException, GeneralSecurityException {
        return (JSONArray) sendGet("get_tests/" + id);
    }

    public List<Long> getTestCaseIdInRun(String id) throws IOException, APIException, URISyntaxException,
            KeyManagementException, MethodNotSupportedException, GeneralSecurityException {
        String requestURL = "get_tests/" + id;
        JSONArray jsonArray = (JSONArray) sendGet(requestURL);

        List<Long> listId = new ArrayList<>();

        jsonArray.forEach((o) -> {
            JSONObject jsonObject = (JSONObject) o;
            listId.add((Long) jsonObject.get("case_id"));
        });
        return listId;
    }

    public JSONObject addResultForTestCase(String testRunId, String testCaseId, int status)
            throws IOException, APIException, URISyntaxException, KeyManagementException, MethodNotSupportedException,
            GeneralSecurityException {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("status_id", status);
        List<Long> listId = getTestCaseIdInRun(testRunId);
        Long tcId = Long.parseLong(testCaseId);

        if (!listId.contains(tcId)) {
            // add test case to test run before sending result
            listId.add(tcId);
            String update_run_url = "update_run/" + testRunId;
            Map<String, Object> body = new HashMap<String, Object>();
            body.put("include_all", false);
            body.put("case_ids", listId);
            System.out.println(body);
            sendPost(update_run_url, body);
        }

        String add_result_url = String.format("add_result_for_case/%s/%s", testRunId, testCaseId);
        return (JSONObject) sendPost(add_result_url, data);
    }

    public JSONObject updateRun(String testRunId, Map<String, Object> body) throws IOException, APIException, URISyntaxException,
            KeyManagementException, MethodNotSupportedException, GeneralSecurityException {
        String update_run_url = "update_run/" + testRunId;
        return (JSONObject) sendPost(update_run_url, body);
    }

    public JSONArray addMultipleResultForCases(String testRunId, Map<String, Object> body) throws IOException, APIException,
            URISyntaxException, KeyManagementException, MethodNotSupportedException, GeneralSecurityException {
        String add_result_url = String.format("add_results_for_cases/%s", testRunId);
        return (JSONArray) sendPost(add_result_url, body);
    }

    public JSONObject addRun(String projectId, String suiteId, String name, List<Long> testCaseIds)
            throws IOException, APIException, URISyntaxException, KeyManagementException, MethodNotSupportedException,
            GeneralSecurityException {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("suite_id", Long.parseLong(suiteId));
        data.put("name", name);
        data.put("include_all", false);
        data.put("case_ids", testCaseIds);
        String requestURL = String.format("add_run/%s", projectId);
        return (JSONObject) sendPost(requestURL, data);

    }


}
