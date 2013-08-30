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
package com.wingnest.play2.frames.plugin;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import play.Application;
import play.Play;
import play.Plugin;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.annotations.AnnotationHandler;
import com.wingnest.play2.frames.GraphDB;
import com.wingnest.play2.frames.plugin.annotation_handler.DatePropertyAnnotationHandler;
import com.wingnest.play2.frames.plugin.annotation_handler.GremlinGroovyExAnnotationHandler;
import com.wingnest.play2.frames.plugin.annotation_handler.IdAnnotationHandler;
import com.wingnest.play2.frames.plugin.annotation_handler.IndexPropertyAnnotationHandler;
import com.wingnest.play2.frames.plugin.framedgraph.FramedGraphDirector;
import com.wingnest.play2.frames.plugin.utils.TypeUtils;

public abstract class PluginBase extends Plugin {
	
	final protected Application application;
	final protected static Set<AnnotationHandler<? extends Annotation>> ANNOTATION_HANDLERS = new HashSet<AnnotationHandler<? extends Annotation>>();

	protected static FramedGraphDirector<? extends FramedGraph<? extends Graph>> FRAMED_GRAPH_DIRECTOR;
	
	//

	public PluginBase(final Application application) {
		this.application = application;		
	}

	@Override
	final public void onStart() {
		onBeginStart();
		if ( FRAMED_GRAPH_DIRECTOR == null ) {
			FRAMED_GRAPH_DIRECTOR = createFramedGraphDirector();
			FramesLogger.info("add shutdown hook");
			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					if(FRAMED_GRAPH_DIRECTOR != null) {
						FramesLogger.info("shutdown hook was called");						
						System.out.println("[FramesPlugin:system out] shutdown hook was called");
						FRAMED_GRAPH_DIRECTOR.onShutdown();
					}
				}
			});			
		} else {
			FRAMED_GRAPH_DIRECTOR.onRestart();
		}
		
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
	
	protected void onBeginStart() {
	}
	
	protected void onEndStart() {
	}	
	
	protected void registerAnnotations() {
		ANNOTATION_HANDLERS.clear();
		onRegisterAnnotations(ANNOTATION_HANDLERS);
		if(isEnableRegisterAnnotationHandlers()) {
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
		}
		ANNOTATION_HANDLERS.add(new IdAnnotationHandler());
		ANNOTATION_HANDLERS.add(new IndexPropertyAnnotationHandler());
		ANNOTATION_HANDLERS.add(new GremlinGroovyExAnnotationHandler());
		ANNOTATION_HANDLERS.add(new DatePropertyAnnotationHandler());
	}
	
	public static boolean isEnableRegisterAnnotationHandlers() {
		return Play.application().configuration().getBoolean("frames.enable.register.annotation.handlers", false);
	}
	
}
