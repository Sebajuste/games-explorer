package io.edge.games.explorer.util;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.ResolverCache;
import io.swagger.v3.parser.core.models.AuthorizationValue;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.contract.RouterFactoryException;
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;
import io.vertx.ext.web.api.contract.openapi3.impl.OpenAPI3RouterFactoryImpl;
import io.vertx.ext.web.api.contract.openapi3.impl.OpenApi3Utils;

public class OpenAPI3RouterBufferFactory {

	public static void create(Vertx vertx, Buffer buffer, Handler<AsyncResult<OpenAPI3RouterFactory>> handler) {
		create(vertx, buffer, Collections.emptyList(), null, handler);
	}

	public static void create(Vertx vertx, Buffer buffer, Handler<RoutingContext> globalHandler, Handler<AsyncResult<OpenAPI3RouterFactory>> handler) {
		create(vertx, buffer, Collections.emptyList(), globalHandler, handler);
	}

	public static void create(Vertx vertx, Buffer buffer, List<JsonObject> auth, Handler<RoutingContext> globalHandler, Handler<AsyncResult<OpenAPI3RouterFactory>> handler) {

		List<AuthorizationValue> authorizationValues = auth.stream().map(obj -> obj.mapTo(AuthorizationValue.class)).collect(Collectors.toList());
		
		vertx.executeBlocking((Promise<OpenAPI3RouterFactory> promise) -> {
			SwaggerParseResult swaggerParseResult = new OpenAPIV3Parser().readContents(buffer.toString(Charset.defaultCharset()), authorizationValues, OpenApi3Utils.getParseOptions());
			if (swaggerParseResult.getMessages().isEmpty()) {

				OpenAPI3RouterFactory openAPI3RouterFactory = new OpenAPI3RouterFactoryImpl(vertx, swaggerParseResult.getOpenAPI(), new ResolverCache(swaggerParseResult.getOpenAPI(), null, null));

				if (globalHandler != null) {
					openAPI3RouterFactory.addGlobalHandler(globalHandler);
				}

				promise.complete(openAPI3RouterFactory);
			} else {
				if (swaggerParseResult.getMessages().size() == 1 && swaggerParseResult.getMessages().get(0).matches("unable to read location `?\\Q" + "" + "\\E`?"))
					promise.fail(RouterFactoryException.createSpecNotExistsException(""));
				else
					promise.fail(RouterFactoryException.createSpecInvalidException(StringUtils.join(swaggerParseResult.getMessages(), ", ")));
			}
		}, handler);
	}

}
