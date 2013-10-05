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
import com.tinkerpop.frames.FrameInitializer;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.modules.MethodHandler;
import com.tinkerpop.frames.modules.TypeResolver;

import com.wingnest.play2.frames.GraphDB;
import com.wingnest.play2.frames.plugin.framedgraph.FramedGraphDirector;
import com.wingnest.play2.frames.plugin.method_handler.DatePropertyMethodHandler;
import com.wingnest.play2.frames.plugin.method_handler.IdMethodHandler;
import com.wingnest.play2.frames.plugin.method_handler.IndexPropertyMethodHandler;
import com.wingnest.play2.frames.plugin.utils.TypeUtils;

public abstract class PluginBase extends Plugin {
	
	final protected Application application;
	final protected static Set<MethodHandler<? extends Annotation>> METHOD_HANDLERS = new HashSet<MethodHandler<? extends Annotation>>();
	final protected static Set<FrameInitializer> FRAME_INITIALIZERS = new HashSet<FrameInitializer>();
	final protected static Set<TypeResolver> TYPE_RESOLVERS = new HashSet<TypeResolver>();

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
		registerInitializers();
		registerTypeResolvers();

		GraphDB.setGraphDBConfiguration(new GraphDBConfiguration(){
			@Override
			public <T extends FramedGraph<? extends Graph>> FramedGraphDirector<T> getFramedGraphDirector() {
				return (FramedGraphDirector<T>)PluginBase.FRAMED_GRAPH_DIRECTOR;
			}
			@Override
			public Set<MethodHandler<? extends Annotation>> getMethodHandlers() {
				return (Set<MethodHandler<? extends Annotation>>)METHOD_HANDLERS;
			}
			@Override
			public Set<FrameInitializer> getFrameInitializers() {
				return FRAME_INITIALIZERS;
			}			
		});
		onEndStart();		
	}
	
	@Override
	final public void onStop() {
		if(FRAMED_GRAPH_DIRECTOR != null)
			FRAMED_GRAPH_DIRECTOR.onStop();		
	}
	
	protected abstract <T extends FramedGraph<? extends Graph>> FramedGraphDirector<T> createFramedGraphDirector();

	protected void onRegisterAnnotations(final Set<MethodHandler<? extends Annotation>> annotationHandlers) {
	}
	protected void onRegisterInitializers(Set<FrameInitializer> frameInitializers) {
	}	
	protected void onRegisterTypeResolvers(Set<TypeResolver> typeResolvers) {
	}	
		
	protected void onBeginStart() {
	}	
	protected void onEndStart() {
	}	
	
	protected void registerAnnotations() {
		METHOD_HANDLERS.clear();
		onRegisterAnnotations(METHOD_HANDLERS);
		if(isEnableRegisterAnnotationHandlers()) {
			@SuppressWarnings("rawtypes")			
			final Set<Class<MethodHandler>> handlerClasses = TypeUtils.getSubTypesOf(application, getAnnotationHandlersPackageName(), MethodHandler.class);
			for ( @SuppressWarnings("rawtypes")
			final Class<MethodHandler> javaClass : handlerClasses ) {
				if ( MethodHandler.class.isAssignableFrom(javaClass) ) {
					FramesLogger.info("register AnnotationHandler: %s", javaClass.getName());
					final MethodHandler<? extends Annotation> handler;
					try {
						@SuppressWarnings("unchecked")
						final MethodHandler<? extends Annotation> whandler = (MethodHandler<? extends Annotation>) javaClass.newInstance();
						handler = whandler;
						METHOD_HANDLERS.add(handler);
					} catch ( Exception e ) {
						FramesLogger.error(e, e.getMessage());
					}
				}
			}
		}
		
		METHOD_HANDLERS.add(new IdMethodHandler());
		METHOD_HANDLERS.add(new IndexPropertyMethodHandler());
		METHOD_HANDLERS.add(new DatePropertyMethodHandler());
	}
	
	protected void registerInitializers() {
		FRAME_INITIALIZERS.clear();
		onRegisterInitializers(FRAME_INITIALIZERS);
		if(isEnableRegisterFrameInitializers()) {
			@SuppressWarnings("rawtypes")			
			final Set<Class<FrameInitializer>> initializerClasses = TypeUtils.getSubTypesOf(application, getFrameInitializersPackageName(), FrameInitializer.class);
			for ( @SuppressWarnings("rawtypes")
			final Class<FrameInitializer> javaClass : initializerClasses ) {
				if ( FrameInitializer.class.isAssignableFrom(javaClass) ) {
					FramesLogger.info("register FrameInitializer: %s", javaClass.getName());
					final FrameInitializer initializer;
					try {
						@SuppressWarnings("unchecked")
						final FrameInitializer winitializer = (FrameInitializer) javaClass.newInstance();
						initializer = winitializer;
						FRAME_INITIALIZERS.add(initializer);
					} catch ( Exception e ) {
						FramesLogger.error(e, e.getMessage());
					}
				}
			}
		}	
	}
	
	protected void registerTypeResolvers() {
		TYPE_RESOLVERS.clear();
		onRegisterTypeResolvers(TYPE_RESOLVERS);
		if(isEnableRegisterTypeResolvers()) {
			@SuppressWarnings("rawtypes")			
			final Set<Class<TypeResolver>> resolverClasses = TypeUtils.getSubTypesOf(application, getTypeResolversPackageName(), TypeResolver.class);
			for ( @SuppressWarnings("rawtypes")
			final Class<TypeResolver> javaClass : resolverClasses ) {
				if ( TypeResolver.class.isAssignableFrom(javaClass) ) {
					FramesLogger.info("register TypeResolver: %s", javaClass.getName());
					final TypeResolver resolver;
					try {
						@SuppressWarnings("unchecked")
						final TypeResolver wresolver = (TypeResolver) javaClass.newInstance();
						resolver = wresolver;
						TYPE_RESOLVERS.add(resolver);
					} catch ( Exception e ) {
						FramesLogger.error(e, e.getMessage());
					}
				}
			}
		}	
	}	

	public static String getAnnotationHandlersPackageName() {
		return Play.application().configuration().getString("frames.register.annotation_handlers.package_anme", "annotation_handlers");
	}
	public static String getFrameInitializersPackageName() {
		return Play.application().configuration().getString("frames.register.frame_initializers.package_name", "frame_initializers");
	}	
	public static String getTypeResolversPackageName() {
		return Play.application().configuration().getString("frames.register.type_resolvers.package_name", "type_resolvers");
	}
	
	public static boolean isEnableRegisterAnnotationHandlers() {
		return Play.application().configuration().getBoolean("frames.register.annotation_handlers.enable", false);
	}
	public static boolean isEnableRegisterFrameInitializers() {
		return Play.application().configuration().getBoolean("frames.register.frame_initializers.enable", false);
	}	
	public static boolean isEnableRegisterTypeResolvers() {
		return Play.application().configuration().getBoolean("frames.register.type_resolvers.enable", false);
	}	
	
}
