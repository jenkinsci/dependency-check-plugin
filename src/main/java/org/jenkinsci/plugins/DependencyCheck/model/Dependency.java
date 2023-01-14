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
package org.jenkinsci.plugins.DependencyCheck.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Java Bean class for a Dependency found by DependencyCheck.
 *
 * @author Steve Springett (steve.springett@owasp.org)
 * @since 5.0.0
 */
public class Dependency implements Comparable<Dependency>, Serializable {

    private static final long serialVersionUID = 1670679619302610671L;

    private String fileName;
    private String filePath;
    private String md5;
    private String sha1;
    private String sha256;
    private String description;
    private String license;
    private List<Vulnerability> vulnerabilities = new ArrayList<>();

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getSha1() {
        return sha1;
    }

    public void setSha1(String sha1) {
        this.sha1 = sha1;
    }

    public String getSha256() {
        return sha256;
    }

    public void setSha256(String sha256) {
        this.sha256 = sha256;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public List<Vulnerability> getVulnerabilities() {
        return vulnerabilities;
    }

    public void setVulnerabilities(List<Vulnerability> vulnerabilities) {
        this.vulnerabilities = vulnerabilities;
    }

    public void addVulnerability(Vulnerability vulnerability) {
        vulnerabilities.add(vulnerability);
    }

	@Override
	public int hashCode() {
		return Objects.hash(fileName, filePath, md5, sha1, sha256);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Dependency other = (Dependency) obj;
		return Objects.equals(fileName, other.fileName) && Objects.equals(filePath, other.filePath)
				&& Objects.equals(md5, other.md5) && Objects.equals(sha1, other.sha1) && Objects.equals(sha256, other.sha256);
	}
	
	@Override
	public int compareTo(Dependency other) {
		if  (this == other) {
			return 0;
		}
		if (other == null) {
			return -1;
		}
		if (!Objects.equals(fileName, other.fileName)) {
			return fileName.compareTo(other.fileName);
		}
		if (!Objects.equals(filePath, other.filePath)) {
			return filePath.compareTo(other.filePath);
		}
		if (!Objects.equals(md5, other.md5)) {
			return md5.compareTo(other.md5);
		}
		if (!Objects.equals(sha1, other.sha1)) {
			return sha1.compareTo(other.sha1);
		}
		if (!Objects.equals(sha256, other.sha256)) {
			return sha256.compareTo(other.sha256);
		}
		return 0;
	}
    
}
