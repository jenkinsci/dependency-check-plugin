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

import hudson.Plugin;
import hudson.plugins.analysis.views.DetailFactory;

/**
 * Initializes the DependencyCheck plugin
 *
 * @author Steve Springett (steve.springett@owasp.org)
 */
public class DependencyCheckPlugin extends Plugin {

    @Override
    public void start() {
        // Customize some of the default views built into analysis-core
        DependencyCheckDetailBuilder detailBuilder = new DependencyCheckDetailBuilder();
        DetailFactory.addDetailBuilder(DependencyCheckResultAction.class, detailBuilder);
    }

}