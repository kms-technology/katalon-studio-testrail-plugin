package com.katalon.plugin.testrail;

import com.katalon.platform.api.exception.ResourceException;
import com.katalon.platform.api.extension.TestCaseIntegrationViewDescription;
import com.katalon.platform.api.model.ProjectEntity;
import com.katalon.platform.api.preference.PluginPreference;
import com.katalon.platform.api.service.ApplicationManager;

public class TestRailTestCaseIntegrationViewDescription implements TestCaseIntegrationViewDescription {

    @Override
    public String getName() {
        return "TestRail";
    }

    @Override
    public Class<? extends TestCaseIntegrationView> getTestCaseIntegrationView() {
        return TestRailTestCaseIntegrationView.class;
    }

    @Override
    public boolean isEnabled(ProjectEntity projectEntity) {
        try {
            PluginPreference pluginPreference = ApplicationManager.getInstance()
                    .getPreferenceManager()
                    .getPluginPreference(projectEntity.getId(), TestRailConstants.PLUGIN_ID);
            if (pluginPreference == null) {
                return false;
            }
            return pluginPreference.getBoolean(TestRailConstants.PREF_TESTRAIL_ENABLED, false);
        } catch (ResourceException e) {
            return false;
        }
    }
}
