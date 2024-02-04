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
package org.jenkinsci.plugins.DependencyCheck.transformer;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.entry;

import java.io.InputStream;
import java.util.List;

import org.jenkinsci.plugins.DependencyCheck.model.Finding;
import org.jenkinsci.plugins.DependencyCheck.model.ReportParser;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.Issue;

class FindingsTransformerTest {

    @Issue("SECURITY-3344")
    @Test
    void test_html_escape() throws Exception {
        InputStream report = this.getClass().getResourceAsStream("dependency-check-report-html-injection.xml");
        List<Finding> findings = ReportParser.parse(report);
        String json = new FindingsTransformer() {
            @Override
            protected void initSymbols() {
            };
        }.transform(findings).toString();
        assertThatJson(json).node("rows").isArray() //
                .element(0).isObject() //
                .contains( //
                        entry("vulnerability.description", "&lt;img/src/onerror=alert(`VulnDescription`)&gt;"), //
                        entry("dependency.filePath", "&lt;img/src/onerror=alert(`FilePath`)&gt;") //
                ) //
                .node("dependency\\.fileName").isObject() //
                .containsEntry("value", "&lt;img/src/onerror=alert(`FileName`)&gt;") //
                .node("options.sortValue").isEqualTo("&lt;img/src/onerror=alert(`FilePath`)&gt;");
    }
}
