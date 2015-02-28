package br.com.mk.qbeasy.model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import br.com.mk.qbeasy.model.enumerated.OperationType;
import br.com.mk.qbeasy.model.interfaces.annotations.QueryField;

@Entity
public class Cliente extends EntidadeBase {

	@OneToOne
	@JoinColumn(name = "id_pessoa")
	@QueryField(operationType = OperationType.JOIN)
	private Pessoa pessoa;
	
	@ManyToOne
	@JoinColumn(name = "id_empresa")
	private Empresa empresa;

	public Pessoa getPessoa() {
		return pessoa;
	}

	public void setPessoa(Pessoa pessoa) {
		this.pessoa = pessoa;
	}

	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}
	
}
