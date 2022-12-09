package org.jenkinsci.plugins.DependencyCheck.charts;

import java.util.stream.Stream;

import org.jenkinsci.plugins.DependencyCheck.model.Severity;

public enum SeverityThrendChart {
    UNASSIGNED(Severity.UNASSIGNED, "Unassigned", "#c0c0c0"), //
    LOW(Severity.LOW, "Low", "#4cae4c"), //
    MEDIUM(Severity.MEDIUM, "Medium", "#fdc500"), //
    HIGH(Severity.HIGH, "High", "#fd8c00"), //
    CRITICAL(Severity.CRITICAL, "Critical", "#dc0000");

    private final Severity severity;
    private final String lineSeriesName;
    private final String color;

    static SeverityThrendChart forSeverity(Severity severity) {
        return Stream.of(values()) //
                .filter(s -> s.severity == severity) //
                .findFirst() //
                .orElseThrow(() -> new IllegalArgumentException("No thrend chart for severity " + severity));
    }

    private SeverityThrendChart(Severity severity, String lineSeriesName, String color) {
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
