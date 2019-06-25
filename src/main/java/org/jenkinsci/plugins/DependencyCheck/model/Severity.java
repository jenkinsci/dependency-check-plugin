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
 * Ported from the Dependency-Track Jenkins plugin
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
        switch (severity.toUpperCase()) {
            case "CRITICAL":
                return Severity.CRITICAL;
            case "HIGH":
                return Severity.HIGH;
            case "MEDIUM":
                return Severity.MEDIUM;
            case "MODERATE":
                return Severity.MEDIUM;
            case "LOW":
                return Severity.LOW;
            default:
                return Severity.UNASSIGNED;
        }
    }
}