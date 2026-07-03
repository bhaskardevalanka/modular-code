package com.techvedika.harmonycvi.gateway.projection;

public interface OrgPacsValidationUrlProjection {
	String getName();
    String getPacsUrl();
    String getValidationUrl();
    Boolean getHasExternalPacs();
}