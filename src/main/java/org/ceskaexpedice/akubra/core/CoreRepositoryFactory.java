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
package org.ceskaexpedice.akubra.core;

import org.ceskaexpedice.akubra.config.RepositoryConfiguration;
import org.ceskaexpedice.akubra.core.processingindex.ProcessingIndexSolr;
import org.ceskaexpedice.akubra.core.repository.CoreRepository;
import org.ceskaexpedice.akubra.core.repository.impl.CoreRepositoryImpl;
import org.ceskaexpedice.akubra.processingindex.ProcessingIndex;

/**
 * Factory for creating instances of {@link CoreRepository}.
 *
 * <p>This class cannot be instantiated.</p>
 *
 * @author ppodsednik
 */
public final class CoreRepositoryFactory {

    private CoreRepositoryFactory() {
    }

    public static CoreRepository createRepository(RepositoryConfiguration configuration) {
        CoreRepositoryImpl coreRepository = new CoreRepositoryImpl(configuration);
        ProcessingIndex processingIndex = new ProcessingIndexSolr(configuration, coreRepository);
        coreRepository.setProcessingIndex(processingIndex);
        return coreRepository;
    }

}
