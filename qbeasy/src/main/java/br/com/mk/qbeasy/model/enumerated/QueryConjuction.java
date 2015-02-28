package br.com.mk.qbeasy.model.enumerated;

public enum QueryConjuction {

	AND(" and "),
	OR(" or ");
	
	private String value;
	
	private QueryConjuction(String value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return value;
	}
}
