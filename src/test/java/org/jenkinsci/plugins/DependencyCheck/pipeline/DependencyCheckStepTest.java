package org.jenkinsci.plugins.DependencyCheck.pipeline;

import static org.assertj.core.api.Assertions.*;
import org.jenkinsci.plugins.DependencyCheck.ResultAction;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import hudson.FilePath;
import hudson.model.Result;

public class DependencyCheckStepTest {

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    private WorkflowJob getBaseJob(String jobName) throws Exception {
        WorkflowJob job = jenkinsRule.jenkins.createProject(WorkflowJob.class, jobName);
        FilePath workspace = jenkinsRule.jenkins.getWorkspaceFor(job);
        FilePath report = workspace.child("target").child("dependency-check-report.xml");
        report.copyFrom(DependencyCheckStepTest.class.getResourceAsStream("/org/jenkinsci/plugins/DependencyCheck/parser/dependency-check-report.xml"));
        return job;
    }

    /*
     * Run a workflow job using org.jenkinsci.plugins.DependencyCheck.DependencyCheckPublisher and check for success.
     */
    @Test
    public void no_configuration() throws Exception {
        WorkflowJob job = getBaseJob("dependencyCheckWorkPublisherWorkflowStep");
        job.setDefinition(new CpsFlowDefinition(""
                        + "node {\n"
                        + "  dependencyCheckPublisher()\n"
                        + "}\n", true)
        );
        jenkinsRule.assertBuildStatusSuccess(job.scheduleBuild2(0));
    }

    /*
     * Run a workflow job using DependencyCheckPublisher with a failing threshold of 0, so the given example file
     * "/org/jenkinsci/plugins/DependencyCheck/parser/dependency-check-report2.xml" will make the build to fail.
     */
    @Test
    public void fail_on_total_high() throws Exception {
        WorkflowJob job = getBaseJob("dependencyCheckPublisherWorkflowStepSetLimits");
        job.setDefinition(new CpsFlowDefinition(""
                        + "node {\n"
                        + "  dependencyCheckPublisher(pattern: '**/dependency-check-report.xml', failedTotalHigh: 0)\n"
                        + "}\n", true)
        );
        jenkinsRule.assertBuildStatus(Result.FAILURE, job.scheduleBuild2(0).get());
        ResultAction result = job.getLastBuild().getAction(ResultAction.class);
        assertThat(result.getSeverityDistribution().getHigh()).isPositive();
    }

    /*
     * Run a workflow job using DependencyCheckPublisher with a unstable threshold of 0, so the given example file
     * "/org/jenkinsci/plugins/DependencyCheck/parser/dependency-check-report2.xml" will make the build to fail.
     */
    @Test
    public void unstable_on_total_high() throws Exception {
        WorkflowJob job = getBaseJob("dependencyCheckPublisherWorkflowStepFailure");
        job.setDefinition(new CpsFlowDefinition(""
                        + "node {\n"
                        + "  dependencyCheckPublisher(pattern: '**/dependency-check-report.xml', unstableTotalHigh: 0)\n"
                        + "}\n", true)
        );
        jenkinsRule.assertBuildStatus(Result.UNSTABLE, job.scheduleBuild2(0).get());
        ResultAction result = job.getLastBuild().getAction(ResultAction.class);
        assertThat(result.getSeverityDistribution().getHigh()).isPositive();
    }

    @Test
    public void stop_build_on_failed_threshold() throws Exception {
        WorkflowJob job = getBaseJob("dependencyCheckPublisherWorkflowStepSetLimits");
        job.setDefinition(new CpsFlowDefinition(""
                + "node {\n"
                + "  dependencyCheckPublisher(pattern: '**/dependency-check-report.xml', failedTotalHigh: 0, stopBuild:true)\n"
                + "  echo('Hello World')\n"
                + "}\n", true)
                );
        WorkflowRun run = job.scheduleBuild2(0).get();
        jenkinsRule.assertBuildStatus(Result.FAILURE, run);
        jenkinsRule.assertLogNotContains("Hello World", run);
        ResultAction result = job.getLastBuild().getAction(ResultAction.class);
        assertThat(result.getSeverityDistribution().getHigh()).isPositive();
    }

}