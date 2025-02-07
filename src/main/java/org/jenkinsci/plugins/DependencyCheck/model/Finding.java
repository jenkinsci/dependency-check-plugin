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

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Java Bean class for Findings which represent a single pair of Dependency + Vulnerability.
 *
 * @author Steve Springett (steve.springett@owasp.org)
 * @since 5.0.0
 */
public class Finding implements Serializable {

    @Serial
    private static final long serialVersionUID = 2916981097517354202L;

    private int count;
    private final Dependency dependency;
    private final Vulnerability vulnerability;

    public Finding(Dependency dependency, Vulnerability vulnerability) {
        this.dependency = dependency;
        this.vulnerability = vulnerability;
        this.count = 1;
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

    public void increaseCount() {
        count += 1;
    }

    public int getCount() {
        return count;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dependency, vulnerability);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Finding other = (Finding) obj;
        return Objects.equals(dependency, other.dependency) && Objects.equals(vulnerability, other.vulnerability);
    }

}
