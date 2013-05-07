/*
 * This file is part of DependencyCheck Jenkins plugin.
 *
 * DependencyCheck is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * DependencyCheck is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * DependencyCheck. If not, see http://www.gnu.org/licenses/.
 */
package org.jenkinsci.plugins.DependencyCheck.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Java Bean class for a dependency as a result of a DependencyCheck scan.
 *
 * @author Steve Springett
 */
public class Dependency
{
    private String fileName;
    private String filePath;
    private String md5;
    private String sha1;
    private String description;
    private String license;

    /**
     * All vulnerabilities for this dependency
     */
    private final List<Vulnerability> vulnerabilities = new ArrayList<Vulnerability>();

    /**
     * Returns the filename of the dependency
     *
     * @return the filename
     */
    public String getFileName()
    {
        return fileName;
    }

    /**
     * Sets the filename of the dependency
     *
     * @param fileName the filename
     */
    public void setFileName(final String fileName)
    {
        this.fileName = fileName;
    }

    /**
     * Returns the file path of the dependency
     *
     * @return the files path
     */
    public String getFilePath()
    {
        return filePath;
    }

    /**
     * Sets the files path to the dependency
     *
     * @param filePath the file path
     */
    public void setFilePath(final String filePath)
    {
        this.filePath = filePath;
    }

    /**
     * Returns the MD5 hash of the dependency
     *
     * @return the MD5 hash
     */
    public String getMd5()
    {
        return md5;
    }

    /**
     * Sets the MD5 hash of the dependency
     *
     * @param md5 the MD5 hash
     */
    public void setMd5(final String md5)
    {
        this.md5 = md5;
    }

    /**
     * Returns the SHA-1 hash of the dependency
     *
     * @return the SHA-1 hash
     */
    public String getSha1()
    {
        return sha1;
    }

    /**
     * Sets the SHA-1 hash of the dependency
     *
     * @param sha1 the SHA-1 hash
     */
    public void setSha1(final String sha1)
    {
        this.sha1 = sha1;
    }

    /**
     * Returns the description of the dependency
     *
     * @return the description of the dependency
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Sets the description of the dependency
     *
     * @param description the description of the dependency
     */
    public void setDescription(final String description)
    {
        this.description = description;
    }

    /**
     * Returns the license the dependency is licensed under.
     *
     * @return the license of the dependency
     */
    public String getLicense()
    {
        return license;
    }

    /**
     * Sets the license the dependency is licensed under
     *
     * @param license the license of the dependency
     */
    public void setLicense(final String license)
    {
        this.license = license;
    }

    /**
     * Adds a new vulnerability to this collection
     *
     * @param vulnerability the vulnerability to add
     */
    public void addVulnerability(final Vulnerability vulnerability)
    {
        vulnerabilities.add(vulnerability);
    }

    /**
     * Returns a read-only collection of all vulnerabilities from this dependency
     *
     * @return all vulnerabilities of this dependency
     */
    public Collection<Vulnerability> getVulnerabilities()
    {
        return Collections.unmodifiableCollection(vulnerabilities);
    }
}