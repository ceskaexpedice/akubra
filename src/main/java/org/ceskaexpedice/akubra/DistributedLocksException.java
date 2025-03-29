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
package org.ceskaexpedice.akubra;

public class DistributedLocksException extends RepositoryException {
    public static final String LOCK_TIMEOUT = "LockTimeout";
    public static final String LOCK_SERVER_ERROR = "LockServerError";
    public static final String LOCK_NULL = "LockNull";

    private final String code;

    public DistributedLocksException(String code, Throwable cause) {
        super(cause);
        this.code = code;
    }

    public DistributedLocksException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
