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

import com.cedarsoftware.util.io.JsonReader;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.artifact.PluginArtifact;

/**
 * A simple factory that constructs Maven Artifacts for use with {@link com.cedarsoftware.util.io.JsonReader}.
 */
public class ArtifactClassFactory implements JsonReader.ClassFactory {

    public Object newInstance(Class c) {
        if (DefaultArtifact.class.isAssignableFrom(c)) {
            return new DefaultArtifact("groupId", "artifactId", "0.0", "", "type", "", null);
        } else if (PluginArtifact.class.isAssignableFrom(c)) {
            Artifact artifact = new DefaultArtifact("groupId", "artifactId", "0.0", "", "type", "", null);
            Plugin plugin = new Plugin();
            plugin.setGroupId("groupId");
            plugin.setArtifactId("artifactId");
            plugin.setVersion("0.0");
            return new PluginArtifact(plugin, artifact);
        }
        throw new RuntimeException("ArtifactClassFactory handed Class for which it was not expecting: " + c.getName());
    }

}
