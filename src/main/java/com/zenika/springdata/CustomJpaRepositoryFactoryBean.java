package com.zenika.springdata;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.QueryHint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.query.JpaQueryLookupStrategy;
import org.springframework.data.jpa.repository.query.JpaQueryMethod;
import org.springframework.data.jpa.repository.query.JpaQueryMethodFactory;
import org.springframework.data.jpa.repository.query.QueryExtractor;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryLookupStrategy.Key;
import org.springframework.util.ReflectionUtils;

public class CustomJpaRepositoryFactoryBean extends JpaRepositoryFactoryBean {
	
	@Autowired QueryHintsProvider queryHintsProvider;
	
	@Override
	protected RepositoryFactorySupport createRepositoryFactory(
			EntityManager entityManager) {
		
		return new CustomRepositoryFactory(entityManager,queryHintsProvider);
	}
	
	private static class CustomRepositoryFactory extends JpaRepositoryFactory {
		
		private QueryHintsProvider queryHintsProvider;

		public CustomRepositoryFactory(EntityManager entityManager, QueryHintsProvider queryHintsProvider) {
			super(entityManager,new CustomCrudMethodMetadataPostProcessor(queryHintsProvider));
			this.queryHintsProvider = queryHintsProvider;
		}

		@Override
		protected QueryLookupStrategy getQueryLookupStrategy(Key key) {
			Field entityManagerField = ReflectionUtils.findField(JpaRepositoryFactory.class, "entityManager");
			entityManagerField.setAccessible(true);
			EntityManager entityManager = (EntityManager) ReflectionUtils.getField(entityManagerField, this);
			
			Field extractorField = ReflectionUtils.findField(JpaRepositoryFactory.class, "extractor");
			extractorField.setAccessible(true);
			QueryExtractor extractor = (QueryExtractor) ReflectionUtils.getField(extractorField, this);
			
			return JpaQueryLookupStrategy.create(entityManager, key, extractor, new JpaQueryMethodFactory() {
				
				@Override
				public JpaQueryMethod create(final Method method, RepositoryMetadata metadata,
						QueryExtractor extractor) {
					
					return new JpaQueryMethod(method, metadata, extractor) {
						
						@Override
						protected List<QueryHint> getHints() {
							return queryHintsProvider.getQueryHints(method);
						}
						
					};
				}
			});
		}
		
	}
	
}
