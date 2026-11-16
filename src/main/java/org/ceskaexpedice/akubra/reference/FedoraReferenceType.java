package org.ceskaexpedice.akubra.reference;

import org.ceskaexpedice.akubra.pid.PIDParser;

public class FedoraReferenceType implements ReferenceType {

    @Override
    public boolean supports(String value) {
        return value != null && value.startsWith(PIDParser.INFO_FEDORA_PREFIX);
    }

    @Override
    public Reference create(String value) {
        return new FedoraReference(value);
    }
}