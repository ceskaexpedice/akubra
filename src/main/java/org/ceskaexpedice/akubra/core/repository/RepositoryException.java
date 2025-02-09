/*
 * Copyright (C) 2025 Inovatika
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ceskaexpedice.akubra.core.repository;

/**
 * Exception representing an error that occurs during repository operations.
 * This class extends {@link RuntimeException} and is used to indicate
 * repository-related issues such as data access or integrity problems.
 *
 * It provides several constructors to create an exception with different
 * levels of detail (message, cause, suppression, and stack trace writing).
 *
 * @author pavels
 */
public class RepositoryException extends RuntimeException {

    /**
     * Default constructor for the exception. Initializes the exception
     * with no detail message or cause.
     */
    public RepositoryException() {
        super();
    }

    /**
     * Constructs a new RepositoryException with the specified detail message,
     * cause, suppression enabled or disabled, and whether or not the stack trace
     * should be writable.
     *
     * @param message The detail message (which is saved for later retrieval
     *        by the {@link Throwable#getMessage()} method).
     * @param cause The cause (which is saved for later retrieval by the
     *        {@link Throwable#getCause()} method). A null value is allowed.
     * @param enableSuppression Whether or not suppression is enabled.
     * @param writableStackTrace Whether or not the stack trace should be writable.
     */
    public RepositoryException(String message, Throwable cause, boolean enableSuppression,
                               boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * Constructs a new RepositoryException with the specified detail message
     * and cause. The suppression and stack trace writing are set to default values.
     *
     * @param message The detail message (which is saved for later retrieval
     *        by the {@link Throwable#getMessage()} method).
     * @param cause The cause (which is saved for later retrieval by the
     *        {@link Throwable#getCause()} method). A null value is allowed.
     */
    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new RepositoryException with the specified detail message.
     *
     * @param message The detail message (which is saved for later retrieval
     *        by the {@link Throwable#getMessage()} method).
     */
    public RepositoryException(String message) {
        super(message);
    }

    /**
     * Constructs a new RepositoryException with the specified cause.
     *
     * @param cause The cause (which is saved for later retrieval by the
     *        {@link Throwable#getCause()} method). A null value is allowed.
     */
    public RepositoryException(Throwable cause) {
        super(cause);
    }
}

