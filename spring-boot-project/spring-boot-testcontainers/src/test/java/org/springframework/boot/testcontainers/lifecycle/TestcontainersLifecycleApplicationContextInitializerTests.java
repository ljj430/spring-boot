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

package org.springframework.boot.testcontainers.lifecycle;

import org.junit.jupiter.api.Test;
import org.testcontainers.lifecycle.Startable;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

/**
 * Tests for {@link TestcontainersLifecycleApplicationContextInitializer}.
 *
 * @author Stephane Nicoll
 */
class TestcontainersLifecycleApplicationContextInitializerTests {

	@Test
	void whenStartableBeanInvokesStartOnRefresh() {
		Startable container = mock(Startable.class);
		try (AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext()) {
			applicationContext.registerBean("container", Startable.class, () -> container);
			new TestcontainersLifecycleApplicationContextInitializer().initialize(applicationContext);
			then(container).shouldHaveNoInteractions();
			applicationContext.refresh();
			then(container).should().start();
		}
	}

	@Test
	void whenStartableBeanInvokesDestroyOnShutdown() {
		Startable mock = mock(Startable.class);
		AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
		applicationContext.registerBean("container", Startable.class, () -> mock);
		new TestcontainersLifecycleApplicationContextInitializer().initialize(applicationContext);
		applicationContext.refresh();
		then(mock).should(never()).close();
		applicationContext.close();
		then(mock).should().close();
	}

}
