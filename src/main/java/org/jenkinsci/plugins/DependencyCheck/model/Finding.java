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
import java.util.Objects;

/**
 * Java Bean class for Findings which represent a single pair of Dependency + Vulnerability.
 *
 * @author Steve Springett (steve.springett@owasp.org)
 * @since 5.0.0
 */
public class Finding implements Comparable<Finding>, Serializable {

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

	@Override
	public int compareTo(Finding other) {
		if  (this == other) {
			return 0;
		}
		if (other == null) {
			return -1;
		}
		if (!Objects.equals(dependency, other.dependency)) {
			return dependency.compareTo(other.dependency);
		}
		if (!Objects.equals(vulnerability, other.vulnerability)) {
			return vulnerability.compareTo(other.vulnerability);
		}
		return 0;
	}

}
