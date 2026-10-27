package org.ceskaexpedice.akubra.reference;

import org.ceskaexpedice.akubra.pid.PIDParser;

public class FedoraReference implements Reference {

    private final String value;

    public FedoraReference(String value) {
        this.value = value;
    }

    @Override
    public String getValueForSPARQLListener() {
        return getPid();
    }

    @Override
    public String getPid() {
        return value.startsWith(PIDParser.INFO_FEDORA_PREFIX) ?
                value.substring(PIDParser.INFO_FEDORA_PREFIX.length()) : value;
    }

    @Override
    public String getPrefixedPid() {
        return value.startsWith(PIDParser.INFO_FEDORA_PREFIX) ?
                value : PIDParser.INFO_FEDORA_PREFIX + value;
    }

}