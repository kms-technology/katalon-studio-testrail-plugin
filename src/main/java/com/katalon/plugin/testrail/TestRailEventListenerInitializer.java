package com.katalon.plugin.testrail;

import com.katalon.platform.api.controller.TestCaseController;
import com.katalon.platform.api.model.Integration;
import com.katalon.platform.api.model.ProjectEntity;
import com.katalon.platform.api.model.TestCaseEntity;
import com.katalon.platform.api.service.ApplicationManager;
import org.json.simple.JSONObject;
import org.osgi.service.event.Event;

import com.katalon.platform.api.event.EventListener;
import com.katalon.platform.api.event.ExecutionEvent;
import com.katalon.platform.api.execution.TestSuiteExecutionContext;
import com.katalon.platform.api.extension.EventListenerInitializer;
import com.katalon.platform.api.preference.PluginPreference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TestRailEventListenerInitializer implements EventListenerInitializer, TestRailComponent {
    private String getTestRun(String id, TestRailConnector connector) {
        String[] splitText = id.split("/");
        String name = splitText[splitText.length - 1];

        Pattern pattern = Pattern.compile("R(\\d+)");
        Matcher matcher = pattern.matcher(name);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            try {
                System.out.println("Not found ID in " + id);
                System.out.println("Create new test run");
                JSONObject jsonObject = connector.addRun("1", name);
                return ((Long) jsonObject.get("id")).toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    private String mapToStatusTestRail(String ksStatus) {
        String status;
        switch (ksStatus) {
            case "PASSED":
                status = "1"; //PASSED
                break;
            case "FAILED":
                status = "5"; //FAILED
                break;
            case "ERROR":
                status = "5"; //FAILED
                break;
            default:
                status = "0";
        }
        return status;
    }

    @Override
    public void registerListener(EventListener listener) {
        listener.on(Event.class, event -> {
            try {
                PluginPreference preferences = getPluginStore();
                boolean isIntegrationEnabled = preferences.getBoolean(TestRailConstants.PREF_TESTRAIL_ENABLED, false);
                if (!isIntegrationEnabled) {
                    return;
                }
                String authToken = preferences.getString(TestRailConstants.PREF_TESTRAIL_USERNAME, "");

                if (ExecutionEvent.TEST_SUITE_FINISHED_EVENT.equals(event.getTopic())) {
                    ExecutionEvent eventObject = (ExecutionEvent) event.getProperty("org.eclipse.e4.data");

                    TestSuiteExecutionContext testSuiteContext = (TestSuiteExecutionContext) eventObject
                            .getExecutionContext();
                    TestSuiteStatusSummary testSuiteSummary = TestSuiteStatusSummary.of(testSuiteContext);
                    System.out.println("TestRail: Start sending summary message to channel:");
                    System.out.println(
                            "Summary execution result of test suite: " + testSuiteContext.getSourceId()
                                    + "\nTotal test cases: " + Integer.toString(testSuiteSummary.getTotalTestCases())
                                    + "\nTotal passes: " + Integer.toString(testSuiteSummary.getTotalPasses())
                                    + "\nTotal failures: " + Integer.toString(testSuiteSummary.getTotalFailures())
                                    + "\nTotal errors: " + Integer.toString(testSuiteSummary.getTotalErrors())
                                    + "\nTotal skipped: " + Integer.toString(testSuiteSummary.getTotalSkipped()));
                    System.out.println("TestRail: Summary message has been successfully sent");

                    TestRailConnector connector = new TestRailConnector(
                            preferences.getString(TestRailConstants.PREF_TESTRAIL_URL, "https://haimnguyen.testrail.io/"),
                            preferences.getString(TestRailConstants.PREF_TESTRAIL_USERNAME, "haimnguyen@kms-technology.com"),
                            preferences.getString(TestRailConstants.PREF_TESTRAIL_PASSWORD, "gYokVchRRCBXoIFAcVUJ")
                    );

                    String testRunId = getTestRun(testSuiteContext.getSourceId(), connector);
                    System.out.println("Update Test Run with id " + testRunId);

                    ProjectEntity project = ApplicationManager.getInstance().getProjectManager().getCurrentProject();
                    TestCaseController controller = ApplicationManager.getInstance().getControllerManager().getController(TestCaseController.class);

                    List<Map<String, String>> data = testSuiteContext.getTestCaseContexts().stream().map(testCaseExecutionContext -> {
                        String status = mapToStatusTestRail(testCaseExecutionContext.getTestCaseStatus());
                        try {
                            TestCaseEntity testCaseEntity = controller.getTestCase(project, testCaseExecutionContext.getId());
                            Integration integration = testCaseEntity.getIntegration(TestRailConstants.INTEGRATION_ID);
                            if (integration == null) {
                                return null;
                            }
                            String testRailTCId = integration.getProperties().get(TestRailConstants.INTEGRATION_TESTCASE_ID);
                            Map<String, String> resultMap = new HashMap<>();
                            resultMap.put("case_id", testRailTCId);
                            resultMap.put("status_id", status);
                            return resultMap;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    }).filter(map -> map != null).collect(Collectors.toList());
                    System.out.println("#test case: " + data.size());

                    Map<String, List> requestBody = new HashMap<>();
                    requestBody.put("results", data);
                    connector.addMultipleResultForCases(testRunId, requestBody);
                }
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
        });
    }
}
