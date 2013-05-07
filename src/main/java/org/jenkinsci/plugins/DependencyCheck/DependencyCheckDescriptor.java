/*
 * This file is part of DependencyCheck Jenkins plugin.
 *
 * DependencyCheck is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * DependencyCheck is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * DependencyCheck. If not, see http://www.gnu.org/licenses/.
 */
package org.jenkinsci.plugins.DependencyCheck;

import hudson.Extension;
import hudson.plugins.analysis.core.PluginDescriptor;

/**
 * Descriptor for the class {@link DependencyCheckPublisher}. Used as a singleton. The
 * class is marked as public so that it can be accessed from views.
 *
 * @author Steve Springett, based on PmdDescriptor by Ulli Hafner
 */
@Extension(ordinal = 100)
public final class DependencyCheckDescriptor extends PluginDescriptor
{
    private static final String ICONS_PREFIX = "/plugin/dependency-check/icons/";

    // The ID of this plug-in is used as URL.
    static final String PLUGIN_ID = "dependency-check";

    // The URL of the result action.
    static final String RESULT_URL = PluginDescriptor.createResultUrlName(PLUGIN_ID);

    // Icon to use for the result and project action.
    static final String ICON_URL = ICONS_PREFIX + "dependency-check-24x24.png";

    /**
     * Creates a new instance of {@link DependencyCheckDescriptor}.
     */
    public DependencyCheckDescriptor()
    {
        super(DependencyCheckPublisher.class);
    }

    @Override
    public String getDisplayName()
    {
        return Messages.Publisher_Name();
    }

    @Override
    public String getPluginName()
    {
        return PLUGIN_ID;
    }

    @Override
    public String getIconUrl()
    {
        return ICON_URL;
    }

    @Override
    public String getSummaryIconUrl()
    {
        return ICONS_PREFIX + "dependency-check-48x48.png";
    }

}