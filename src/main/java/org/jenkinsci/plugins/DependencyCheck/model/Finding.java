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

import java.io.Serializable;

/**
 * Java Bean class for Findings which represent a single pair of Dependency + Vulnerability.
 *
 * @author Steve Springett (steve.springett@owasp.org)
 * @since 5.0.0
 */
public class Finding implements Serializable {

    private static final long serialVersionUID = 2916981097517354202L;

    private final Dependency dependency;
    private final Vulnerability vulnerability;

    public Finding(Dependency dependency, Vulnerability vulnerability) {
        this.dependency = dependency;
        this.vulnerability = vulnerability;
    }

    public Dependency getDependency() {
        return dependency;
    }

    public Vulnerability getVulnerability() {
        return vulnerability;
    }
    
    public Severity getNormalizedSeverity() {
        return Severity.normalize(vulnerability.getSeverity());
    }
}
