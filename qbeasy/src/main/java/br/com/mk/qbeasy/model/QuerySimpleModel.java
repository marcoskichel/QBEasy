package br.com.mk.qbeasy.model;

import java.util.HashMap;
import java.util.Map;

public class QuerySimpleModel {
	private String queryString;
	private Map<String, Object> params;
	
	public QuerySimpleModel() {
		super();
		this.params = new HashMap<String, Object>();
	}

	public String getQueryString() {
		return queryString;
	}

	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public void setParams(Map<String, Object> params) {
		this.params = params;
	}
}
