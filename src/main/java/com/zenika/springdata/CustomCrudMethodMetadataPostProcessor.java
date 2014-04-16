/**
 * 
 */
package com.zenika.springdata;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.LockModeType;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.interceptor.ExposeInvocationInterceptor;
import org.springframework.aop.target.AbstractLazyCreationTargetSource;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.support.CrudMethodMetadata;
import org.springframework.data.jpa.repository.support.CrudMethodMetadataProvider;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

/**
 * @author acogoluegnes
 *
 */
public class CustomCrudMethodMetadataPostProcessor implements CrudMethodMetadataProvider {
	
	private final QueryHintsProvider queryHintsProvider;
	
	public CustomCrudMethodMetadataPostProcessor(QueryHintsProvider queryHintsProvider) {
		this.queryHintsProvider = queryHintsProvider;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.core.support.RepositoryProxyPostProcessor#postProcess(org.springframework.aop.framework.ProxyFactory, org.springframework.data.repository.core.RepositoryInformation)
	 */
	@Override
	public void postProcess(ProxyFactory factory, RepositoryInformation repositoryInformation) {

		factory.addAdvice(ExposeInvocationInterceptor.INSTANCE);
		factory.addAdvice(new CrudMethodMetadataPopulatingMethodInterceptor(queryHintsProvider));
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.jpa.repository.support.CrudMethodMetadataProvider#getLockMetadataProvider()
	 */
	public CrudMethodMetadata getLockMetadataProvider() {

		ProxyFactory factory = new ProxyFactory();

		factory.addInterface(CrudMethodMetadata.class);
		factory.setTargetSource(new ThreadBoundTargetSource());

		return (CrudMethodMetadata) factory.getProxy();
	}

	/**
	 * {@link MethodInterceptor} to build and cache {@link DefaultCrudMethodMetadata} instances for the invoked
	 * methods. Will bind the found information to a {@link TransactionSynchronizationManager} for later lookup.
	 * 
	 * @see DefaultCrudMethodMetadata
	 * @author Oliver Gierke
	 */
	private static class CrudMethodMetadataPopulatingMethodInterceptor implements MethodInterceptor {

		private final Map<Method, CrudMethodMetadata> metadataCache = new HashMap<Method, CrudMethodMetadata>();
		
		private final QueryHintsProvider queryHintsProvider;
		
		public CrudMethodMetadataPopulatingMethodInterceptor(QueryHintsProvider queryHintsProvider) {
			this.queryHintsProvider = queryHintsProvider;
		}

		/* 
		 * (non-Javadoc)
		 * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
		 */
		public Object invoke(MethodInvocation invocation) throws Throwable {

			Method method = invocation.getMethod();
			Object metadata = TransactionSynchronizationManager.getResource(method);

			if (metadata != null) {
				return invocation.proceed();
			}

			CrudMethodMetadata methodMetadata = metadataCache.get(method);

			if (methodMetadata == null) {
				methodMetadata = new DefaultCrudMethodMetadata(method,queryHintsProvider);
				metadataCache.put(method, methodMetadata);
			}

			TransactionSynchronizationManager.bindResource(method, methodMetadata);

			try {
				return invocation.proceed();
			} finally {
				TransactionSynchronizationManager.unbindResource(method);
			}
		}
	}

	/**
	 * Default implementation of {@link CrudMethodMetadata} that will inspect the backing method for annotations.
	 * 
	 * @author Oliver Gierke
	 */
	private static class DefaultCrudMethodMetadata implements CrudMethodMetadata {

		private final LockModeType lockModeType;
		private final Map<String, Object> queryHints;

		/**
		 * Creates a new {@link DefaultCrudMethodMetadata} foir the given {@link Method}.
		 * 
		 * @param method must not be {@literal null}.
		 */
		public DefaultCrudMethodMetadata(Method method, QueryHintsProvider queryHintsProvider) {

			Assert.notNull(method, "Method must not be null!");

			this.lockModeType = findLockModeType(method);
			this.queryHints = queryHintsProvider.getQueryHintsAsMap(method);
		}

		private static final LockModeType findLockModeType(Method method) {

			Lock annotation = AnnotationUtils.findAnnotation(method, Lock.class);
			return annotation == null ? null : (LockModeType) AnnotationUtils.getValue(annotation);
		}

		/* 
		 * (non-Javadoc)
		 * @see org.springframework.data.jpa.repository.support.CrudMethodMetadata#getLockModeType()
		 */
		@Override
		public LockModeType getLockModeType() {
			return lockModeType;
		}

		/* 
		 * (non-Javadoc)
		 * @see org.springframework.data.jpa.repository.support.CrudMethodMetadata#getQueryHints()
		 */
		@Override
		public Map<String, Object> getQueryHints() {
			return queryHints;
		}
	}

	private static class ThreadBoundTargetSource extends AbstractLazyCreationTargetSource {

		/* 
		 * (non-Javadoc)
		 * @see org.springframework.aop.target.AbstractLazyCreationTargetSource#createObject()
		 */
		@Override
		protected Object createObject() throws Exception {

			MethodInvocation invocation = ExposeInvocationInterceptor.currentInvocation();
			return TransactionSynchronizationManager.getResource(invocation.getMethod());
		}
	}
}
