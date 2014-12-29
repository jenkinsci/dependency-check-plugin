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
package org.jenkinsci.plugins.DependencyCheck.maven;

import hudson.Extension;
import hudson.FilePath;
import hudson.maven.MavenBuildProxy;
import hudson.maven.MavenModule;
import hudson.maven.MavenReporter;
import hudson.maven.MavenReporterDescriptor;
import hudson.model.BuildListener;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;

import java.io.IOException;

/**
 * The MavenArtifactRecorder saves the artifacts used in a Maven build to JSON format.
 */
public class MavenArtifactRecorder extends MavenReporter {

    private static final long serialVersionUID = 2861843894200530783L;

    @Override
    public boolean postBuild(MavenBuildProxy build, MavenProject pom, final BuildListener listener)
            throws InterruptedException, IOException {

        if (pom != null && pom.getArtifacts() != null) {
            try {
                if (!build.getArtifactsDir().exists()) {
                    build.getArtifactsDir().mkdirs();
                }

                final StringBuilder sb = new StringBuilder();
                for (Artifact artifact: pom.getArtifacts()) {
                    sb.append(artifact.getFile().getAbsolutePath()).append("\n");
                }

                final FilePath artifacts = new FilePath(build.getArtifactsDir(), "artifacts.txt");
                artifacts.write(sb.toString(), "UTF-8");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return true;
    }

    @Extension
    public static final class DescriptorImpl extends MavenReporterDescriptor {
        public String getDisplayName() {
            return MavenArtifactRecorder.class.getName();
        }

        public MavenReporter newAutoInstance(MavenModule module) {
            return new MavenArtifactRecorder();
        }
    }

}
