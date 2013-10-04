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
package com.wingnest.play2.frames.plugin.framedgraph;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.FramedGraphConfiguration;
import com.tinkerpop.frames.FramedGraphFactory;
import com.tinkerpop.frames.annotations.gremlin.GremlinGroovyAnnotationHandler;
import com.tinkerpop.frames.modules.AbstractModule;
import com.tinkerpop.frames.modules.MethodHandler;
import com.wingnest.play2.frames.GraphDB;
import com.wingnest.play2.frames.plugin.graphManager.GraphManager;
import com.tinkerpop.frames.FrameInitializer;

public class DefaultFramedGraphDirector implements FramedGraphDirector<FramedGraph<Graph>> {

	private GremlinGroovyAnnotationHandler gremlinGroovyHandler = new GremlinGroovyAnnotationHandler(); 
	
	private final GraphManager manager;

	public DefaultFramedGraphDirector(final GraphManager manager) {
		this.manager = manager;
	}

	@Override
	public FramedGraph<Graph> createFramedGraph() {
		return new FramedGraphFactory(new AbstractModule() {
			public void doConfigure(FramedGraphConfiguration config) {
				for ( MethodHandler<?> mh : GraphDB.getGraphDBConfiguration().getMethodHandlers() ) {
					config.addMethodHandler(mh);
				}
				config.addMethodHandler(gremlinGroovyHandler);
				for ( FrameInitializer fi: GraphDB.getGraphDBConfiguration().getFrameInitializers() ) {
					config.addFrameInitializer(fi);
				}
			}
		}).create(manager.getGraph());
	}

	@Override
	public GraphManager getGraphManager() {
		return manager;
	}

	@Override
	public void onShutdown() {
		if(manager != null) manager.onShutdown();
	}

	@Override
	public void onRestart() {
		if(manager != null) manager.onRestart();
	}
	
	@Override
	public void onStop() {
		if(manager != null) manager.onStop();
	}	
	
}
