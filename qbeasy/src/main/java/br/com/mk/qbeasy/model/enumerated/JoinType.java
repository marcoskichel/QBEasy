package br.com.mk.qbeasy.model.enumerated;

public enum JoinType {
	INNER(" join "), LEFT(" left join "), RIGHT(" right join ");
	
	private final String value;

	private JoinType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return value;
	}
}
