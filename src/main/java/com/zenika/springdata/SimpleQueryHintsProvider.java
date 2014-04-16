/**
 * 
 */
package com.zenika.springdata;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.QueryHint;

/**
 * @author acogoluegnes
 *
 */
public class SimpleQueryHintsProvider implements QueryHintsProvider {
	
	// pour le test d'intégration
	private Set<String> calls = new HashSet<String>();

	@Override
	public List<QueryHint> getQueryHints(Method method) {
		Map<String, Object> hints = getQueryHintsAsMap(method);
		List<QueryHint> hintsAsAnnotations;
		if(hints.isEmpty()) {
			hintsAsAnnotations = Collections.emptyList();
		} else {
			hintsAsAnnotations = new ArrayList<QueryHint>();
			for(Map.Entry<String, Object> entry : hints.entrySet()) {
				hintsAsAnnotations.add(new SimpleQueryHint(entry.getKey(), entry.getValue().toString()));
			}
		}
		return hintsAsAnnotations;
	}

	@Override
	public Map<String, Object> getQueryHintsAsMap(Method method) {
		calls.add(method.getName());
		Map<String, Object> hints = new HashMap<String, Object>();
		return hints;
	}

	private static class SimpleQueryHint implements QueryHint {

		private final String name;
		private final String value;
		
		public SimpleQueryHint(String name, String value) {
			super();
			this.name = name;
			this.value = value;
		}

		@Override
		public Class<? extends Annotation> annotationType() {
			return QueryHint.class;
		}

		@Override
		public String name() {
			return name;
		}

		@Override
		public String value() {
			return value;
		}
		
	}

	public Set<String> getCalls() {
		return calls;
	}
	
}
