package com.katalon.plugin.testrail;

import com.katalon.platform.api.controller.FolderController;
import com.katalon.platform.api.exception.ResourceException;
import com.katalon.platform.api.extension.DynamicQueryingTestSuiteDescription;
import com.katalon.platform.api.model.FolderEntity;
import com.katalon.platform.api.model.ProjectEntity;
import com.katalon.platform.api.model.TestCaseEntity;
import com.katalon.platform.api.model.TestSuiteEntity;
import com.katalon.platform.api.preference.PluginPreference;
import com.katalon.platform.api.service.ApplicationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestRailQueryingTestSuite implements DynamicQueryingTestSuiteDescription, TestRailComponent {
    private FolderController folderController = ApplicationManager.getInstance()
            .getControllerManager()
            .getController(FolderController.class);

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
                preferences.getString(TestRailConstants.PREF_TESTRAIL_URL, "https://haimnguyen.testrail.io/"),
                preferences.getString(TestRailConstants.PREF_TESTRAIL_USERNAME, "haimnguyen@kms-technology.com"),
                preferences.getString(TestRailConstants.PREF_TESTRAIL_PASSWORD, "gYokVchRRCBXoIFAcVUJ")
        );
        List<TestCaseEntity> resultTestCases = new ArrayList<>();

        try {
            connector.connect();
            List<Long> listId = connector.getTestCaseIdInRun(testRunId.toString());
            allTestCases.forEach(testCaseEntity -> {
                String id = testCaseEntity.getId();
                String testCaseId = parseId(id, "C(\\d+)");
                if (!testCaseId.equals("") && listId.contains(Long.parseLong(testCaseId))){
                    resultTestCases.add(testCaseEntity);
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
