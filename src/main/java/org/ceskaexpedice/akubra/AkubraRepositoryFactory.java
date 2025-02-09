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

import org.ceskaexpedice.akubra.core.CoreRepositoryFactory;
import org.ceskaexpedice.akubra.core.repository.CoreRepository;
import org.ceskaexpedice.akubra.impl.AkubraRepositoryImpl;

import java.util.concurrent.atomic.AtomicReference;

/**
 * RepositoryAccessFactory
 *
 * @author ppodsednik
 */
public final class AkubraRepositoryFactory {
    private static final AtomicReference<AkubraRepository> INSTANCE = new AtomicReference<>();

    private AkubraRepositoryFactory() {
    }

    public static AkubraRepository createRepository(RepositoryConfiguration configuration) {
        return INSTANCE.updateAndGet(existingInstance -> {
            if (existingInstance == null) {
                CoreRepository coreRepository = CoreRepositoryFactory.createRepository(configuration);
                AkubraRepository baseAccess = new AkubraRepositoryImpl(coreRepository);
                // TODO we can also instantiate decorators here; for now let us return just basic access
                return baseAccess;
            }
            return existingInstance;
        });
    }

}
