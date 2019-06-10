package org.jenkinsci.plugins.dependencycheck;

import hudson.FilePath;
import hudson.model.Result;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.junit.Assert.*;

public class DependencyCheckWorkflowTest {

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    /**
     * Run a workflow job using org.jenkinsci.plugins.DependencyCheck.DependencyCheckPublisher and check for success.
     */
    @Test
    public void dependencyCheckPublisherWorkflowStep() throws Exception {
        WorkflowJob job = jenkinsRule.jenkins.createProject(WorkflowJob.class, "dependencyCheckWorkPublisherWorkflowStep");
        FilePath workspace = jenkinsRule.jenkins.getWorkspaceFor(job);
        FilePath report = workspace.child("target").child("dependency-check-report.xml");
        report.copyFrom(DependencyCheckWorkflowTest.class.getResourceAsStream("/org/jenkinsci/plugins/dependencycheck/parser/dependency-check-report.xml"));
        job.setDefinition(new CpsFlowDefinition(""
                        + "node {\n"
                        + "  step([$class: 'DependencyCheckPublisher'])\n"
                        + "}\n", true)
        );
        jenkinsRule.assertBuildStatusSuccess(job.scheduleBuild2(0));
        //TODO
        //DependencyCheckResultAction result = job.getLastBuild().getAction(DependencyCheckResultAction.class);
        //assertTrue(result.getResult().getAnnotations().size() == 2);
    }

    /**
     * Run a workflow job using DependencyCheckPublisher with a failing threshold of 0, so the given example file
     * "/org/jenkinsci/plugins/dependencycheck/parser/dependency-check-report2.xml" will make the build to fail.
     */
    //@Test
    public void dependencyCheckPublisherWorkflowStepSetLimits() throws Exception {
        WorkflowJob job = jenkinsRule.jenkins.createProject(WorkflowJob.class, "dependencyCheckPublisherWorkflowStepSetLimits");
        FilePath workspace = jenkinsRule.jenkins.getWorkspaceFor(job);
        FilePath report = workspace.child("target").child("dependency-check-report.xml");
        report.copyFrom(DependencyCheckWorkflowTest.class.getResourceAsStream("/org/jenkinsci/plugins/dependencycheck/parser/dependency-check-report.xml"));
        job.setDefinition(new CpsFlowDefinition(""
                        + "node {\n"
                        + "  step([$class: 'DependencyCheckPublisher', pattern: '**/dependency-check-report.xml', failedTotalAll: '0', usePreviousBuildAsReference: false])\n"
                        + "}\n", true)
        );
        jenkinsRule.assertBuildStatus(Result.FAILURE, job.scheduleBuild2(0).get());
        //DependencyCheckResultAction result = job.getLastBuild().getAction(DependencyCheckResultAction.class);
        //assertTrue(result.getResult().getAnnotations().size() == 2);
    }

    /**
     * Run a workflow job using DependencyCheckPublisher with a unstable threshold of 0, so the given example file
     * "/org/jenkinsci/plugins/dependencycheck/parser/dependency-check-report2.xml" will make the build to fail.
     */
    //@Test
    public void dependencyCheckPublisherWorkflowStepFailure() throws Exception {
        WorkflowJob job = jenkinsRule.jenkins.createProject(WorkflowJob.class, "dependencyCheckPublisherWorkflowStepFailure");
        FilePath workspace = jenkinsRule.jenkins.getWorkspaceFor(job);
        FilePath report = workspace.child("target").child("dependency-check-report.xml");
        report.copyFrom(DependencyCheckWorkflowTest.class.getResourceAsStream("/org/jenkinsci/plugins/dependencycheck/parser/dependency-check-report.xml"));
        job.setDefinition(new CpsFlowDefinition(""
                        + "node {\n"
                        + "  step([$class: 'DependencyCheckPublisher', pattern: '**/dependency-check-report.xml', unstableTotalAll: '0', usePreviousBuildAsReference: false])\n"
                        + "}\n")
        );
        jenkinsRule.assertBuildStatus(Result.UNSTABLE, job.scheduleBuild2(0).get());
        //DependencyCheckResultAction result = job.getLastBuild().getAction(DependencyCheckResultAction.class);
        //assertTrue(result.getResult().getAnnotations().size() == 2);
    }
}

