package com.wingnest.play2.frames.plugin.annotation_handler;

import java.lang.reflect.Method;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.EdgeFrame;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.VertexFrame;
import com.tinkerpop.frames.annotations.AnnotationHandler;
import com.wingnest.play2.frames.annotations.Id;

public class IdAnnotationHandler implements AnnotationHandler<Id> {

	@Override
	public Class<Id> getAnnotationType() {
		return Id.class;
	}

	@Override
	public Object processVertex(final Id annotation, final Method method, final Object[] arguments, final FramedGraph framedGraph, final Vertex element) {
		final VertexFrame frame = (VertexFrame) framedGraph.frame(element, VertexFrame.class);
		return frame.asVertex().getId();
	}

	@Override
	public Object processEdge(final Id annotation, final Method method, final Object[] arguments, final FramedGraph framedGraph, final Edge element, final Direction direction) {
		final EdgeFrame frame = (EdgeFrame) framedGraph.frame(element, direction, EdgeFrame.class);
		return frame.asEdge().getId();
	}
	
}
