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
import java.io.Serial;
import java.util.List;

import org.jenkinsci.plugins.DependencyCheck.tools.DependencyCheckInstallation;
import org.jenkinsci.plugins.DependencyCheck.tools.DependencyCheckInstaller;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
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

@WithJenkins
class DependencyCheckToolBuilderTest {

    private static class MockDependencyCheckToolBuilder extends DependencyCheckToolBuilder {
        @Serial
        private static final long serialVersionUID = 2630773000142173803L;
        private final int exitCode;

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

    static Object[][] data() {
        return new Object[][] { //
                                { "7.4.4", true, 0, Result.SUCCESS }, //
                                { "7.4.4", true, -1, Result.FAILURE }, //
                                { "8.3.2", true, 15, Result.SUCCESS }, //
                                { "8.3.2", true, 14, Result.SUCCESS }, //
                                { "8.3.2", true, 13, Result.FAILURE }, //
                                { "7.4.4", false, -1, Result.FAILURE }, //
                                { "8.3.2", false, 15, Result.SUCCESS }, //
        };
    }

    private static JenkinsRule jenkinsRule;

    @BeforeEach
    void setUp(JenkinsRule rule) {
        jenkinsRule = rule;
    }

    @ParameterizedTest(name = "test that {0} (autoinstaller {1}) return exit code {2} will result in {3}")
    @MethodSource("data")
    void test_exit_code_by_dependency_check_version(String version, boolean autoinstaller, int exitCode, Result expectedResult) throws Exception {
        FreeStyleProject job = jenkinsRule.createFreeStyleProject("free");
        try {
            String installerName = "dep check";
            jenkinsRule.jenkins.getDescriptorByType(DependencyCheckInstallation.DescriptorImpl.class) //
                    .setInstallations(prepareInstaller(installerName, version, autoinstaller));

            DependencyCheckToolBuilder builder = new MockDependencyCheckToolBuilder(installerName, exitCode);
            job.getBuildersList().add(builder);

            jenkinsRule.assertBuildStatus(expectedResult, job.scheduleBuild2(0));
        } finally {
            job.delete();
        }
    }

    private DependencyCheckInstallation prepareInstaller(String name, String version, boolean isAutoinstaller) throws Exception {
        List<DependencyCheckInstaller> installers = null;
        if (isAutoinstaller) {
            installers = List.of(new DependencyCheckInstaller(version));
        }
        InstallSourceProperty properties = new InstallSourceProperty(installers);
        DependencyCheckInstallation installation = spy(new DependencyCheckInstallation(name, "home", List.of(properties)));
        doReturn("home/dp").when(installation).getExecutable(any());
        doReturn(installation).when(installation).forEnvironment(any(EnvVars.class));
        doReturn(installation).when(installation).forNode(any(Node.class), any(TaskListener.class));

        return installation;
    }
}
