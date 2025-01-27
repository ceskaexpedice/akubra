package org.ceskaexpedice.akubra.access;

public class RelsExtLiteral extends RelsExtItem{
    private String content;

    public RelsExtLiteral(String namespace, String localName, String content) {
        super(namespace, localName);
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return super.toString() + " " + content;
    }
}
