package br.com.mk.qbeasy.model;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class Endereco extends EntidadeBase {

	@Column
	private String rua;
	
	@Column
	private String endereco;

	@Column
	private String cidade;
	
	@Column
	private String bairro;

	public String getRua() {
		return rua;
	}

	public void setRua(String rua) {
		this.rua = rua;
	}

	public String getEndereco() {
		return endereco;
	}

	public void setEndereco(String endereco) {
		this.endereco = endereco;
	}

	public String getCidade() {
		return cidade;
	}

	public void setCidade(String cidade) {
		this.cidade = cidade;
	}

	public String getBairro() {
		return bairro;
	}

	public void setBairro(String bairro) {
		this.bairro = bairro;
	}
}
