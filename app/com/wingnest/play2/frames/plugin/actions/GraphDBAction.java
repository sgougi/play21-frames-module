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
package com.wingnest.play2.frames.plugin.actions;

import java.lang.annotation.Annotation;

import play.libs.F.Promise;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.SimpleResult;

import com.wingnest.play2.frames.GraphDB;

final public class GraphDBAction extends Action<Annotation> {

	@Override
	public Promise<SimpleResult> call(final Http.Context context) throws Throwable {

		final Promise<SimpleResult> res;
		try {
			beforeInvocation();
			res = delegate.call(context);
			onInvocationSuccess();
		} catch ( Exception e ) {
			onInvocationException(e);
			throw e;
		} finally {
			invocationFinally();
		}
		return res;
	}

	private void beforeInvocation() {
		/* nop */
	}

	private void invocationFinally() {
		/* nop */
	}

	private void onInvocationSuccess() {
		GraphDB.getGraphManager().commit();
	}

	private void onInvocationException(Exception e) {
		GraphDB.getGraphManager().rollback();
	}

}
