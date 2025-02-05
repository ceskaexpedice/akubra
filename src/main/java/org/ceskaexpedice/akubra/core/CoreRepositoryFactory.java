/**
 * Copyright ©2023 Accenture and/or its affiliates. All Rights Reserved.
 * <p>
 * Permission to any use, copy, modify, and distribute this software and
 * its documentation for any purpose is subject to a licensing agreement
 * duly entered into with the copyright owner or its affiliate.
 * <p>
 * All information contained herein is, and remains the property of Accenture
 * and/or its affiliates and its suppliers, if any.  The intellectual and
 * technical concepts contained herein are proprietary to Accenture and/or
 * its affiliates and its suppliers and may be covered by one or more patents
 * or pending patent applications in one or more jurisdictions worldwide,
 * and are protected by trade secret or copyright law. Dissemination of this
 * information or reproduction of this material is strictly forbidden unless
 * prior written permission is obtained from Accenture and/or its affiliates.
 */
package org.ceskaexpedice.akubra.core;

import org.ceskaexpedice.akubra.RepositoryConfiguration;
import org.ceskaexpedice.akubra.core.processingindex.ProcessingIndexFeederSolr;
import org.ceskaexpedice.akubra.core.repository.ProcessingIndexFeeder;
import org.ceskaexpedice.akubra.core.repository.CoreRepository;
import org.ceskaexpedice.akubra.core.repository.impl.AkubraDOManager;
import org.ceskaexpedice.akubra.core.repository.impl.CoreRepositoryImpl;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrClient;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheManagerBuilder;

/**
 * AkubraRepositoryFactory
 *
 * @author ppodsednik
 */
public final class CoreRepositoryFactory {

    private CoreRepositoryFactory() {
    }

    public static CoreRepository createRepository(RepositoryConfiguration configuration) {
        ProcessingIndexFeeder processingIndexFeeder = createProcessingIndexFeeder(configuration);
        AkubraDOManager akubraDOManager = new AkubraDOManager(createCacheManager(), configuration);
        return new CoreRepositoryImpl(processingIndexFeeder, akubraDOManager);
    }

    public static ProcessingIndexFeeder createProcessingIndexFeeder(RepositoryConfiguration configuration) {
        ProcessingIndexFeeder processingIndexFeeder = new ProcessingIndexFeederSolr(createProcessingUpdateClient(configuration));
        return processingIndexFeeder;
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
