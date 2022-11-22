package org.jenkinsci.plugins.DependencyCheck.charts;

import java.util.LinkedHashMap;
import java.util.Map;

import org.jenkinsci.plugins.DependencyCheck.ResultAction;
import org.jenkinsci.plugins.DependencyCheck.model.SeverityDistribution;

import edu.hm.hafner.echarts.SeriesBuilder;

public class SeverityDistributionSeriesBuilder extends SeriesBuilder<ResultAction> {

    @Override
    protected Map<String, Integer> computeSeries(ResultAction currentAction) {
        Map<String, Integer> series = new LinkedHashMap<>();

        SeverityDistribution severityDistribution = currentAction.getSeverityDistribution();
        if (severityDistribution == null) {
            severityDistribution = new SeverityDistribution(currentAction.getOwner().getNumber());
        }
        series.put(SeverityThrendChart.CRITICAL.getLineSeriesName(), severityDistribution.getCritical());
        series.put(SeverityThrendChart.HIGH.getLineSeriesName(), severityDistribution.getHigh());
        series.put(SeverityThrendChart.MEDIUM.getLineSeriesName(), severityDistribution.getMedium());
        series.put(SeverityThrendChart.LOW.getLineSeriesName(), severityDistribution.getLow());
        series.put(SeverityThrendChart.UNASSIGNED.getLineSeriesName(), severityDistribution.getUnassigned());
        return series;
    }

}
