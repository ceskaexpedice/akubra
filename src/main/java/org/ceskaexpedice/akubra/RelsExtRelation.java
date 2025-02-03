package org.ceskaexpedice.akubra;

public class RelsExtRelation extends RelsExtItem{
    private String resource;

    public RelsExtRelation(String namespace, String localName, String resource) {
        super(namespace, localName);
        this.resource = resource;
    }

    public String getResource() {
        return resource;
    }

    @Override
    public String toString() {
        return super.toString() + " " + resource;
    }
}
