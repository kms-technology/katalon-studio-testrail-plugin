package com.katalon.plugin.testrail;

import org.eclipse.jface.preference.PreferencePage;

import com.katalon.platform.api.extension.PluginPreferencePage;

public class TestRailPluginPreferencePage implements PluginPreferencePage {

    @Override
    public String getName() {
        return "TestRail";
    }

    @Override
    public String getPageId() {
        return "com.katalon.plugin.testrail.TestRailPluginPreferencePage";
    }

    @Override
    public Class<? extends PreferencePage> getPreferencePageClass() {
        return TestRailPreferencePage.class;
    }

}
