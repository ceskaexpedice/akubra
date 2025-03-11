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
package org.ceskaexpedice.akubra.core.processingindex;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.ceskaexpedice.akubra.RepositoryException;
import org.ceskaexpedice.akubra.RepositoryNamespaceContext;
import org.ceskaexpedice.akubra.RepositoryNamespaces;
import org.ceskaexpedice.akubra.utils.DomUtils;
import org.ceskaexpedice.akubra.utils.StringUtils;
import org.ceskaexpedice.akubra.pid.PIDParser;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by pstastny on 10/11/2017.
 */
class RELSEXTSPARQLBuilderImpl implements RELSEXTSPARQLBuilder {

    private RepositoryNamespaceContext namespaceContext = new RepositoryNamespaceContext();

    void prefix(StringBuilder builder) {
        namespaceContext.getPrefixes().stream().filter(s-> !namespaceContext.getNamespaceURI(s).equals(RepositoryNamespaces.FEDORA_ACCESS_NAMESPACE_URI)).forEach(s-> {builder.append("PREFIX ").append(s).append(':').append(" <").append(namespaceContext.getNamespaceURI(s)).append(">").append('\n');});
    }

    @Override
    public String sparqlProps(String relsExt, RELSEXTSPARQLBuilderListener listener) throws IOException, SAXException, ParserConfigurationException, RepositoryException {
        StringTemplateGroup strGroup = SPARQL_TEMPLATES();

        Document document = DomUtils.streamToDocument(new StringReader(relsExt), true);
        Element description = DomUtils.findElement(document.getDocumentElement(), "Description", RepositoryNamespaces.RDF_NAMESPACE_URI);
        NodeList childNodes = description.getChildNodes();

        List<Triple<String,String,String>> triples = new ArrayList<>();

        Map<String, Integer> counters = new HashMap<>();
        for (int i = 0,ll=childNodes.getLength(); i < ll; i++) {
            Node n = childNodes.item(i);

            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element elm = (Element) n;
                String localName = elm.getLocalName();
                String namespaceURI = elm.getNamespaceURI();

                String relation =  "<"+namespaceURI+(namespaceURI.endsWith("#")? "" :"#")+localName+">";
                //String relation
                List<Triple<String,String, String>> aList = new ArrayList<>();

                Attr resource = elm.getAttributeNodeNS(RepositoryNamespaces.RDF_NAMESPACE_URI, "resource");
                if (resource != null) {
                    String value = resource.getValue();
                    if (value.startsWith(PIDParser.INFO_FEDORA_PREFIX)) {
                        value = value.substring(PIDParser.INFO_FEDORA_PREFIX.length());
                        if (listener != null) {
                            value = listener.inform(value, localName);

                        }
                    }
                    if (!counters.containsKey(localName)) {
                        counters.put(localName,0);
                    }

                    // increment counters
                    counters.put(localName, counters.get(localName)+1);

                    // indirect reference - in order to preserve index
                    // https://wiki.duraspace.org/display/FEDORA4x/Ordering
                    // TODO List<String> treePredicates = Arrays.asList(Configuration.getInstance().getPropertyList("fedora.treePredicates"));
                    List<String> treePredicates = new ArrayList<>();

                    if (namespaceURI.equals(RepositoryNamespaces.KRAMERIUS_URI) && treePredicates.contains(localName)) {
                        String bRelationName = localName.startsWith("has") ? StringUtils.minus(localName, "has").toLowerCase():
                                localName.startsWith("is") ? StringUtils.minus(localName, "is").toLowerCase() : localName.toLowerCase();

                        String reference = "#"+bRelationName+(counters.get(localName));
                        Triple<String, String, String> refTriple = new ImmutableTriple<>("<>",relation, "<"+reference+">");
                        Triple<String, String, String> rawTripleRef = new ImmutableTriple<>("<"+reference+">","<http://www.w3.org/2002/07/owl#sameAs>", "<"+value+">");
                        Triple<String, String, String> rawTripleOrdering = new ImmutableTriple<>("<"+reference+">","<https://schema.org/Order>", "'"+counters.get(localName)+"'");

                        triples.add(refTriple);
                        triples.add(rawTripleRef);
                        triples.add(rawTripleOrdering);
                    } else {
                        triples.add(new ImmutableTriple<>("<>",relation, "<"+value+">"));
                    }
                } else {
                    triples.add(new ImmutableTriple<>("<>",relation, '"'+elm.getTextContent().trim()+'"'));
                }
            }
        }

        StringTemplate sparql = strGroup.getInstanceOf("relsext_sparql");
        sparql.setAttribute("triples",triples);
        return sparql.toString();
    }

    public static StringTemplateGroup SPARQL_TEMPLATES() throws IOException {
        InputStream stream = RELSEXTSPARQLBuilderImpl.class.getResourceAsStream("res/relsextsparql.stg");
        String string = org.apache.commons.io.IOUtils.toString(stream, Charset.forName("UTF-8"));
        return new StringTemplateGroup(new StringReader(string), DefaultTemplateLexer.class);
    }

}
