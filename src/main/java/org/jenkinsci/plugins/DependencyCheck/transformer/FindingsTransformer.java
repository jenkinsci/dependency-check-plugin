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

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.jenkinsci.plugins.DependencyCheck.model.Dependency;
import org.jenkinsci.plugins.DependencyCheck.model.Finding;
import org.jenkinsci.plugins.DependencyCheck.model.Severity;
import org.jenkinsci.plugins.DependencyCheck.model.Vulnerability;

import io.jenkins.plugins.fontawesome.api.SvgTag;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Converts a list of Findings into a data structure suitable
 * for the FooTable Javascript component.
 *
 * Ported from the Dependency-Track Jenkins plugin.
 *
 * @author Steve Springett (steve.springett@owasp.org)
 * @since 5.0.0
 */
public class FindingsTransformer {

    public JSONObject transform(List<Finding> findings) {
        final JSONArray columns = new JSONArray();

        final JSONObject fileName = new JSONObject();
        fileName.put("name", "dependency.fileName");
        fileName.put("title", "File Name");
        fileName.put("visible", true);
        fileName.put("filterable", true);
        fileName.put("sortValue", "dependency.fileName");
        columns.add(fileName);

        final JSONObject filePath = new JSONObject();
        filePath.put("name", "dependency.filePath");
        filePath.put("title", "File Path");
        filePath.put("breakpoints", "all");
        filePath.put("visible", true);
        filePath.put("filterable", false);
        filePath.put("sortValue", "dependency.filePath");
        columns.add(filePath);

        final JSONObject sha1 = new JSONObject();
        sha1.put("name", "dependency.sha1");
        sha1.put("title", "SHA-1");
        sha1.put("breakpoints", "all");
        sha1.put("visible", true);
        sha1.put("filterable", true);
        sha1.put("sortValue", "dependency.sha1");
        columns.add(sha1);

        final JSONObject sha256 = new JSONObject();
        sha256.put("name", "dependency.sha256");
        sha256.put("title", "SHA-256");
        sha256.put("breakpoints", "all");
        sha256.put("visible", true);
        sha256.put("filterable", true);
        sha256.put("sortValue", "dependency.sha256");
        columns.add(sha256);

        final JSONObject vulnNameLabel = new JSONObject();
        vulnNameLabel.put("name", "vulnerability.nameLabel");
        vulnNameLabel.put("title", "Vulnerability");
        vulnNameLabel.put("visible", true);
        vulnNameLabel.put("filterable", true);
        vulnNameLabel.put("sortValue", "vulnerability.name");
        columns.add(vulnNameLabel);

        final JSONObject severityLabel = new JSONObject();
        severityLabel.put("name", "vulnerability.severityLabel");
        severityLabel.put("title", "Severity");
        severityLabel.put("visible", true);
        severityLabel.put("filterable", true);
        severityLabel.put("sortValue", "vulnerability.severityRank");
        columns.add(severityLabel);

        final JSONObject cwe = new JSONObject();
        cwe.put("name", "vulnerability.cwe");
        cwe.put("title", "Weakness");
        cwe.put("visible", true);
        cwe.put("filterable", true);
        cwe.put("sortValue", "vulnerability.cwe");
        columns.add(cwe);

        final JSONObject vulnDescription = new JSONObject();
        vulnDescription.put("name", "vulnerability.description");
        vulnDescription.put("title", "Description");
        vulnDescription.put("breakpoints", "all");
        vulnDescription.put("visible", true);
        vulnDescription.put("filterable", false);
        columns.add(vulnDescription);

        final JSONObject vulnReferences = new JSONObject();
        vulnReferences.put("name", "vulnerability.references");
        vulnReferences.put("title", "References");
        vulnReferences.put("breakpoints", "all");
        vulnReferences.put("visible", true);
        vulnReferences.put("filterable", false);
        columns.add(vulnReferences);

        final JSONArray rows = new JSONArray();
        for (Finding finding: findings) {
            final Dependency dependency = finding.getDependency();
            final Vulnerability vulnerability = finding.getVulnerability();
            final JSONObject row = new JSONObject();
            row.put("dependency.fileName", dependency.getFileName());
            row.put("dependency.filePath", dependency.getFilePath());
            row.put("dependency.description", dependency.getDescription());
            row.put("dependency.license", dependency.getLicense());
            row.put("dependency.md5", dependency.getMd5());
            row.put("dependency.sha1", dependency.getSha1());
            row.put("dependency.sha256", dependency.getSha256());
            row.put("vulnerability.source", vulnerability.getSource());
            row.put("vulnerability.name", vulnerability.getName());
            row.put("vulnerability.nameLabel", generateVulnerabilityField(vulnerability.getSource().name(), vulnerability.getName()));
            row.put("vulnerability.description", vulnerability.getDescription());
            if (CollectionUtils.isNotEmpty(vulnerability.getReferences())) {
                StringBuilder referecens = new StringBuilder();
                vulnerability.getReferences().forEach(ref -> {
                    referecens.append("<a href=\"" + ref.getUrl() + "\" target=\"_blank\">");
                    referecens.append(ref.getName());
                    referecens.append("</a>");
                    referecens.append("<br />");
                    referecens.append("\n");
                });
                row.put("vulnerability.references", referecens.toString());
            }
            row.put("vulnerability.severityLabel", generateSeverityField(Severity.normalize(vulnerability.getSeverity())));
            row.put("vulnerability.severity", vulnerability.getSeverity());
            row.put("vulnerability.severityRank", Severity.normalize(vulnerability.getSeverity()).ordinal());
            if (CollectionUtils.isNotEmpty(vulnerability.getCwes())) {
                row.put("vulnerability.cwe", vulnerability.getCwes().get(0));
            }
            rows.add(row);
        }
        final JSONObject data = new JSONObject();
        data.put("columns", columns);
        data.put("rows", rows);
        return data;
    }

    private String generateSeverityField(Severity severity) {
        return "<div style=\"height:24px;margin:-4px;\">\n" +
                "<div class=\"severity-" + severity.name().toLowerCase() + "-bg text-center pull-left\" style=\"width:24px; height:24px; color:#ffffff\">\n" +
                SvgTag.fontAwesomeSvgIcon("bug").withClasses("no-issues-banner", "small-svg-icon").render() + //
                "</div>\n" +
                "<div class=\"text-center pull-left\" style=\"height:24px;\">\n" +
                "  <div style=\"font-size:12px; padding:4px\"><span class=\"severity-value\">" + convert(severity.name()) + "</span></div>\n" +
                "</div>\n" +
                "</div>";
    }

    private String generateVulnerabilityField(String source, String vulnId) {
        return "<span class=\"vuln-source vuln-source-" + source.toLowerCase() + "\">" + source + "</span>" + vulnId;
    }

    private String convert(String str) {
        char ch[] = str.toCharArray();
        for (int i = 0; i < str.length(); i++) {
            if (i == 0 && ch[i] != ' ' || ch[i] != ' ' && ch[i - 1] == ' ') {
                if (ch[i] >= 'a' && ch[i] <= 'z') {
                    ch[i] = (char) (ch[i] - 'a' + 'A');
                }
            } else if (ch[i] >= 'A' && ch[i] <= 'Z') {
                ch[i] = (char) (ch[i] + 'a' - 'A');
            }
        }
        return new String(ch);
    }
}