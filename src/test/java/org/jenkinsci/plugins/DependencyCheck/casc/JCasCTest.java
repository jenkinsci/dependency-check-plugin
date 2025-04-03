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
package org.jenkinsci.plugins.DependencyCheck.casc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.jenkins.plugins.casc.misc.junit.jupiter.AbstractRoundTripTest;
import org.jenkinsci.plugins.DependencyCheck.tools.DependencyCheckInstallation;
import org.jenkinsci.plugins.DependencyCheck.tools.DependencyCheckInstaller;

import org.jvnet.hudson.test.JenkinsRule;

import hudson.tools.InstallSourceProperty;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstallation;
import hudson.tools.ToolProperty;
import hudson.tools.ToolPropertyDescriptor;
import hudson.util.DescribableList;
import jenkins.model.Jenkins;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class JCasCTest extends AbstractRoundTripTest {

    @Override
    protected void assertConfiguredAsExpected(JenkinsRule jenkinsRule, String s) {
        checkInstallations(jenkinsRule.jenkins);
    }

    private static void checkInstallations(Jenkins j) {
        @SuppressWarnings({ "unchecked", "rawtypes" })
        final ToolDescriptor<DependencyCheckInstallation> descriptor = (ToolDescriptor) j.getDescriptor(DependencyCheckInstallation.class);
        final ToolInstallation[] installations = descriptor.getInstallations();
        assertThat(installations, arrayWithSize(2));

        ToolInstallation withInstaller = installations[0];
        assertEquals("latest", withInstaller.getName());

        final DescribableList<ToolProperty<?>, ToolPropertyDescriptor> properties = withInstaller.getProperties();
        assertThat(properties, hasSize(1));
        final ToolProperty<?> property = properties.get(0);

        assertThat(((InstallSourceProperty) property).installers, //
                containsInAnyOrder( //
                        allOf(instanceOf(DependencyCheckInstaller.class)) //
                ));

        ToolInstallation withoutInstaller = installations[1];
        assertThat(withoutInstaller, //
                allOf( //
                        hasProperty("name", equalTo("anotherWithNoInstall")), //
                        hasProperty("home", equalTo("/onePath") //
                        )));
    }

    @Override
    protected String stringInLogExpected() {
        return "dependency-check";
    }
}
