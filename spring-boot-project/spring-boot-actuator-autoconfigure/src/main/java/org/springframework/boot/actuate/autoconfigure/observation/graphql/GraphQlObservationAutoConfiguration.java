/*
 * Copyright 2012-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.actuate.autoconfigure.observation.graphql;

import graphql.GraphQL;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.propagation.Propagator;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.autoconfigure.observation.ObservationAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.graphql.execution.GraphQlSource;
import org.springframework.graphql.observation.DataFetcherObservationConvention;
import org.springframework.graphql.observation.ExecutionRequestObservationConvention;
import org.springframework.graphql.observation.GraphQlObservationInstrumentation;
import org.springframework.graphql.observation.PropagationWebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlHandler;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for instrumentation of Spring
 * GraphQL endpoints.
 *
 * @author Brian Clozel
 * @since 3.0.0
 */
@AutoConfiguration(after = ObservationAutoConfiguration.class)
@ConditionalOnBean(ObservationRegistry.class)
@ConditionalOnClass({ GraphQL.class, GraphQlSource.class, Observation.class })
@SuppressWarnings("removal")
public class GraphQlObservationAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public GraphQlObservationInstrumentation graphQlObservationInstrumentation(ObservationRegistry observationRegistry,
			ObjectProvider<ExecutionRequestObservationConvention> executionConvention,
			ObjectProvider<DataFetcherObservationConvention> dataFetcherConvention) {
		return new GraphQlObservationInstrumentation(observationRegistry, executionConvention.getIfAvailable(),
				dataFetcherConvention.getIfAvailable());
	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(Propagator.class)
	@ConditionalOnBean(WebGraphQlHandler.class)
	static class TracingObservationConfiguration {

		@Bean
		@ConditionalOnBean(Propagator.class)
		@ConditionalOnMissingBean
		@Order(Ordered.HIGHEST_PRECEDENCE + 1)
		PropagationWebGraphQlInterceptor propagationWebGraphQlInterceptor(Propagator propagator) {
			return new PropagationWebGraphQlInterceptor(propagator);
		}

	}

}
