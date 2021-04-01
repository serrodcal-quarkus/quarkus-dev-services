Get all fruits:
> curl http://localhost:8080/fruit -w '\n'

Find a fruit by id:
> curl http://localhost:8080/fruit/1 -w '\n'

Create a new fruit:
> curl -X POST localhost:8080/fruit -H 'Content-Type: application/json' -d '{"name":"Lemon"}' -w '\n'