/*
 * Copyright 2012-2023 the original author or authors.
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

package org.springframework.boot.testcontainers.service.connection;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.testcontainers.containers.Container;

import org.springframework.boot.origin.Origin;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.ContextCustomizerFactory;
import org.springframework.test.context.TestContextAnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * Spring Test {@link ContextCustomizerFactory} to support
 * {@link ServiceConnection @ServiceConnection} annotated {@link Container} fields in
 * tests.
 *
 * @author Moritz Halbritter
 * @author Andy Wilkinson
 * @author Phillip Webb
 */
class ServiceConnectionContextCustomizerFactory implements ContextCustomizerFactory {

	@Override
	public ContextCustomizer createContextCustomizer(Class<?> testClass,
			List<ContextConfigurationAttributes> configAttributes) {
		List<ContainerConnectionSource<?, ?>> sources = new ArrayList<>();
		findSources(testClass, sources);
		return (sources.isEmpty()) ? null : new ServiceConnectionContextCustomizer(sources);
	}

	private void findSources(Class<?> clazz, List<ContainerConnectionSource<?, ?>> sources) {
		ReflectionUtils.doWithFields(clazz, (field) -> {
			MergedAnnotations annotations = MergedAnnotations.from(field);
			annotations.stream(ServiceConnection.class)
				.forEach((annotation) -> sources.add(createSource(field, annotation)));
		});
		if (TestContextAnnotationUtils.searchEnclosingClass(clazz)) {
			findSources(clazz.getEnclosingClass(), sources);
		}
	}

	private ContainerConnectionSource<?, ?> createSource(Field field, MergedAnnotation<ServiceConnection> annotation) {
		Assert.state(Modifier.isStatic(field.getModifiers()),
				() -> "@ServiceConnection field '%s' must be static".formatted(field.getName()));
		String beanNameSuffix = StringUtils.capitalize(ClassUtils.getShortNameAsProperty(field.getDeclaringClass()))
				+ StringUtils.capitalize(field.getName());
		Origin origin = new FieldOrigin(field);
		Object fieldValue = getFieldValue(field);
		Assert.state(Container.class.isInstance(fieldValue), () -> "Field '%s' in %s must be a %s"
			.formatted(field.getName(), field.getDeclaringClass().getName(), Container.class.getName()));
		Container<?> container = (Container<?>) fieldValue;
		return new ContainerConnectionSource<>(beanNameSuffix, origin, container, annotation);
	}

	private Object getFieldValue(Field field) {
		ReflectionUtils.makeAccessible(field);
		return ReflectionUtils.getField(field, null);
	}

}
