package de.shop.kundeverwaltung.rest;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.FixMethodOrder;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
@FixMethodOrder(NAME_ASCENDING)
public class KundeRessourceTest {
	
	@Test
	public void validate() {
		assertThat(true, is(true));
	}

}
