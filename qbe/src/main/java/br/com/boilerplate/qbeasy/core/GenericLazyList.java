package br.com.boilerplate.qbeasy.core;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import br.com.boilerplate.qbeasy.core.Example;
import br.com.boilerplate.qbeasy.core.QBEasy;
import br.com.boilerplate.qbeasy.model.interfaces.ExampleGenerator;
import br.com.boilerplate.qbeasy.model.interfaces.IdentifiableBySerial;

public class GenericLazyList<T extends IdentifiableBySerial> extends LazyDataModel<T> {
	private static final long serialVersionUID = 1L;
	protected EntityManager entityManager;
	
    protected List<T> lista;
    protected T pesquisa;
    private Example exemplo;
    
    public GenericLazyList(EntityManager entityManager, ExampleGenerator generator, T pesquisa) {
    	try {
    		this.entityManager = entityManager;
    		this.pesquisa = pesquisa;
    		exemplo = generator.generate(pesquisa);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    @Override
    public List<T> load(int first, int pageSize, String sortField,  SortOrder sortOrder, Map<String, String> filters) {
    	try {
	        if (getRowCount() <= 0) {
	        	Long rowCount = QBEasy.managedBy(entityManager).total(exemplo);
	        	int rowCountIntValue = Integer.valueOf(rowCount+"");
	            setRowCount(rowCountIntValue);
	            first = 0;
	        }
	        
	        String ordenacao = ordenarASCouDESC(sortOrder);
	        lista = QBEasy.managedBy(entityManager).getPaginatedList(exemplo, first, pageSize, ordenacao, sortField);
	
	        setPageSize(pageSize);
	        return lista;
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return null;
    }

    @Override
    public T getRowData(String sid) {
        Long id = Long.valueOf(sid);
        for (T entidade : lista) {
            if (id.equals(entidade.getId())) {
                return entidade;
            }
        }
        return null;
    }

    @Override
    public void setRowIndex(int rowIndex) {
        if (rowIndex == -1 || getPageSize() == 0) {
            super.setRowIndex(-1);
        } else {
            super.setRowIndex(rowIndex % getPageSize());
        }
    }

    protected String ordenarASCouDESC(SortOrder ordernarAscOuDesc) {
        if (SortOrder.UNSORTED.equals(ordernarAscOuDesc)
                || ordernarAscOuDesc.equals(SortOrder.DESCENDING)) {
            return "DESC";
        } else {
            return "ASC";
        }
    }
}
