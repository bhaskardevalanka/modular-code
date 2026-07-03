package com.techvedika.harmonycvi.gateway.pacsproxy;

import org.springframework.core.io.ByteArrayResource;

public class BodyPart {

    private final ByteArrayResource resource;

    public BodyPart(ByteArrayResource resource) {
        this.resource = resource;
    }

    public ByteArrayResource getResource() {
        return resource;
    }

    public String getContentType() {
        return "application/dicom";
    }
}
