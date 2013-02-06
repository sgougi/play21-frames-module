/*
 * Copyright since 2013 Shigeru GOUGI
 *                              e-mail:  sgougi@gmail.com
 *                              twitter: @igerugo
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
package com.wingnest.play2.frames.plugin;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.frames.FramedGraph;
import com.wingnest.play2.frames.GraphDB;
import com.wingnest.play2.frames.plugin.annotation_handler.*;
import com.wingnest.play2.frames.plugin.framedgraph.FramedGraphDirector;
import com.wingnest.play2.frames.plugin.utils.TypeUtils;
import com.tinkerpop.frames.annotations.AnnotationHandler;
import play.Application;
import play.Plugin;

public abstract class PluginBase extends Plugin {
	
	final protected Application application;
	final protected static Set<AnnotationHandler<? extends Annotation>> ANNOTATION_HANDLERS = new HashSet<AnnotationHandler<? extends Annotation>>();

	protected static FramedGraphDirector<? extends FramedGraph<? extends Graph>> FRAMED_GRAPH_DIRECTOR;
	
	//

	public PluginBase(final Application application) {
		this.application = application;
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				if(FRAMED_GRAPH_DIRECTOR != null)
					FRAMED_GRAPH_DIRECTOR.onShutdown();
			}
		});			
	}

	@Override
	final public void onStart() {
		onBeingStart();
		if ( FRAMED_GRAPH_DIRECTOR == null )
			FRAMED_GRAPH_DIRECTOR = createFramedGraphDirector();
		else
			FRAMED_GRAPH_DIRECTOR.onRestart();
		
		registerAnnotations();
		GraphDB.setRawGraphDB(new RawGraphDB(){
			@Override
			public <T extends FramedGraph<? extends Graph>> FramedGraphDirector<T> getFramedGraphDirector() {
				return (FramedGraphDirector<T>)PluginBase.FRAMED_GRAPH_DIRECTOR;
			}
			@Override
			public Set<AnnotationHandler<? extends Annotation>> getAnnotationHandlers() {
				return (Set<AnnotationHandler<? extends Annotation>>)ANNOTATION_HANDLERS;
			}});
		onEndStart();		
	}
	
	@Override
	final public void onStop() {
		if(FRAMED_GRAPH_DIRECTOR != null)
			FRAMED_GRAPH_DIRECTOR.onStop();		
	}
	
	protected abstract <T extends FramedGraph<? extends Graph>> FramedGraphDirector<T> createFramedGraphDirector();

	protected void onRegisterAnnotations(final Set<AnnotationHandler<? extends Annotation>> annotationHandlers) {
	}
	
	protected void onBeingStart() {
	}
	
	protected void onEndStart() {
	}	
	
	protected void registerAnnotations() {
		ANNOTATION_HANDLERS.clear();
		onRegisterAnnotations(ANNOTATION_HANDLERS);
		@SuppressWarnings("rawtypes")
		final Set<Class<AnnotationHandler>> handlerClasses = TypeUtils.getSubTypesOf(application, "handlers", AnnotationHandler.class);

		for ( @SuppressWarnings("rawtypes")
		final Class<AnnotationHandler> javaClass : handlerClasses ) {
			if ( AnnotationHandler.class.isAssignableFrom(javaClass) ) {
				FramesLogger.info("register AnnotationHandler: %s", javaClass.getName());
				final AnnotationHandler<? extends Annotation> handler;
				try {
					@SuppressWarnings("unchecked")
					final AnnotationHandler<? extends Annotation> whandler = (AnnotationHandler<? extends Annotation>) javaClass.newInstance();
					handler = whandler;
					ANNOTATION_HANDLERS.add(handler);
				} catch ( Exception e ) {
					FramesLogger.error(e, e.getMessage());
				}
			}
		}
		ANNOTATION_HANDLERS.add(new IdAnnotationHandler());
		ANNOTATION_HANDLERS.add(new IndexPropertyAnnotationHandler());
		ANNOTATION_HANDLERS.add(new GremlinGroovyExAnnotationHandler());
		ANNOTATION_HANDLERS.add(new DatePropertyAnnotationHandler());
	}
	
}
