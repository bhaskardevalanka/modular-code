package com.techvedika.harmonycvi.gateway.dto;

public class UploadPart {

    private int partNumber;
    private String eTag;
	public UploadPart(int partNumber, String eTag) {
		this.partNumber = partNumber;
		this.eTag = eTag;
	}
	public int getPartNumber() {
		return partNumber;
	}
	public void setPartNumber(int partNumber) {
		this.partNumber = partNumber;
	}
	public String geteTag() {
		return eTag;
	}
	public void seteTag(String eTag) {
		this.eTag = eTag;
	}

    // getters + setters
    
}