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

/**
 * Ported from the Dependency-Track Jenkins plugin.
 *
 * @author Steve Springett (steve.springett@owasp.org)
 * @since 5.0.0
 */
public enum Severity {
    CRITICAL,
    HIGH,
    MEDIUM,
    LOW,
    INFO,
    UNASSIGNED;

    public static Severity normalize(String severity) {
        if (severity == null) {
            return Severity.UNASSIGNED;
        }
        return switch (severity.toUpperCase()) {
        case "CRITICAL" -> Severity.CRITICAL;
        case "HIGH" -> Severity.HIGH;
        case "MEDIUM" -> Severity.MEDIUM;
        case "MODERATE" -> Severity.MEDIUM;
        case "LOW" -> Severity.LOW;
        default -> Severity.UNASSIGNED;
        };
    }
}