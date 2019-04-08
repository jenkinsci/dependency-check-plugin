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

import hudson.tasks.Recorder;
import org.jenkinsci.plugins.DependencyCheck.model.Thresholds;
import org.kohsuke.stapler.DataBoundSetter;
import java.io.Serializable;

/**
 * Ported from the Dependency-Track Jenkins plugin
 */
@SuppressWarnings("unused")
public abstract class ThresholdCapablePublisher extends Recorder implements Serializable {

    private static final long serialVersionUID = 5849869400487825164L;

    private Integer unstableTotalCritical;
    private Integer unstableTotalHigh;
    private Integer unstableTotalMedium;
    private Integer unstableTotalLow;
    private Integer failedTotalCritical;
    private Integer failedTotalHigh;
    private Integer failedTotalMedium;
    private Integer failedTotalLow;
    private boolean totalThresholdAnalysisExploitable;

    private Integer unstableNewCritical;
    private Integer unstableNewHigh;
    private Integer unstableNewMedium;
    private Integer unstableNewLow;
    private Integer failedNewCritical;
    private Integer failedNewHigh;
    private Integer failedNewMedium;
    private Integer failedNewLow;
    private boolean newThresholdAnalysisExploitable;

    Thresholds getThresholds() {
        final Thresholds thresholds = new Thresholds();
        thresholds.totalFindings.unstableCritical = unstableTotalCritical;
        thresholds.totalFindings.unstableHigh = unstableTotalHigh;
        thresholds.totalFindings.unstableMedium = unstableTotalMedium;
        thresholds.totalFindings.unstableLow = unstableTotalLow;
        thresholds.totalFindings.failedCritical = failedTotalCritical;
        thresholds.totalFindings.failedHigh = failedTotalHigh;
        thresholds.totalFindings.failedMedium = failedTotalMedium;
        thresholds.totalFindings.failedLow = failedTotalLow;
        thresholds.totalFindings.limitToAnalysisExploitable = totalThresholdAnalysisExploitable;

        thresholds.newFindings.unstableCritical = unstableNewCritical;
        thresholds.newFindings.unstableHigh = unstableNewHigh;
        thresholds.newFindings.unstableMedium = unstableNewMedium;
        thresholds.newFindings.unstableLow = unstableNewLow;
        thresholds.newFindings.failedCritical = failedNewCritical;
        thresholds.newFindings.failedHigh = failedNewHigh;
        thresholds.newFindings.failedMedium = failedNewMedium;
        thresholds.newFindings.failedLow = failedNewLow;
        thresholds.newFindings.limitToAnalysisExploitable = newThresholdAnalysisExploitable;
        return thresholds;
    }


    public Integer getUnstableTotalCritical() {
        return unstableTotalCritical;
    }

    @DataBoundSetter
    public void setUnstableTotalCritical(final Integer unstableTotalCritical) {
        this.unstableTotalCritical = unstableTotalCritical;
    }

    public Integer getUnstableTotalHigh() {
        return unstableTotalHigh;
    }

    @DataBoundSetter
    public void setUnstableTotalHigh(final Integer unstableTotalHigh) {
        this.unstableTotalHigh = unstableTotalHigh;
    }

    public Integer getUnstableTotalMedium() {
        return unstableTotalMedium;
    }

    @DataBoundSetter
    public void setUnstableTotalMedium(final Integer unstableTotalMedium) {
        this.unstableTotalMedium = unstableTotalMedium;
    }

    public Integer getUnstableTotalLow() {
        return unstableTotalLow;
    }

    @DataBoundSetter
    public void setUnstableTotalLow(final Integer unstableTotalLow) {
        this.unstableTotalLow = unstableTotalLow;
    }

    public Integer getFailedTotalCritical() {
        return failedTotalCritical;
    }

    @DataBoundSetter
    public void setFailedTotalCritical(final Integer failedTotalCritical) {
        this.failedTotalCritical = failedTotalCritical;
    }

    public Integer getFailedTotalHigh() {
        return failedTotalHigh;
    }

    @DataBoundSetter
    public void setFailedTotalHigh(final Integer failedTotalHigh) {
        this.failedTotalHigh = failedTotalHigh;
    }

    public Integer getFailedTotalMedium() {
        return failedTotalMedium;
    }

    @DataBoundSetter
    public void setFailedTotalMedium(final Integer failedTotalMedium) {
        this.failedTotalMedium = failedTotalMedium;
    }

    public Integer getFailedTotalLow() {
        return failedTotalLow;
    }

    @DataBoundSetter
    public void setFailedTotalLow(final Integer failedTotalLow) {
        this.failedTotalLow = failedTotalLow;
    }

    public boolean getTotalThresholdAnalysisExploitable() {
        return totalThresholdAnalysisExploitable;
    }

    @DataBoundSetter
    public void setTotalThresholdAnalysisExploitable(final boolean totalThresholdAnalysisExploitable) {
        this.totalThresholdAnalysisExploitable = totalThresholdAnalysisExploitable;
    }

    public Integer getUnstableNewCritical() {
        return unstableNewCritical;
    }

    @DataBoundSetter
    public void setUnstableNewCritical(final Integer unstableNewCritical) {
        this.unstableNewCritical = unstableNewCritical;
    }

    public Integer getUnstableNewHigh() {
        return unstableNewHigh;
    }

    @DataBoundSetter
    public void setUnstableNewHigh(final Integer unstableNewHigh) {
        this.unstableNewHigh = unstableNewHigh;
    }

    public Integer getUnstableNewMedium() {
        return unstableNewMedium;
    }

    @DataBoundSetter
    public void setUnstableNewMedium(final Integer unstableNewMedium) {
        this.unstableNewMedium = unstableNewMedium;
    }

    public Integer getUnstableNewLow() {
        return unstableNewLow;
    }

    @DataBoundSetter
    public void setUnstableNewLow(final Integer unstableNewLow) {
        this.unstableNewLow = unstableNewLow;
    }

    public Integer getFailedNewCritical() {
        return failedNewCritical;
    }

    @DataBoundSetter
    public void setFailedNewCritical(final Integer failedNewCritical) {
        this.failedNewCritical = failedNewCritical;
    }

    public Integer getFailedNewHigh() {
        return failedNewHigh;
    }

    @DataBoundSetter
    public void setFailedNewHigh(final Integer failedNewHigh) {
        this.failedNewHigh = failedNewHigh;
    }

    public Integer getFailedNewMedium() {
        return failedNewMedium;
    }

    @DataBoundSetter
    public void setFailedNewMedium(final Integer failedNewMedium) {
        this.failedNewMedium = failedNewMedium;
    }

    public Integer getFailedNewLow() {
        return failedNewLow;
    }

    @DataBoundSetter
    public void setFailedNewLow(final Integer failedNewLow) {
        this.failedNewLow = failedNewLow;
    }

    public boolean getNewThresholdAnalysisExploitable() {
        return newThresholdAnalysisExploitable;
    }

    @DataBoundSetter
    public void setNewThresholdAnalysisExploitable(final boolean newThresholdAnalysisExploitable) {
        this.newThresholdAnalysisExploitable = newThresholdAnalysisExploitable;
    }
}
