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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.tinkerpop.blueprints.Element;
import com.tinkerpop.frames.ClassUtilities;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.modules.MethodHandler;
import com.wingnest.play2.frames.annotations.DateProperty;

public class DatePropertyMethodHandler implements MethodHandler<DateProperty> {

	@Override
	public Object processElement(final Object frame, final Method method, final Object[] arguments, final DateProperty annotation, final FramedGraph<?> framedGraph, final Element element) {
		return process(annotation, method, arguments, element);
	}
	
	@Override
	public Class<DateProperty> getAnnotationType() {
		return DateProperty.class;
	}
	
	private <T extends Element> Object process(final DateProperty annotation, final Method method, final Object[] arguments, final T element) {
		if ( ClassUtilities.isGetMethod(method) ) {
			return toDate(annotation, element.getProperty(annotation.value()));
		} else if ( ClassUtilities.isSetMethod(method) ) {
			Object value = arguments[0];
			if ( null == value ) {
				element.removeProperty(annotation.value());
			} else {
				if(annotation.storeType() == DateProperty.StoreType.STRING ) 
					element.setProperty(annotation.value(), toFormattedString(annotation, value));
				else
					element.setProperty(annotation.value(), ((Date)value).getTime() );
			}
			return null;
		} else if ( ClassUtilities.isRemoveMethod(method) ) {
			element.removeProperty(annotation.value());
			return null;
		}
		return null;
	}

	private String toFormattedString(final DateProperty annotation, final Object value) {
		if ( value == null )
			return null;
		if ( value instanceof Date ) {
			final SimpleDateFormat sdf = new SimpleDateFormat(annotation.stringFormat());
			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			return sdf.format(value);
		} else {
			throw new IllegalStateException("value's type must be Date : key = " + annotation.value() + ", class = " + value.getClass().getName());
		}
	}

	private Date toDate(final DateProperty annotation,  final Object property) {
		if ( property == null )
			return null;
		if ( property instanceof String ) {
			try {
				final SimpleDateFormat sdf = new SimpleDateFormat(annotation.stringFormat());
				return sdf.parse((String) property);
			} catch ( Exception e ) {
				throw new RuntimeException(e);
			}
		} else if ( property instanceof Long ) {
			return new Date((Long)property);
		} else if ( property instanceof Date ) {
			// memo: cacheされるのでこのケースは必要。
			return (Date) property;
		} else {
			throw new IllegalStateException("property's type must be String : key = " + annotation.value() + ", class = " + property.getClass().getName());
		}
	}




}
