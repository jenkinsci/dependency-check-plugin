/*
 * This file is part of Dependency-Check Jenkins plugin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

    static final String PLUGIN_NAME = "DependencyCheck";

    @Override
    public void start() {
        // Customize some of the default views built into analysis-core
        DependencyCheckDetailBuilder detailBuilder = new DependencyCheckDetailBuilder();
        DetailFactory.addDetailBuilder(DependencyCheckResultAction.class, detailBuilder);
    }

}