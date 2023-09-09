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
package org.jenkinsci.plugins.DependencyCheck.tools;

import java.text.MessageFormat;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;

/**
 * NodeJSVersion identifier.
 *
 * <p>
 * NodeJSVersion identifiers have tree components.
 * <ol>
 * <li>Major version. A non-negative integer.</li>
 * <li>Minor version. A non-negative integer.</li>
 * <li>Micro version. A non-negative integer.</li>
 * </ol>
 *
 * <p>
 * {@code NodeJSVersion} objects are immutable.
 * @author Nikolas Falco
 */
public class Version implements Comparable<Version> {
    private static final String MSG_INVALID_FORMAT = "invalid version \"{0}\": invalid format";
    private static final String MSG_NEGATIVE_NUMBER = "invalid version \"{0}\": negative number \"{1}\"";
    private static final String SEPARATOR = ".";

    private final int major;
    private final int minor;
    private final int micro;
    private transient String versionString /* default to null */; // NOSONAR
    private transient int hash /* default to 0 */; // NOSONAR

    /**
     * The empty version "0.0.0".
     */
    @SuppressWarnings("ConstantName")
    public static final Version emptyVersion = new Version(0, 0, 0);

    /**
     * Creates a version identifier from the specified numerical components.
     *
     * @param major
     *            Major component of the version identifier.
     * @param minor
     *            Minor component of the version identifier.
     * @param micro
     *            Micro component of the version identifier.
     * @throws IllegalArgumentException
     *             If the numerical components are negative.
     */
    public Version(int major, int minor, int micro) {
        this.major = major;
        this.minor = minor;
        this.micro = micro;
        validate();
    }

    /**
     * Creates a version identifier from the specified string.
     *
     * <p>
     * NodeJSVersion string grammar:
     *
     * <pre>
     * version ::= major('.'minor('.'micro)?)?
     * major ::= digit+
     * minor ::= digit+
     * micro ::= digit+
     * digit ::= [0..9]
     * </pre>
     *
     * @param version
     *            String representation of the version identifier. There must be
     *            no whitespace in the argument.
     * @throws IllegalArgumentException
     *             If {@code version} is improperly formatted.
     */
    public Version(String version) {
        int maj = 0;
        int min = 0;
        int mic = 0;

        String value = null;
        try {
            StringTokenizer st = new StringTokenizer(version, SEPARATOR, true);
            value = st.nextToken();
            maj = Integer.parseInt(value);

            if (st.hasMoreTokens()) {
                st.nextToken(); // consume delimiter
                value = st.nextToken(); // minor
                min = Integer.parseInt(value);

                if (st.hasMoreTokens()) {
                    st.nextToken(); // consume delimiter
                    value = st.nextToken(); // micro
                    mic = Integer.parseInt(value);

                    if (st.hasMoreTokens()) { // fail safe
                        throw new IllegalArgumentException(MessageFormat.format(MSG_INVALID_FORMAT, version));
                    }
                }
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("invalid version \"" + version + "\": non-numeric \"" + value + "\"");
        }

        major = maj;
        minor = min;
        micro = mic;
        validate();
    }

    /**
     * Called by the constructors to validate the version components.
     *
     * @throws IllegalArgumentException
     *             If the numerical components are negative.
     */
    private void validate() {
        if (major < 0) {
            throw new IllegalArgumentException(MessageFormat.format(MSG_NEGATIVE_NUMBER, toString0(), major));
        }
        if (minor < 0) {
            throw new IllegalArgumentException(MessageFormat.format(MSG_NEGATIVE_NUMBER, toString0(), minor));
        }
        if (micro < 0) {
            throw new IllegalArgumentException(MessageFormat.format(MSG_NEGATIVE_NUMBER, toString0(), micro));
        }
    }

    /**
     * Parses a version identifier from the specified string.
     *
     * <p>
     * See {@code NodeJSVersion(String)} for the format of the version string.
     *
     * @param version
     *            String representation of the version identifier. Leading and
     *            trailing whitespace will be ignored.
     * @return A {@code NodeJSVersion} object representing the version
     *         identifier. If {@code version} is {@code null} or the empty
     *         string then {@code emptyVersion} will be returned.
     * @throws IllegalArgumentException
     *             If {@code version} is improperly formatted.
     */
    public static Version parseVersion(final String version) {
        String v = StringUtils.trimToNull(version);
        if (v == null) {
            return emptyVersion;
        }

        return new Version(v);
    }

    /**
     * Returns the major component of this version identifier.
     *
     * @return The major component.
     */
    public int getMajor() {
        return major;
    }

    /**
     * Returns the minor component of this version identifier.
     *
     * @return The minor component.
     */
    public int getMinor() {
        return minor;
    }

    /**
     * Returns the micro component of this version identifier.
     *
     * @return The micro component.
     */
    public int getMicro() {
        return micro;
    }

    /**
     * Returns the string representation of this version identifier.
     *
     * <p>
     * The format of the version string will be {@code major.minor.micro}.
     *
     * @return The string representation of this version identifier.
     */
    @Override
    public String toString() {
        return toString0();
    }

    /**
     * Internal toString behavior
     *
     * @return The string representation of this version identifier.
     */
    String toString0() {
        if (versionString != null) {
            return versionString;
        }
        StringBuilder result = new StringBuilder(20);
        result.append(major);
        result.append(SEPARATOR);
        result.append(minor);
        result.append(SEPARATOR);
        result.append(micro);
        versionString = result.toString();
        return versionString;
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return An integer which is a hash code value for this object.
     */
    @Override
    public int hashCode() {
        if (hash != 0) {
            return hash;
        }
        int h = 31 * 17;
        h = 31 * h + major;
        h = 31 * h + minor;
        h = 31 * h + micro;
        hash = h;
        return hash;
    }

    /**
     * Compares this {@code NodeJSVersion} object to another object.
     *
     * <p>
     * A version is considered to be <b>equal to </b> another version if the
     * major, minor and micro components are equal.
     *
     * @param object
     *            The {@code NodeJSVersion} object to be compared.
     * @return {@code true} if {@code object} is a {@code NodeJSVersion} and is
     *         equal to this object; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object object) {
        if (object == this) { // quicktest
            return true;
        }

        if (!(object instanceof Version)) {
            return false;
        }

        Version other = (Version) object;
        return (major == other.major) && (minor == other.minor) && (micro == other.micro);
    }

    /**
     * Compares this {@code NodeJSVersion} object to another
     * {@code NodeJSVersion}.
     *
     * <p>
     * A version is considered to be <b>less than</b> another version if its
     * major component is less than the other version's major component, or the
     * major components are equal and its minor component is less than the other
     * version's minor component, or the major and minor components are equal
     * and its micro component is less than the other version's micro component,
     * or the major, minor and micro components are equal.
     *
     * <p>
     * A version is considered to be <b>equal to</b> another version if the
     * major, minor and micro components are equal.
     *
     * @param other
     *            The {@code NodeJSVersion} object to be compared.
     * @return A negative integer, zero, or a positive integer if this version
     *         is less than, equal to, or greater than the specified
     *         {@code NodeJSVersion} object.
     * @throws ClassCastException
     *             If the specified object is not a {@code NodeJSVersion}
     *             object.
     */
    @Override
    public int compareTo(Version other) {
        if (other == this) { // quicktest
            return 0;
        }

        int result = major - other.major;
        if (result != 0) {
            return result;
        }

        result = minor - other.minor;
        if (result != 0) {
            return result;
        }

        result = micro - other.micro;
        if (result != 0) {
            return result;
        }

        return 0;
    }
}
