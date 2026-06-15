package com.zoho.getemp.dto;

/*public class ZohoInsertResponse {

    private boolean success;
    private String zohoId;
    private String error;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getZohoId() {
        return zohoId;
    }

    public void setZohoId(String zohoId) {
        this.zohoId = zohoId;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}*/

public class ZohoInsertResponse {
    private boolean success;
    private String zohoId;
    private String error;
    private String rawResponse;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getZohoId() {
        return zohoId;
    }

    public void setZohoId(String zohoId) {
        this.zohoId = zohoId;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getRawResponse() {
        return rawResponse;
    }

    public void setRawResponse(String rawResponse) {
        this.rawResponse = rawResponse;
    }
}
