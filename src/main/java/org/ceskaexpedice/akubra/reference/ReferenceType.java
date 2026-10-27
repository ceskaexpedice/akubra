package org.ceskaexpedice.akubra.reference;

public interface ReferenceType {
    boolean supports(String value);

    Reference create(String value);
}