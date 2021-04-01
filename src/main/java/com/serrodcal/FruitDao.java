package com.serrodcal;

import io.quarkus.runtime.Startup;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Startup
@Singleton
public class FruitDao {

    @Inject
    @ConfigProperty(name = "fruit.schema.create", defaultValue = "true")
    boolean schemaCreate;

    @Inject
    io.vertx.mutiny.pgclient.PgPool client;

    @PostConstruct
    void config() {
        if (schemaCreate) {
            initdb();
        }
    }

    private void initdb() {
        client.query("DROP TABLE IF EXISTS fruits").execute()
            .flatMap(r -> client.query("CREATE TABLE fruits (id SERIAL PRIMARY KEY, name TEXT NOT NULL)").execute())
            .flatMap(r -> client.query("INSERT INTO fruits (name) VALUES ('Orange')").execute())
            .flatMap(r -> client.query("INSERT INTO fruits (name) VALUES ('Pear')").execute())
            .flatMap(r -> client.query("INSERT INTO fruits (name) VALUES ('Apple')").execute())
            .await().indefinitely();
    }

    public Uni<List<Fruit>> findAll() {
        return client.query("SELECT id, name FROM fruits ORDER BY name ASC").execute()
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(FruitDao::from)
                .collect().asList();
    }

    public Uni<Fruit> findById(Long id) {
        return client.preparedQuery("SELECT id, name FROM fruits WHERE id = $1")
                .execute(Tuple.of(id))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? from(iterator.next()) : null);
    }

    public Uni<Long> create(FruitPayload fruit) {
        return client.preparedQuery("INSERT INTO fruits (name) VALUES ($1) RETURNING id")
                .execute(Tuple.of(fruit.name))
                .onItem().transform(pgRowSet -> pgRowSet.iterator().next().getLong("id"));
    }

    private static Fruit from(Row row) {
        return new Fruit(row.getLong("id"), row.getString("name"));
    }

}
