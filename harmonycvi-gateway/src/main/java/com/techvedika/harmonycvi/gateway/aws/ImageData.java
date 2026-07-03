package com.techvedika.harmonycvi.gateway.aws;

public class ImageData {
	private String seriesDescription;
	private String seriesType;
	private String sopId;
	private String imageType;
	private String seriesId;
	private String imageString;

	private String key;

	private int imageOrder;

	public String getSeriesDescription() {
		return seriesDescription;
	}

	public void setSeriesDescription(String seriesDescription) {
		this.seriesDescription = seriesDescription;
	}

	public String getSeriesType() {
		return seriesType;
	}

	public void setSeriesType(String seriesType) {
		this.seriesType = seriesType;
	}

	public String getSopId() {
		return sopId;
	}

	public void setSopId(String sopId) {
		this.sopId = sopId;
	}

	public String getImageType() {
		return imageType;
	}

	public void setImageType(String imageType) {
		this.imageType = imageType;
	}

	public String getSeriesId() {
		return seriesId;
	}

	public void setSeriesId(String seriesId) {
		this.seriesId = seriesId;
	}

	public String getImageString() {
		return imageString;
	}

	public void setImageString(String imageString) {
		this.imageString = imageString;
	}

	public int getImageOrder() {
		return imageOrder;
	}

	public void setImageOrder(int imageOrder) {
		this.imageOrder = imageOrder;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
}