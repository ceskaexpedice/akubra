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
package org.ceskaexpedice.akubra.access;

import org.ceskaexpedice.akubra.access.impl.RepositoryAccessImpl;
import org.ceskaexpedice.akubra.core.RepositoryConfiguration;
import org.ceskaexpedice.akubra.core.RepositoryFactory;
import org.ceskaexpedice.akubra.core.repository.Repository;

/**
 * RepositoryAccessFactory
 * @author ppodsednik
 */
public final class RepositoryAccessFactory {

  private RepositoryAccessFactory() {
  }

  public static RepositoryAccess createRepositoryAccess(RepositoryConfiguration configuration) {
    Repository coreRepository = RepositoryFactory.createRepository(configuration);
    RepositoryAccess repositoryAccess = new RepositoryAccessImpl(coreRepository);
    return repositoryAccess;
  }

}
