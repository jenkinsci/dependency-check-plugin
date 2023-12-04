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
package org.jenkinsci.plugins.DependencyCheck.pipeline;

import java.io.Serializable;
import java.util.Set;

import org.jenkinsci.plugins.DependencyCheck.Messages;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import com.google.common.collect.ImmutableSet;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.TaskListener;

public class DependencyCheckStep extends Step implements Serializable {

    private static final long serialVersionUID = -251474850582356300L;

    private String pattern;
    private boolean stopBuild = false;
    private boolean ignoreNoResults = false;
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

    @DataBoundConstructor
    public DependencyCheckStep() {
    }

    /**
     * Returns the Ant file-set pattern of files to work with.
     *
     * @return Ant file-set pattern of files to work with
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * Sets the Ant file-set pattern of files to work with.
     *
     * @param pattern the pattern of files
     */
    @DataBoundSetter
    public void setPattern(final String pattern) {
        this.pattern = pattern;
    }

    @DataBoundSetter
    public void setStopBuild(boolean stopBuild) {
        this.stopBuild = stopBuild;
    }

    public boolean isStopBuild() {
        return stopBuild;
    }

    public boolean isIgnoreNoResults() {
        return ignoreNoResults;
    }

    @DataBoundSetter
    public void setIgnoreNoResults(boolean ignoreNoResults) {
        this.ignoreNoResults = ignoreNoResults;
    }

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new DependencyCheckStepExecutor(this, context);
    }

    public Integer getUnstableTotalCritical() {
        return unstableTotalCritical;
    }

    @DataBoundSetter
    public void setUnstableTotalCritical(Integer unstableTotalCritical) {
        this.unstableTotalCritical = unstableTotalCritical;
    }

    public Integer getUnstableTotalHigh() {
        return unstableTotalHigh;
    }

    @DataBoundSetter
    public void setUnstableTotalHigh(Integer unstableTotalHigh) {
        this.unstableTotalHigh = unstableTotalHigh;
    }

    public Integer getUnstableTotalMedium() {
        return unstableTotalMedium;
    }

    @DataBoundSetter
    public void setUnstableTotalMedium(Integer unstableTotalMedium) {
        this.unstableTotalMedium = unstableTotalMedium;
    }

    public Integer getUnstableTotalLow() {
        return unstableTotalLow;
    }

    @DataBoundSetter
    public void setUnstableTotalLow(Integer unstableTotalLow) {
        this.unstableTotalLow = unstableTotalLow;
    }

    public Integer getFailedTotalCritical() {
        return failedTotalCritical;
    }

    @DataBoundSetter
    public void setFailedTotalCritical(Integer failedTotalCritical) {
        this.failedTotalCritical = failedTotalCritical;
    }

    public Integer getFailedTotalHigh() {
        return failedTotalHigh;
    }

    @DataBoundSetter
    public void setFailedTotalHigh(Integer failedTotalHigh) {
        this.failedTotalHigh = failedTotalHigh;
    }

    public Integer getFailedTotalMedium() {
        return failedTotalMedium;
    }

    @DataBoundSetter
    public void setFailedTotalMedium(Integer failedTotalMedium) {
        this.failedTotalMedium = failedTotalMedium;
    }

    public Integer getFailedTotalLow() {
        return failedTotalLow;
    }

    @DataBoundSetter
    public void setFailedTotalLow(Integer failedTotalLow) {
        this.failedTotalLow = failedTotalLow;
    }

    public boolean isTotalThresholdAnalysisExploitable() {
        return totalThresholdAnalysisExploitable;
    }

    @DataBoundSetter
    public void setTotalThresholdAnalysisExploitable(boolean totalThresholdAnalysisExploitable) {
        this.totalThresholdAnalysisExploitable = totalThresholdAnalysisExploitable;
    }

    public Integer getUnstableNewCritical() {
        return unstableNewCritical;
    }

    @DataBoundSetter
    public void setUnstableNewCritical(Integer unstableNewCritical) {
        this.unstableNewCritical = unstableNewCritical;
    }

    public Integer getUnstableNewHigh() {
        return unstableNewHigh;
    }

    @DataBoundSetter
    public void setUnstableNewHigh(Integer unstableNewHigh) {
        this.unstableNewHigh = unstableNewHigh;
    }

    public Integer getUnstableNewMedium() {
        return unstableNewMedium;
    }

    @DataBoundSetter
    public void setUnstableNewMedium(Integer unstableNewMedium) {
        this.unstableNewMedium = unstableNewMedium;
    }

    public Integer getUnstableNewLow() {
        return unstableNewLow;
    }

    @DataBoundSetter
    public void setUnstableNewLow(Integer unstableNewLow) {
        this.unstableNewLow = unstableNewLow;
    }

    public Integer getFailedNewCritical() {
        return failedNewCritical;
    }

    @DataBoundSetter
    public void setFailedNewCritical(Integer failedNewCritical) {
        this.failedNewCritical = failedNewCritical;
    }

    public Integer getFailedNewHigh() {
        return failedNewHigh;
    }

    @DataBoundSetter
    public void setFailedNewHigh(Integer failedNewHigh) {
        this.failedNewHigh = failedNewHigh;
    }

    public Integer getFailedNewMedium() {
        return failedNewMedium;
    }

    @DataBoundSetter
    public void setFailedNewMedium(Integer failedNewMedium) {
        this.failedNewMedium = failedNewMedium;
    }

    public Integer getFailedNewLow() {
        return failedNewLow;
    }

    @DataBoundSetter
    public void setFailedNewLow(Integer failedNewLow) {
        this.failedNewLow = failedNewLow;
    }

    public boolean isNewThresholdAnalysisExploitable() {
        return newThresholdAnalysisExploitable;
    }

    @DataBoundSetter
    public void setNewThresholdAnalysisExploitable(boolean newThresholdAnalysisExploitable) {
        this.newThresholdAnalysisExploitable = newThresholdAnalysisExploitable;
    }

    @Extension
    public static final class DescriptorImpl extends StepDescriptor {

        /**
         * This name is used on the build configuration screen.
         */
        @Override
        public String getDisplayName() {
            return Messages.Publisher_Name();
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return ImmutableSet.of(FilePath.class, FlowNode.class, TaskListener.class, Launcher.class);
        }

        @Override
        public String getFunctionName() {
            return "dependencyCheckPublisher";
        }

    }
}
