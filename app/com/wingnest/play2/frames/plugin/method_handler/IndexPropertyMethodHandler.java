/*
 * Copyright since 2013 Shigeru GOUGI (sgougi@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wingnest.play2.frames.plugin.method_handler;

import java.lang.reflect.Method;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.ClassUtilities;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.modules.MethodHandler;
import com.wingnest.play2.frames.GraphDB;
import com.wingnest.play2.frames.annotations.IndexedProperty;

public class IndexPropertyMethodHandler implements MethodHandler<IndexedProperty> {

	@Override
	public Class<IndexedProperty> getAnnotationType() {
		return IndexedProperty.class;
	}
	
	@Override
	public Object processElement(final Object frame, final Method method, final Object[] arguments, final IndexedProperty annotation, final FramedGraph<?> framedGraph, final Element element) {
		if( element instanceof Vertex ) {
			return processVertex( annotation, method, arguments, framedGraph, (Vertex)element);
		} else {
			return processEdge( annotation, method, arguments, framedGraph, (Edge)element);
		}	
	}

	private Object processVertex(final IndexedProperty annotation, final Method method, final Object[] arguments, final FramedGraph<?> framedGraph, final Vertex element) {
		return process(annotation, method, arguments, element);
	}

	private Object processEdge(final IndexedProperty annotation, final Method method, final Object[] arguments, final FramedGraph<?> framedGraph, final Edge element) {
		return process(annotation, method, arguments, element);
	}

	private <T extends Element> Object process(final IndexedProperty annotation, final Method method, final Object[] arguments, final T element) {
		final Index<T> index = (Index<T>) getIndex(element, method);
		if ( ClassUtilities.isGetMethod(method) ) {
			return element.getProperty(annotation.value());
		} else if ( ClassUtilities.isSetMethod(method) ) {

			removeFromIndex(annotation, element, index);
			Object value = arguments[0];
			if ( null == value ) {
				element.removeProperty(annotation.value());
			} else {
				index.put(annotation.value(), value, (T) element);
				element.setProperty(annotation.value(), value);
			}
			return null;
		} else if ( ClassUtilities.isRemoveMethod(method) ) {
			removeFromIndex(annotation, element, index);
			element.removeProperty(annotation.value());
			return null;
		}
		return null;
	}

	private <T extends Element> void removeFromIndex(final IndexedProperty annotation, final T element, final Index<T> index) {
		final Object value = element.getProperty(annotation.value());
		if ( value != null ) {
			index.remove(annotation.value(), element.getProperty(annotation.value()), (T) element);
		}
	}

	private <T extends Element> Index<T> getIndex(T element, final Method method) {
		final Class<T> clazz = (Class<T>) element.getClass();
		final String indexName = (String) method.getDeclaringClass().getSimpleName();
		Index<T> index = GraphDB.getIndex(indexName, clazz);
		if ( index == null ) {
			GraphDB.createIndex(indexName, clazz);
			index = GraphDB.getIndex(indexName, clazz);
		}
		return index;
	}

}
