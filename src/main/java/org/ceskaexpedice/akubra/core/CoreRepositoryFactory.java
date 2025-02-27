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

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrClient;
import org.ceskaexpedice.akubra.config.RepositoryConfiguration;
import org.ceskaexpedice.akubra.core.processingindex.ProcessingIndexSolr;
import org.ceskaexpedice.akubra.core.repository.CoreRepository;
import org.ceskaexpedice.akubra.core.repository.impl.AkubraDOManager;
import org.ceskaexpedice.akubra.core.repository.impl.CoreRepositoryImpl;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheManagerBuilder;

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
        ProcessingIndexSolr processingIndex = createProcessingIndexFeeder(configuration);
        AkubraDOManager akubraDOManager = new AkubraDOManager(createCacheManager(), configuration);
        return new CoreRepositoryImpl(processingIndex, akubraDOManager);
    }

    public static ProcessingIndexSolr createProcessingIndexFeeder(RepositoryConfiguration configuration) {
        ProcessingIndexSolr processingIndex = new ProcessingIndexSolr(createProcessingUpdateClient(configuration));
        return processingIndex;
    }

    public static CacheManager createCacheManager() {
        CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build();
        cacheManager.init();
        return cacheManager;
    }

  /* TODO
  private SolrClient processingQueryClient() {
    String processingSolrHost = KConfiguration.getInstance().getSolrProcessingHost();
    return new HttpSolrClient.Builder(processingSolrHost).build();
  }*/

    private static SolrClient createProcessingUpdateClient(RepositoryConfiguration configuration) {
        String processingSolrHost = configuration.getProcessingIndexHost();
        return new ConcurrentUpdateSolrClient.Builder(processingSolrHost).withQueueSize(100).build();
    }

}
