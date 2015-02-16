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

import hudson.plugins.analysis.util.model.AnnotationsLabelProvider;


public class CustomAnnotationsLabelProvider extends AnnotationsLabelProvider {

    private static final long serialVersionUID = -3412394855904106646L;

    private final String packageLabel;

    public CustomAnnotationsLabelProvider(final String packageLabel) {
        this.packageLabel = packageLabel;
    }

    @Override
    public String getPackages() {
        return packageLabel;
    }

    @Override
    public String getCategories() {
        return Messages.Tab_Categories();
    }

    @Override
    public String getTypes() {
        return Messages.Tab_Types();
    }

    @Override
    public String getHigh() {
        return Messages.Severity_High();
    }

    @Override
    public String getNormal() {
        return Messages.Severity_Medium();
    }

    @Override
    public String getLow() {
        return Messages.Severity_Low();
    }

}
