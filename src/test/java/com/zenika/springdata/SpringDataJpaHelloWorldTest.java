/**
 * 
 */
package com.zenika.springdata;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.zenika.springdata.model.Contact;
import com.zenika.springdata.repository.ContactRepository;

/**
 * @author acogoluegnes
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/spring-data-jpa-hello-world.xml")
public class SpringDataJpaHelloWorldTest {

	@Autowired ContactRepository repo;
	@Autowired SimpleQueryHintsProvider queryHintsProvider;

	@Test public void springJpa() {
		Contact contact = new Contact();
		contact.setFirstname("Mickey");
		contact.setLastname("Mouse");
		long initialCount = repo.count();
		repo.save(contact);
		Assert.assertEquals(initialCount + 1, repo.count());
		repo.findAll();
		repo.findByLastname("Mouse");
		repo.findByLastname("Mouse");
		repo.findByFirstname("Mickey");
		
		Assert.assertEquals(5,queryHintsProvider.getCalls().size());
		Assert.assertTrue(queryHintsProvider.getCalls().contains("save"));
		Assert.assertTrue(queryHintsProvider.getCalls().contains("count"));
		Assert.assertTrue(queryHintsProvider.getCalls().contains("findAll"));
		Assert.assertTrue(queryHintsProvider.getCalls().contains("findByLastname"));
		Assert.assertTrue(queryHintsProvider.getCalls().contains("findByFirstname"));
	}
	

}
