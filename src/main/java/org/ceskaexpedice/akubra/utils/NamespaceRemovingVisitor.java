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
package org.ceskaexpedice.akubra.utils;

import org.dom4j.*;

public class NamespaceRemovingVisitor extends VisitorSupport {
    private final boolean removeNsFromElements;
    private final boolean removeNsFromAttributes;
    private Namespace from;
    private Namespace to;

    public NamespaceRemovingVisitor(boolean removeNsFromElements, boolean removeNsFromAttributes) {
        this.removeNsFromElements = removeNsFromElements;
        this.removeNsFromAttributes = removeNsFromAttributes;
    }

    public void visit(Element element) {
        if (removeNsFromElements) {
            if (!Namespace.NO_NAMESPACE.equals(element.getNamespace())) {
                QName newQName = new QName(element.getName(), Namespace.NO_NAMESPACE);
                element.setQName(newQName);
            }
        }
    }

    public void visit(Attribute attribute) {
        if (removeNsFromAttributes) {
            Element parent = attribute.getParent();
            String value = attribute.getValue();
            parent.remove(attribute);
            parent.addAttribute(new QName(attribute.getName(), Namespace.NO_NAMESPACE), value);
        }
    }
}
