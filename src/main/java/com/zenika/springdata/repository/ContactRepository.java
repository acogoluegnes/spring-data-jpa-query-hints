/**
 * 
 */
package com.zenika.springdata.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.zenika.springdata.model.Contact;

/**
 * 
 * @author acogoluegnes
 *
 */
public interface ContactRepository extends JpaRepository<Contact,Long> {

	List<Contact> findByLastname(String lastname);
	
	@Query("select c from Contact c where c.firstname = ?1")
	List<Contact> findByFirstname(String firstname);
	
}
