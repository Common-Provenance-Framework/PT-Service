# Provenance traversal service

This service traverses a provenance chain in a specified direction. It fetches the results of a query for each bundle in the chain from that bundle's provenance controller (provenance access service instance).

## Running with Docker

Build the Docker image from the project root directory:

```sh
docker build -t traversal-service .
```

Run the container:

```sh
docker run -p 8083:8080 --env-file .env traversal-service
```

By default, the service listens on port `8080`. You can change the default port using the `PT_SERVICE_PORT` environment variable (see `.env`).

> Note: This service is normally deployed alongside one or more provenance access service instances ([PA-Service](https://github.com/Common-Provenance-Framework/PA-Service)) as part of the full demo setup described in the original project's README.

### Environment variables

| Variable          | Description                 | Default |
| ----------------- | ---------------------------- | ------- |
| `PT_SERVICE_PORT` | Port the service listens on. | `8080`  |

## Provenance service table

The traverser needs the URI of a bundle's provenance access service ([PA-Service](https://github.com/Common-Provenance-Framework/PA-Service)) to fetch that bundle's data. It can obtain the URI from either the referencing connector's `cpm:provenanceServiceUri` attribute or the provenance service table. The table is especially important for the initial bundle because the traverser does not have a connector that references it.

The demo table is loaded from `src/main/resources/provServiceTable.json` at startup. It is a JSON object whose keys are bundle URI prefixes and whose values are the corresponding prov-access service URIs. For example:

```json
{
	"http://localhost:8080/api/v1/organizations/example/": "http://localhost:8082/api/"
}
```

When looking up a bundle, the table uses the first key (in JSON insertion order) that is a prefix of the bundle URI. Add or update an entry to map bundles to the service that hosts them. Keep prefixes specific enough to avoid ambiguous matches. If no key matches, the table has no URI for that bundle and the connector value can be used instead.

When both the table and a connector provide a URI, configure the preferred source in `src/main/resources/application.properties`:

```properties
traverser.preferProvServiceFromConnectors=false
```

Set this to `false` (the default) to prefer the table, or `true` to prefer the connector. If the preferred source has no URI, the traverser falls back to the other source. The initial bundle's URI is looked up in the table. Changes to either resource file take effect after restarting the service; when running with Docker, rebuild the image to include the updated files.

## API documentation (Swagger)

Once the service is running, the Swagger UI is available at:

```
http://localhost:8083/swagger-ui/index.html#
```

> Note that the service container must be running for the Swagger UI to load.

The Swagger UI provides several executable query examples for testing the service. It also lists the available validity checks and traversal priorities.
