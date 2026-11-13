package org.ceskaexpedice.akubra.reference;

public class CDKReferenceType implements ReferenceType {
    public static final String CDK_PREFIX = "cdk/";

    @Override
    public boolean supports(String value) {
        return value != null && value.startsWith(CDK_PREFIX);
    }

    @Override
    public Reference create(String value) {
        return new CDKReference(value);
    }
}