/*
 * Copyright (C) 2012 Pavel Stastny
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

import javax.xml.namespace.NamespaceContext;
import java.util.*;

import static org.ceskaexpedice.akubra.core.repository.RepositoryNamespaces.*;

/**
 * Fedora XML namespaces
 * @author pavels
 */
public class RepositoryNamespaceContext implements NamespaceContext {

    private static final Map<String, String> MAP_PREFIX2URI = new IdentityHashMap<String, String>();
    private static final Map<String, String> MAP_URI2PREFIX = new IdentityHashMap<String, String>();

    static {
        MAP_PREFIX2URI.put("mods", BIBILO_MODS_URI);
        MAP_PREFIX2URI.put("dc", DC_NAMESPACE_URI);
        MAP_PREFIX2URI.put("oai_dc", OAI_DC_NAMESPACE_URI);
        //MAP_PREFIX2URI.put("fedora_models", FEDORA_MODELS_URI);
        MAP_PREFIX2URI.put("kramerius", KRAMERIUS_URI);
        MAP_PREFIX2URI.put("rdf", RDF_NAMESPACE_URI);
        MAP_PREFIX2URI.put("oai", OAI_NAMESPACE_URI);
        MAP_PREFIX2URI.put("sparql", SPARQL_NAMESPACE_URI);
        MAP_PREFIX2URI.put("apia", FEDORA_ACCESS_NAMESPACE_URI);
        MAP_PREFIX2URI.put("apim", FEDORA_MANAGEMENT_NAMESPACE_URI);

        // fedora4 mappings

        MAP_PREFIX2URI.put("premis", PREMIS_NAMESPACE_URI);
        MAP_PREFIX2URI.put("indexing", INDEXING_NAMESPACE_URI);
        MAP_PREFIX2URI.put("xsi", SCHEMA_INSTANCE_NAMESPACE_URI);
        MAP_PREFIX2URI.put("xmlns", XMLNS_NAMESPACE_URI);
        MAP_PREFIX2URI.put("fedora3model", FEDORA_MODELS_URI);
        MAP_PREFIX2URI.put("fedoraaccess", FEDORA_ACCESS_NAMESPACE_URI);
        MAP_PREFIX2URI.put("fedora", FEDORA_4_NAMESPACE_URI);
        MAP_PREFIX2URI.put("xml", XML_NAMESPACE_URI);
        MAP_PREFIX2URI.put("ebucore", EBUCORE_NAMESPACE_URI);
        MAP_PREFIX2URI.put("ldp", LDP_NAMESPACE_URI);
        MAP_PREFIX2URI.put("dcterms", DCTERMS_NAMESPACE_URI);
        MAP_PREFIX2URI.put("xs", SCHEMA_NAMESPACE_URI);
        MAP_PREFIX2URI.put("fedoraconfig", FEDORACONFIG_NAMESPACE_URI);
        MAP_PREFIX2URI.put("foaf", FOAF_NAMESPACE_URI);


        for (Map.Entry<String, String> entry : MAP_PREFIX2URI.entrySet()) {
            MAP_URI2PREFIX.put(entry.getValue(), entry.getKey());
        }
    }

    @Override
    public String getNamespaceURI(String arg0) {
        return MAP_PREFIX2URI.get(arg0.intern());
    }

    @Override
    public String getPrefix(String arg0) {
        return MAP_URI2PREFIX.get(arg0.intern());
    }

    @Override
    public Iterator getPrefixes(String arg0) {
        String prefixInternal = MAP_URI2PREFIX.get(arg0.intern());
        if (prefixInternal != null) {
            return Arrays.asList(prefixInternal).iterator();
        } else {
            return Collections.emptyList().iterator();
        }
    }


    public List<String> getNamespaceURIs() {
        return new ArrayList<>(MAP_URI2PREFIX.keySet());
    }

    public List<String> getPrefixes() {
        return new ArrayList<>(MAP_PREFIX2URI.keySet());
    }
}
