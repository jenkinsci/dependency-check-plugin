package org.jenkinsci.plugins.DependencyCheck.model;

/**
 * Exception used to indicate that the defined thresholds are exceeded. This
 * causes the build step and also the build to fail.
 */
public class ThresholdsExceededException extends RuntimeException {
    public ThresholdsExceededException(String message) {
        super(message);
    }
}
