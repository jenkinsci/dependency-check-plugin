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

import org.jenkinsci.plugins.DependencyCheck.DependencyCheckPublisher;
import org.jenkinsci.plugins.DependencyCheck.Messages;
import org.jenkinsci.plugins.workflow.actions.WarningAction;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.AbortException;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;

public class DependencyCheckStepExecutor extends SynchronousNonBlockingStepExecution<Void> {

    private static final long serialVersionUID = -8209320657657318589L;

    private DependencyCheckStep step;

    protected DependencyCheckStepExecutor(@NonNull DependencyCheckStep step, @NonNull StepContext context) {
        super(context);
        this.step = step;
    }

    @Override
    protected Void run() throws Exception {
        FilePath workspace = getContext().get(FilePath.class);
        workspace.mkdirs();
        Run<?,?> run = getContext().get(Run.class);
        TaskListener listener = getContext().get(TaskListener.class);
        FlowNode node = getContext().get(FlowNode.class);
        Launcher launcher = getContext().get(Launcher.class);

        DependencyCheckPublisher publisher = new DependencyCheckPublisher();
        publisher.setPattern(step.getPattern());
        publisher.setStopBuild(step.isStopBuild());

        publisher.setTotalThresholdAnalysisExploitable(step.isTotalThresholdAnalysisExploitable());
        publisher.setFailedTotalCritical(step.getFailedTotalCritical());
        publisher.setFailedTotalHigh(step.getFailedTotalHigh());
        publisher.setFailedTotalMedium(step.getFailedTotalMedium());
        publisher.setFailedTotalLow(step.getFailedTotalLow());
        publisher.setUnstableTotalCritical(step.getUnstableTotalCritical());
        publisher.setUnstableTotalHigh(step.getUnstableTotalHigh());
        publisher.setUnstableTotalMedium(step.getUnstableTotalMedium());
        publisher.setUnstableTotalLow(step.getUnstableTotalLow());

        publisher.setNewThresholdAnalysisExploitable(step.isNewThresholdAnalysisExploitable());
        publisher.setFailedNewCritical(step.getFailedNewCritical());
        publisher.setFailedNewHigh(step.getFailedNewHigh());
        publisher.setFailedNewMedium(step.getFailedNewMedium());
        publisher.setFailedNewLow(step.getFailedNewLow());
        publisher.setUnstableNewCritical(step.getUnstableNewCritical());
        publisher.setUnstableNewHigh(step.getUnstableNewHigh());
        publisher.setUnstableNewMedium(step.getUnstableNewMedium());
        publisher.setUnstableNewLow(step.getUnstableNewLow());

        Result result = publisher.process(run, workspace, launcher, listener);
        if (result.isWorseThan(Result.SUCCESS)) {
            node.addOrReplaceAction(new WarningAction(result).withMessage(Messages.Publisher_Threshold_Exceed()));
            run.setResult(result);
        }
        if (Result.FAILURE == result && step.isStopBuild()) {
            throw new AbortException(Messages.Publisher_Threshold_Exceed());
        }

        return null;
    }
}
