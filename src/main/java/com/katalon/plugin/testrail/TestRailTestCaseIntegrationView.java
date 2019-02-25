package com.katalon.plugin.testrail;

import com.katalon.platform.api.controller.TestCaseController;
import com.katalon.platform.api.extension.TestCaseIntegrationViewDescription;
import com.katalon.platform.api.model.Integration;
import com.katalon.platform.api.model.ProjectEntity;
import com.katalon.platform.api.model.TestCaseEntity;
import com.katalon.platform.api.service.ApplicationManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class TestRailTestCaseIntegrationView implements TestCaseIntegrationViewDescription.TestCaseIntegrationView {

    private Composite container;

    private Text txtId;

    @Override
    public Control onCreateView(Composite parent, TestCaseIntegrationViewDescription.PartActionService partActionService, TestCaseEntity testCase) {

        container = new Composite(parent, SWT.NONE);

        createLabel("ID");
        txtId = createTextbox();

        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.verticalSpacing = 10;
        gridLayout.horizontalSpacing = 15;
        container.setLayout(gridLayout);

//        ProjectEntity project = ApplicationManager.getInstance().getProjectManager().getCurrentProject();
//        Integration integration = new TestRailTestCaseIntegration();
//        ApplicationManager.getInstance().getControllerManager().getController(TestCaseController.class).updateIntegration(project, testCase, integration);

        return container;
    }

    private Text createTextbox() {
        Text text = new Text(container, SWT.BORDER);
        GridData gridData = new GridData(SWT.FILL, SWT.CENTER, false    , false);
        gridData.widthHint = 200;
        text.setLayoutData(gridData);
//        text.addModifyListener(new ModifyListener() {
//            @Override
//            public void modifyText(ModifyEvent modifyEvent) {
//
//            }
//        });
        return text;
    }

    private void createLabel(String text) {
        Label label = new Label(container, SWT.NONE);
        label.setText(text);
        GridData gridData = new GridData(SWT.LEFT, SWT.TOP, false, false);
        label.setLayoutData(gridData);
    }
}
