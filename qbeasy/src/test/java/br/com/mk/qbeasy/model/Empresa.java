package br.com.mk.qbeasy.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

@Entity
public class Empresa extends EntidadeBase {
	
	@OneToOne
	@JoinColumn(name = "id_endereco")
	private Endereco endereco;
	
	@Column
	private String razao;

	@OneToMany(mappedBy = "empresa")
	private List<Cliente> clientes;
	
}
