package com.katalon.plugin.testrail;

import com.katalon.platform.api.controller.FolderController;
import com.katalon.platform.api.exception.ResourceException;
import com.katalon.platform.api.extension.DynamicQueryingTestSuiteDescription;
import com.katalon.platform.api.model.FolderEntity;
import com.katalon.platform.api.model.ProjectEntity;
import com.katalon.platform.api.model.TestCaseEntity;
import com.katalon.platform.api.model.TestSuiteEntity;
import com.katalon.platform.api.service.ApplicationManager;

import java.util.List;

public class TestRailQueryingTestSuite implements DynamicQueryingTestSuiteDescription {
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
        return null;
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
