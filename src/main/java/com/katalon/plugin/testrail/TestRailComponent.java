package com.katalon.plugin.testrail;

import com.katalon.platform.api.exception.ResourceException;
import com.katalon.platform.api.preference.PluginPreference;
import com.katalon.platform.api.service.ApplicationManager;

public interface TestRailComponent {
    default PluginPreference getPluginStore() throws ResourceException {
        PluginPreference pluginStore = ApplicationManager.getInstance().getPreferenceManager().getPluginPreference(
                ApplicationManager.getInstance().getProjectManager().getCurrentProject().getId(),
                TestRailConstants.PLUGIN_ID);
        return pluginStore;
    }
}
