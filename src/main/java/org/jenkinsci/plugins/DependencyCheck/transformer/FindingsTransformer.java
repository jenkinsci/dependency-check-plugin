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

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.DependencyCheck.model.Finding;
import org.jenkinsci.plugins.DependencyCheck.model.Severity;
import org.jenkinsci.plugins.DependencyCheck.model.Dependency;
import org.jenkinsci.plugins.DependencyCheck.model.Vulnerability;
import java.util.List;

/**
 * Converts a list of Findings into a data structure suitable
 * for the FooTable Javascript component.
 *
 * Ported from the Dependency-Track Jenkins plugin
 */
public class FindingsTransformer {

    public JSONObject transform(List<Finding> findings) {
        final JSONArray columns = new JSONArray();

        final JSONObject c1 = new JSONObject();
        c1.put("name", "dependency.nameLabel");
        c1.put("title", "Name");
        c1.put("visible", true);
        c1.put("filterable", true);
        c1.put("sortValue", "dependency.name");
        columns.add(c1);

        final JSONObject c2 = new JSONObject();
        c2.put("name", "dependency.versionLabel");
        c2.put("title", "Version");
        c2.put("visible", true);
        c2.put("filterable", true);
        c2.put("sortValue", "component.version");
        columns.add(c2);

        final JSONObject c3 = new JSONObject();
        c3.put("name", "dependency.groupLabel");
        c3.put("title", "Group");
        c3.put("visible", true);
        c3.put("filterable", true);
        c3.put("sortValue", "component.group");
        columns.add(c3);

        final JSONObject c4 = new JSONObject();
        c4.put("name", "vulnerability.vulnIdLabel");
        c4.put("title", "Vulnerability");
        c4.put("visible", true);
        c4.put("filterable", true);
        c4.put("sortValue", "vulnerability.vulnId");
        columns.add(c4);

        final JSONObject c5 = new JSONObject();
        c5.put("name", "vulnerability.severityLabel");
        c5.put("title", "Severity");
        c5.put("visible", true);
        c5.put("filterable", true);
        c5.put("sortValue", "vulnerability.severityRank");
        columns.add(c5);

        final JSONObject c6 = new JSONObject();
        c6.put("name", "vulnerability.cweLabel");
        c6.put("title", "CWE");
        c6.put("visible", true);
        c6.put("filterable", true);
        c6.put("sortValue", "vulnerability.cweId");
        JSONObject c61 = new JSONObject();
        c61.put("width", "30%");
        c6.put("style", c61);
        columns.add(c6);

        final JSONObject c7 = new JSONObject();
        c7.put("name", "vulnerability.title");
        c7.put("title", "Title");
        c7.put("breakpoints", "all");
        c7.put("visible", true);
        c7.put("filterable", false);
        columns.add(c7);

        final JSONObject c8 = new JSONObject();
        c8.put("name", "vulnerability.subtitle");
        c8.put("title", "Subtitle");
        c8.put("breakpoints", "all");
        c8.put("visible", true);
        c8.put("filterable", false);
        columns.add(c8);

        final JSONObject c9 = new JSONObject();
        c9.put("name", "vulnerability.description");
        c9.put("title", "Description");
        c9.put("breakpoints", "all");
        c9.put("visible", true);
        c9.put("filterable", false);
        columns.add(c9);

        final JSONObject c10 = new JSONObject();
        c10.put("name", "vulnerability.recommendation");
        c10.put("title", "Recommendation");
        c10.put("breakpoints", "all");
        c10.put("visible", true);
        c10.put("filterable", false);
        columns.add(c10);

        final JSONArray rows = new JSONArray();
        for (Finding finding: findings) {
            final Dependency component = finding.getDependency();
            final Vulnerability vulnerability = finding.getVulnerability();
            final JSONObject row = new JSONObject();
            row.put("component.fileName", component.getFileName());
            row.put("component.filePath", component.getFilePath());
            row.put("vulnerability.source", vulnerability.getSource());
            row.put("vulnerability.name", vulnerability.getName());
            //row.put("vulnerability.vulnNameLabel", generateVulnerabilityField(vulnerability.getSource(), vulnerability.getName()));
            row.put("vulnerability.description", vulnerability.getDescription());
            //row.put("vulnerability.severityLabel", generateSeverityField(vulnerability.getSeverity()));
            //row.put("vulnerability.severity", vulnerability.getSeverity());
            //row.put("vulnerability.severityRank", vulnerability.getSeverityRank());
            //row.put("vulnerability.cweLabel", generateCweField(vulnerability.getCweId(), vulnerability.getCweName()));
            //row.put("vulnerability.cweId", vulnerability.getCweId());
            //row.put("vulnerability.cweName", vulnerability.getCweName());
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
                "  <i class=\"fa fa-bug\" style=\"font-size:12px; padding:6px\" aria-hidden=\"true\"></i>\n" +
                "</div>\n" +
                "<div class=\"text-center pull-left\" style=\"height:24px;\">\n" +
                "  <div style=\"font-size:12px; padding:4px\"><span class=\"severity-value\">" + convert(severity.name()) + "</span></div>\n" +
                "</div>\n" +
                "</div>";
    }

    private String generateVulnerabilityField(String source, String vulnId) {
        return "<span class=\"vuln-source vuln-source-" + source.toLowerCase() + "\">" + source + "</span>" + vulnId;
    }

    private String generateCweField(Integer cweId, String cweName) {
        if (cweId == null || cweName == null) {
            return null;
        }
        return generateTruncatedStringField("CWE-" + cweId + " " + cweName);
    }

    private String generateTruncatedStringField(String in) {
        if (in == null) {
            return null;
        }
        return "<div class=\"truncate-ellipsis\"><span>" + in + "</span></div>";
    }

    private String convert(String str) {
        char ch[] = str.toCharArray();
        for (int i = 0; i < str.length(); i++) {
            if (i == 0 && ch[i] != ' ' || ch[i] != ' ' && ch[i - 1] == ' ') {
                if (ch[i] >= 'a' && ch[i] <= 'z') {
                    ch[i] = (char)(ch[i] - 'a' + 'A');
                }
            } else if (ch[i] >= 'A' && ch[i] <= 'Z') {
                ch[i] = (char) (ch[i] + 'a' - 'A');
            }
        }
        return new String(ch);
    }
}