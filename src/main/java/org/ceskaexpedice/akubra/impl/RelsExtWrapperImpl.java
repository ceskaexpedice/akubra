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
package org.ceskaexpedice.akubra.impl;

import org.apache.commons.lang3.tuple.Triple;
import org.ceskaexpedice.akubra.DatastreamContentWrapper;
import org.ceskaexpedice.akubra.relsext.RelsExtLiteral;
import org.ceskaexpedice.akubra.relsext.RelsExtRelation;
import org.ceskaexpedice.akubra.relsext.RelsExtWrapper;
import org.ceskaexpedice.akubra.utils.Dom4jUtils;
import org.ceskaexpedice.akubra.utils.DomUtils;
import org.ceskaexpedice.akubra.utils.RelsExtUtils;
import org.ceskaexpedice.akubra.utils.StringUtils;
import org.w3c.dom.Document;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * RelsExtWrapperImpl
 */
// TODO AK_NEW consider rewriting all input stream methods to SAX
class RelsExtWrapperImpl implements RelsExtWrapper {

    private final DatastreamContentWrapper datastreamContentWrapper;
    private Document domContent;

    RelsExtWrapperImpl(DatastreamContentWrapper datastreamContentWrapper) {
        this.datastreamContentWrapper = datastreamContentWrapper;
    }

    @Override
    public InputStream asInputStream() {
        return datastreamContentWrapper.asInputStream();
    }

    @Override
    public Document asDom(boolean nsAware) {
        if(domContent == null) {
            domContent = DomUtils.streamToDocument(asInputStream(), nsAware);
        }
        return domContent;
    }

    @Override
    public org.dom4j.Document asDom4j(boolean nsAware) {
        return Dom4jUtils.streamToDocument(asInputStream(), nsAware);
    }

    @Override
    public String asString() {
        return StringUtils.streamToString(asInputStream());
    }

    @Override
    public List<RelsExtRelation> getRelations(String namespace) {
        List<RelsExtRelation> rels = new ArrayList<>();
        List<Triple<String, String, String>> triples = RelsExtUtils.relsExtGetRelations(asDom(true), namespace);
        for (Triple<String, String, String> triple : triples) {
            RelsExtRelation relsExtRelation = new RelsExtRelation(triple.getLeft(), triple.getMiddle(), triple.getRight());
            rels.add(relsExtRelation);
        }
        return rels;
    }

    @Override
    public List<RelsExtLiteral> getLiterals(String namespace) {
        List<RelsExtLiteral> rels = new ArrayList<>();
        List<Triple<String, String, String>> triples = RelsExtUtils.relsExtGetLiterals(asDom(true), namespace);
        for (Triple<String, String, String> triple : triples) {
            RelsExtLiteral relsExtLiteral = new RelsExtLiteral(triple.getLeft(), triple.getMiddle(), triple.getRight());
            rels.add(relsExtLiteral);
        }
        return rels;
    }

}
