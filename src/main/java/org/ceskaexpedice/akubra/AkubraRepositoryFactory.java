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

import org.ceskaexpedice.akubra.config.RepositoryConfiguration;
import org.ceskaexpedice.akubra.core.CoreRepositoryFactory;
import org.ceskaexpedice.akubra.core.repository.CoreRepository;
import org.ceskaexpedice.akubra.impl.AkubraRepositoryImpl;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Factory for creating instances of {@link AkubraRepository}.
 *
 * <p>This class ensures that a single instance of {@code AkubraRepository} is created
 * and reused whenever requested, implementing a thread-safe singleton pattern.</p>
 *
 * <p>The repository is built using a {@link CoreRepository} obtained from
 * {@link CoreRepositoryFactory}, and additional decorators could be added
 * in the future.</p>
 *
 * <p>This class cannot be instantiated.</p>
 *
 * @author pavels, petrp
 */
public final class AkubraRepositoryFactory {

    /**
     * Holds the singleton instance of {@link AkubraRepository}.
     */
    private static final AtomicReference<AkubraRepository> INSTANCE = new AtomicReference<>();

    /**
     * Private constructor to prevent instantiation.
     */
    private AkubraRepositoryFactory() {
    }

    /**
     * Creates or retrieves a singleton instance of {@link AkubraRepository}.
     *
     * <p>If an instance does not already exist, this method initializes one using
     * the provided {@link RepositoryConfiguration}. The implementation currently
     * returns an instance of {@link AkubraRepositoryImpl}, but future enhancements
     * may introduce additional decorators.</p>
     *
     * @param configuration The repository configuration settings.
     * @return A singleton instance of {@link AkubraRepository}.
     */
    public static AkubraRepository createRepository(RepositoryConfiguration configuration) {
        return INSTANCE.updateAndGet(existingInstance -> {
            if (existingInstance == null) {
                CoreRepository coreRepository = CoreRepositoryFactory.createRepository(configuration);
                AkubraRepository baseAccess = new AkubraRepositoryImpl(coreRepository);
                return baseAccess;
            }
            return existingInstance;
        });
    }

    public static AkubraRepository createRepository(CoreRepository coreRepository) {
        return INSTANCE.updateAndGet(existingInstance -> {
            if (existingInstance == null) {
                AkubraRepository baseAccess = new AkubraRepositoryImpl(coreRepository);
                return baseAccess;
            }
            return existingInstance;
        });
    }

}
