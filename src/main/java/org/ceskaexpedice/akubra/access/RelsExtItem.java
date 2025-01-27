package org.ceskaexpedice.akubra.access;

public abstract class RelsExtItem {
    private String namespace;
    private String localName;

    public RelsExtItem(String namespace, String localName) {
        this.namespace = namespace;
        this.localName = localName;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getLocalName() {
        return localName;
    }

    @Override
    public String toString() {
        return namespace + " " + localName;
    }
}
