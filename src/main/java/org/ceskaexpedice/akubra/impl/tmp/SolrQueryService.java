package org.ceskaexpedice.akubra.impl.tmp;

import org.ceskaexpedice.akubra.fedora.impl.tmp.ProcessingIndexQueryParameters;
import org.ceskaexpedice.akubra.fedora.impl.tmp.ResultMapper;
import org.ceskaexpedice.akubra.fedora.om.repository.RepositoryException;

import java.io.IOException;

public interface SolrQueryService {

    <T> T query(ProcessingIndexQueryParameters params, ResultMapper<T> mapper) throws RepositoryException, IOException, SolrServerException;

}
