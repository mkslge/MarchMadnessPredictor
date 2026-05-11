# March Madness Frontend

React + Vite UI for generating game simulations and viewing recorded yearly statistics.

## Run

From `Frontend/`:

```bash
npm install
npm run dev
```

The Vite dev server proxies API calls to `http://localhost:8081`. Start the backend with:

```bash
../mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

## Build

```bash
npm run build
```
