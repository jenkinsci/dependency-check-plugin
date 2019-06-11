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
package org.jenkinsci.plugins.DependencyCheck.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Java Bean class for dependencies found by DependencyCheck.
 *
 * @author Steve Springett (steve.springett@owasp.org)
 */
public class Analysis {

    /**
     * All dependencies as the result of an analysis
     */
    private final List<Dependency> dependencies = new ArrayList<>();

    /**
     * Adds a new dependency to this collection.
     *
     * @param dependency the dependency to add
     */
    public void addDependency(final Dependency dependency) {
        dependencies.add(dependency);
    }

    /**
     * Returns a read-only collection of all dependencies from the analysis.
     *
     * @return all dependencies from the analysis
     */
    public Collection<Dependency> getDependencies() {
        return Collections.unmodifiableCollection(dependencies);
    }
}
