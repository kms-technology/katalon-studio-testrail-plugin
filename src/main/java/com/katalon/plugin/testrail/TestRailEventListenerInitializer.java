package com.katalon.plugin.testrail;

import com.katalon.platform.api.Application;
import com.katalon.platform.api.controller.TestCaseController;
import com.katalon.platform.api.exception.ResourceException;
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestRailEventListenerInitializer implements EventListenerInitializer, TestRailComponent {
    private String parseId(String text, String patternString) {
        String[] splitText = text.split("/");
        String name = splitText[splitText.length - 1];

        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(name);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            System.out.println("Not found ID in " + text);
            return "";
        }
    }

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

                    connector.connect();
                    String testRunId = getTestRun(testSuiteContext.getSourceId(), connector);
                    System.out.println("Update Test Run with id " + testRunId);

                    ProjectEntity project = ApplicationManager.getInstance().getProjectManager().getCurrentProject();
                    TestCaseController controller = ApplicationManager.getInstance().getControllerManager().getController(TestCaseController.class);

                    testSuiteContext.getTestCaseContexts().forEach(tcContext -> {
                        int status = 0;
                        switch (tcContext.getTestCaseStatus()) {
                            case "PASSED":
                                status = 1;
                                break;
                            case "FAILED":
                                status = 5;
                                break;
                            case "ERROR":
                                status = 5;
                                break;
                            default:
                        }
                        try {
                            TestCaseEntity testCaseEntity = controller.getTestCase(project, tcContext.getId());
                            String testRailTCId = testCaseEntity.getIntegration(TestRailConstants.INTEGRATION_ID)
                                    .getProperties().get(TestRailConstants.TESTRAIL_TC_ID);
                            System.out.println("TestRailId " + testRailTCId);
                        } catch (ResourceException e) {
                            e.printStackTrace();
                        }
                        String testCaseId = parseId(tcContext.getId(), "C(\\d+)");

                        if (!testCaseId.equals("")) {
                            try {
                                JSONObject result = connector.addResultForTestCase(testRunId, testCaseId, status);
                                System.out.println(result);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                    });
                }
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
        });
    }
}
