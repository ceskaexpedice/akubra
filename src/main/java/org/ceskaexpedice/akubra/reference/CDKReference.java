package org.ceskaexpedice.akubra.reference;

public class CDKReference implements Reference {

    private final String value;

    public CDKReference(String value) {
        this.value = value;
    }

    @Override
    public String getValueForSPARQLListener() {
        return getPrefixedPid();
    }

    @Override
    public String getPid() {
        return value.startsWith(CDKReferenceType.CDK_PREFIX) ?
                value.substring(CDKReferenceType.CDK_PREFIX.length()) : value;
    }

    @Override
    public String getPrefixedPid() {
        return value.startsWith(CDKReferenceType.CDK_PREFIX) ?
                value : CDKReferenceType.CDK_PREFIX + value;
    }

}