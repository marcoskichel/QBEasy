package br.com.mk.qbeasy.core;

import org.junit.Test;

public class QBEasyTest {
	
	private static final String QUERY_CLIENTE = 
			  "select cliente "
			+ "from Cliente "
			+ "join Empresa empresa "
			+ "where 1=1 and empresa.id = 1";
	
	@Test
	public void QueryClienteTest() {
//		EntityManager em = Mockito.mock(EntityManager.class);
//		Query query Mockito.when(em.createQuery(QUERY_CLIENTE)).thenReturn(Mockito.mock(Query.class));
	}
	
	private boolean compareTruncated(String one, String two) {
		return one.replaceAll(" ", "").compareTo(two.replaceAll(" ", "")) == 0;
	}
}
