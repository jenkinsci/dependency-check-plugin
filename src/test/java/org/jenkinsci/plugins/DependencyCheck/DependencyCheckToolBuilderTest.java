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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.jenkinsci.plugins.DependencyCheck.tools.DependencyCheckInstallation;
import org.jenkinsci.plugins.DependencyCheck.tools.DependencyCheckInstaller;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.Answers;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Launcher.ProcStarter;
import hudson.model.FreeStyleProject;
import hudson.model.Node;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tools.InstallSourceProperty;
import hudson.util.ArgumentListBuilder;

@RunWith(Parameterized.class)
public class DependencyCheckToolBuilderTest {

    private static class MockDependencyCheckToolBuilder extends DependencyCheckToolBuilder {
        private static final long serialVersionUID = 2630773000142173803L;
        private int exitCode;

        MockDependencyCheckToolBuilder(String name, int exitCode) {
            super(name);
            this.exitCode = exitCode;
        }

        @Override
        public void perform(@NonNull final Run<?, ?> build,
                            @NonNull final FilePath workspace,
                            @NonNull final EnvVars env,
                            @NonNull final Launcher launcher,
                            @NonNull final TaskListener listener) throws InterruptedException, IOException {
            Launcher mockLauncher = mock(Launcher.class);
            ProcStarter proc = mock(ProcStarter.class, Answers.RETURNS_DEEP_STUBS);
            when(mockLauncher.launch()).thenReturn(proc);
            when(proc.cmds(any(ArgumentListBuilder.class)) //
                    .envs(env) //
                    .stdout(any(PrintStream.class)) //
                    .quiet(true) //
                    .pwd(workspace) //
                    .join()).thenReturn(exitCode);
            super.perform(build, workspace, env, mockLauncher, listener);
        }
    }

    @Parameters(name = "test that {0} (autoinstaller {1}) return exit code {2} will result in {3}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { //
                                              { "7.4.4", true, 0, Result.SUCCESS }, //
                                              { "7.4.4", true, -1, Result.FAILURE }, //
                                              { "8.3.2", true, 15, Result.SUCCESS }, //
                                              { "8.3.2", true, 14, Result.SUCCESS }, //
                                              { "8.3.2", true, 13, Result.FAILURE }, //
                                              { "7.4.4", false, -1, Result.FAILURE }, //
                                              { "8.3.2", false, 15, Result.SUCCESS }, //
        });
    }

    @ClassRule
    public static JenkinsRule jenkinsRule = new JenkinsRule();

    @Parameter
    public String version;
    @Parameter(1)
    public boolean autoinstaller;
    @Parameter(2)
    public int exitCode;
    @Parameter(3)
    public Result expectedResutt;

    @Test
    public void test_exit_code_by_dependency_check_version() throws Exception {
        FreeStyleProject job = jenkinsRule.createFreeStyleProject("free");
        try {
            String installerName = "dep check";
            jenkinsRule.jenkins.getDescriptorByType(DependencyCheckInstallation.DescriptorImpl.class) //
                    .setInstallations(prepareInstaller(installerName, version, autoinstaller));

            DependencyCheckToolBuilder builder = new MockDependencyCheckToolBuilder(installerName, exitCode);
            job.getBuildersList().add(builder);

            jenkinsRule.assertBuildStatus(expectedResutt, job.scheduleBuild2(0));
        } finally {
            job.delete();
        }
    }

    private DependencyCheckInstallation prepareInstaller(String name, String version, boolean isAutoinstaller) throws Exception {
        List<DependencyCheckInstaller> installers = null;
        if (isAutoinstaller) {
            installers = Arrays.asList(new DependencyCheckInstaller(version));
        }
        InstallSourceProperty properties = new InstallSourceProperty(installers);
        DependencyCheckInstallation installation = spy(new DependencyCheckInstallation(name, "home", Arrays.asList(properties)));
        doReturn("home/dp").when(installation).getExecutable(any());
        doReturn(installation).when(installation).forEnvironment(any(EnvVars.class));
        doReturn(installation).when(installation).forNode(any(Node.class), any(TaskListener.class));

        return installation;
    }
}
