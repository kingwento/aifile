package cn.xingyuan.cloud.model.utils;

import java.util.HashMap;
import java.util.Map;

public class Response {
    private int statusCode;
    private String contentType;
    private String errorMessage;
    private Map<String, String> headers;
    private String body;
    private String data;

    public Response() {
		
    }

	public Response(int statusCode) {
		this.statusCode = statusCode;
	}

	public Response(int statusCode,String errorMessage) {
    	this.statusCode = statusCode;
    	this.errorMessage = errorMessage;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}
	
	public String getHeader(String key) {
		if (null != headers) {
			return headers.get(key);
		} else {
			return null;
		}
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}
	
	public void setHeader(String key, String value) {
		if (null == this.headers) {
			this.headers = new HashMap<String, String>(); 
		}
		this.headers.put(key, value);
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getData() {
		return body;
	}

	public void setData(String data) {
		this.body = data;
	}
    
    
}
