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
package com.wingnest.play2.frames.plugin.annotation_handler;

import java.lang.reflect.Method;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.ClassUtilities;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.annotations.AnnotationHandler;
import com.tinkerpop.frames.structures.FramedVertexIterable;
import com.tinkerpop.gremlin.groovy.Gremlin;
import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.util.iterators.SingleIterator;
import com.wingnest.play2.frames.annotations.GremlinGroovyEx;

public class GremlinGroovyExAnnotationHandler implements AnnotationHandler<GremlinGroovyEx> {

	@Override
	public Object processElement(final GremlinGroovyEx annotation, final Method method, final Object[] arguments, final FramedGraph framedGraph, final Element element, final Direction direction) {
		if( element instanceof Vertex) {
			return processVertex(annotation, method, arguments, framedGraph, (Vertex)element);
		} else {
			return processEdge(annotation, method, arguments, framedGraph, (Edge)element, direction);
		}
	}
	
	@Override
	public Class<GremlinGroovyEx> getAnnotationType() {
		return GremlinGroovyEx.class;
	}

	private Object processVertex(final GremlinGroovyEx annotation, final Method method, final Object[] arguments, final FramedGraph framedGraph, final Vertex vertex) {

		if ( ClassUtilities.isGetMethod(method) ) {
			final Pipe pipe = Gremlin.compile(annotation.value());
			pipe.setStarts(new SingleIterator<Element>(vertex));
			if ( ClassUtilities.returnsIterable(method) ) {
				return new FramedVertexIterable(framedGraph, pipe, ClassUtilities.getGenericClass(method));
			} else {
				if ( !pipe.iterator().hasNext() )
					return null;
				return framedGraph.frame((Vertex) pipe.iterator().next(), ClassUtilities.getGenericClass(method));
			}
		} else {
			throw new UnsupportedOperationException("Gremlin only works with getters");
		}

	}

	private Object processEdge(final GremlinGroovyEx annotation, final Method method, final Object[] arguments, final FramedGraph framedGraph, final Edge edge, final Direction direction) {
		throw new UnsupportedOperationException("This method only works for vertices");
	}
}
