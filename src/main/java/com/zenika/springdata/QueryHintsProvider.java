/**
 * 
 */
package com.zenika.springdata;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import javax.persistence.QueryHint;

/**
 * @author acogoluegnes
 *
 */
public interface QueryHintsProvider {

	List<QueryHint> getQueryHints(Method method);
	
	Map<String, Object> getQueryHintsAsMap(Method method);
	
}
