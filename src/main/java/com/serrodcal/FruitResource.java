package com.serrodcal;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkus.vertx.web.Body;
import io.quarkus.vertx.web.Param;
import io.quarkus.vertx.web.Route;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FruitResource {

    private static final Logger log = Logger.getLogger(FruitResource.class);


    @Inject
    FruitDao dao;

    @Route(path = "/fruit", methods = HttpMethod.GET, produces = "application/json")
    public void findAll(RoutingContext rc) {
        log.info("FruitResource.findAll()");
        dao.findAll().subscribe().with(result -> {
            rc.response().setStatusCode(HttpResponseStatus.CREATED.code()).end(Json.encode(result));
        }, failure -> {
            log.error(failure.getMessage());
            rc.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).putHeader("Content-Type","text/plain").end(failure.getMessage());
        });
    }

    @Route(path = "/fruit/:id", methods = HttpMethod.GET, produces = "application/json")
    public void findById(RoutingContext rc, @Param("id") Long id) {
        log.info("FruitResource.findById(" + id.toString() + ")");
        dao.findById(id).subscribe().with(result -> {
            rc.response().setStatusCode(HttpResponseStatus.CREATED.code()).end(Json.encode(result));
        }, failure -> {
            log.error(failure.getMessage());
            rc.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).putHeader("Content-Type","text/plain").end(failure.getMessage());
        });
    }

    @Route(path = "/fruit", methods = HttpMethod.POST, consumes = "application/json", produces = "application/json")
    public void createFruit(RoutingContext rc, @Body FruitPayload fruit) {
        log.info("FruitResource.createFruit(" + fruit.name + ")");
        dao.create(fruit).subscribe().with(id -> {
            rc.response().setStatusCode(HttpResponseStatus.CREATED.code()).end(Json.encode(new Fruit(id, fruit.name)));
        }, failure -> {
            log.error(failure.getMessage());
            rc.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).putHeader("Content-Type","text/plain").end(failure.getMessage());
        });
    }

}