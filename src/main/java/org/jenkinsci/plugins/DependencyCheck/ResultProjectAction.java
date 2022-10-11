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

import hudson.model.Action;
import hudson.model.Job;

/**
 * Action attached to project to link latest dependency-check report.
 *
 * @author Nikolas Falco
 * @since 5.1.3
 */
public class ResultProjectAction implements Action {

    /**
     * Project that owns this action.
     */
    public final Job<?,?> job;

    public ResultProjectAction(Job<?,?> job) {
        this.job = job;
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return "Dependency-Check";
    }

    @Override
    public String getUrlName() {
        return null;
    }

}