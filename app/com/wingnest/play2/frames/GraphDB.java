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
package com.wingnest.play2.frames;

import java.lang.annotation.Annotation;
import java.util.Set;

import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.IndexableGraph;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.Parameter;
import com.tinkerpop.blueprints.TransactionalGraph.Conclusion;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.annotations.AnnotationHandler;
import com.wingnest.play2.frames.plugin.FramesLogger;
import com.wingnest.play2.frames.plugin.RawGraphDB;
import com.wingnest.play2.frames.plugin.graphManager.GraphManager;


final public class GraphDB {
	
	private static RawGraphDB rawGraphDB; 

	private static RawGraphDB getRawGraphDB() {
		return rawGraphDB;
	}
	
	public static void setRawGraphDB(final RawGraphDB graphDBRaw) {
		GraphDB.rawGraphDB = graphDBRaw;
	}	
	
	public static void commit() {
		getGraphManager().stopTransaction(Conclusion.SUCCESS);
	}
	
	public static void rollback() {
		getGraphManager().stopTransaction(Conclusion.FAILURE);
	}	

	public static void shutdown() {
		getGraphManager().onShutdown();
	}
	
	public static <T extends Graph> T getGraph() {
		return (T)getRawGraphDB().getFramedGraphDirector().getGraphManager().getGraph();
	}
	
	public static <T extends Graph> FramedGraph<T> createFramedGraph() {
		final FramedGraph<T> framedGraph = (FramedGraph<T>)getRawGraphDB().getFramedGraphDirector().createFramedGraph();
		registerAnnotations(framedGraph);
		return framedGraph;
	}

	public static GraphManager getGraphManager() {
		return getRawGraphDB().getFramedGraphDirector().getGraphManager();
	}

	private static <T extends Graph> void registerAnnotations(final FramedGraph<T> framedGraph) {
		for ( AnnotationHandler<? extends Annotation> handler : getRawGraphDB().getAnnotationHandlers() ) {
			framedGraph.registerAnnotationHandler(handler);
		}
	}

	public static <T extends Element> void dropKeyIndex(final String key, final Class<T> elementClass) {
		final Graph graph = getGraph();
		if ( graph instanceof KeyIndexableGraph ) {
			((KeyIndexableGraph) graph).<T> dropKeyIndex(key, elementClass);
		} else {
			FramesLogger.warn("IndexableGraph not supported");
		}
	}

	public static <T extends Element> void createKeyIndex(final String key, final Class<T> elementClass) {
		final Graph graph = getGraph();
		if ( graph instanceof KeyIndexableGraph ) {
			((KeyIndexableGraph) graph).<T> createKeyIndex(key, elementClass);
		} else {
			FramesLogger.warn("KeyIndexableGraph not supported");
		}
	}

	public static <T extends Element> Set<String> getIndexedKeys(final Class<T> elementClass) {
		final Graph graph = getGraph();
		if ( graph instanceof KeyIndexableGraph ) {
			return ((KeyIndexableGraph) graph).<T> getIndexedKeys(elementClass);
		} else {
			FramesLogger.warn("KeyIndexableGraph not supported");
		}
		return null;
	}

	public static void dropIndex(final String indexName) {
		final Graph graph = getGraph();
		if ( graph instanceof IndexableGraph ) {
			((IndexableGraph) graph).dropIndex(indexName);
		} else {
			FramesLogger.warn("IndexableGraph not supported");
		}
	}

	public static <T extends Element> void createIndex(final String indexName, final Class<T> indexClass, final Parameter... indexParameters) {
		final Graph graph = getGraph();
		if ( graph instanceof IndexableGraph ) {
			((IndexableGraph) graph).<T> createIndex(indexName, indexClass, indexParameters);
		} else {
			FramesLogger.warn("IndexableGraph not supported");
		}
	}

	public static <T extends Element> Index<T> getIndex(final String indexName, final Class<T> indexClass) {
		final Graph graph = getGraph();
		if ( graph instanceof IndexableGraph ) {
			return ((IndexableGraph) graph).getIndex(indexName, indexClass);
		} else {
			FramesLogger.warn("IndexableGraph not supported");
		}
		return null;
	}

	public static Iterable<Index<? extends Element>> getIndices() {
		final Graph graph = getGraph();
		if ( graph instanceof IndexableGraph ) {
			return ((IndexableGraph) graph).getIndices();
		} else {
			FramesLogger.warn("IndexableGraph not supported");
		}
		return null;
	}
}
