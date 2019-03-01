package com.katalon.plugin.testrail;

import com.katalon.platform.api.controller.FolderController;
import com.katalon.platform.api.exception.ResourceException;
import com.katalon.platform.api.extension.DynamicQueryingTestSuiteDescription;
import com.katalon.platform.api.model.*;
import com.katalon.platform.api.preference.PluginPreference;
import com.katalon.platform.api.service.ApplicationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestRailQueryingTestSuite implements DynamicQueryingTestSuiteDescription, TestRailComponent {
    private FolderController folderController = ApplicationManager.getInstance()
            .getControllerManager()
            .getController(FolderController.class);

    @Override
    public String getQueryingType() {
        return "TestRail";
    }

    @Override
    public List<TestCaseEntity> query(ProjectEntity project, TestSuiteEntity testSuiteEntity, String s) throws ResourceException {
        FolderEntity testCaseRoot = folderController.getFolder(project, "Test Cases");
        List<TestCaseEntity> allTestCases = getAllTestCases(project, testCaseRoot);

        Long testRunId = Long.parseLong(s);
        PluginPreference preferences = getPluginStore();
        TestRailConnector connector = new TestRailConnector(
                preferences.getString(TestRailConstants.PREF_TESTRAIL_URL, ""),
                preferences.getString(TestRailConstants.PREF_TESTRAIL_USERNAME, ""),
                preferences.getString(TestRailConstants.PREF_TESTRAIL_PASSWORD, "")
        );
        List<TestCaseEntity> resultTestCases = new ArrayList<>();

        try {
            List<Long> testCaseIdInRun = connector.getTestCaseIdInRun(s);
            allTestCases.forEach(testCaseEntity -> {
                Integration integration = testCaseEntity.getIntegration(TestRailConstants.INTEGRATION_ID);
                if (integration == null) {
                    return;
                }
                Map<String, String> props = integration.getProperties();
                if (props.containsKey(TestRailConstants.INTEGRATION_TESTCASE_ID)) {
                    String testCaseId = props.get(TestRailConstants.INTEGRATION_TESTCASE_ID);
                    if (testCaseIdInRun.contains(Long.parseLong(testCaseId))) {
                        System.out.println("Found testCaseId " + testCaseId);
                        resultTestCases.add(testCaseEntity);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultTestCases;
    }

    private List<TestCaseEntity> getAllTestCases(ProjectEntity project, FolderEntity parentFolder)
            throws ResourceException {
        List<TestCaseEntity> childTestCases = folderController.getChildTestCases(project, parentFolder);

        for (FolderEntity childFolder : folderController.getChildFolders(project, parentFolder)) {
            childTestCases.addAll(getAllTestCases(project, childFolder));
        }
        return childTestCases;
    }
}
