package com.techvedika.harmonycvi.gateway.pacsproxy;

public class DeleteStudyResponse {
    private boolean success;
    private String message;

    public DeleteStudyResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

	public boolean isSuccess() {
		return success;
	}
}
