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
package org.jenkinsci.plugins.DependencyCheck.charts;

import java.util.stream.Stream;

import org.jenkinsci.plugins.DependencyCheck.model.Severity;

public enum SeverityThrendChart {
    UNASSIGNED(Severity.UNASSIGNED, "Unassigned", "#c0c0c0"), //
    LOW(Severity.LOW, "Low", "#4cae4c"), //
    MEDIUM(Severity.MEDIUM, "Medium", "#fdc500"), //
    HIGH(Severity.HIGH, "High", "#fd8c00"), //
    CRITICAL(Severity.CRITICAL, "Critical", "#dc0000");

    static SeverityThrendChart forSeverity(Severity severity) {
        return Stream.of(values()) //
                .filter(s -> s.severity == severity) //
                .findFirst() //
                .orElseThrow(() -> new IllegalArgumentException("No thrend chart for severity " + severity));
    }

    final Severity severity;
    final String lineSeriesName;
    final String color;

    SeverityThrendChart(Severity severity, String lineSeriesName, String color) {
        this.severity = severity;
        this.lineSeriesName = lineSeriesName;
        this.color = color;
    }

    public String getLineSeriesName() {
        return lineSeriesName;
    }

    public String getColor() {
        return color;
    }

}
