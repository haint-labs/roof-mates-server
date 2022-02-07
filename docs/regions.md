# ROOF MATES API

### Regions

- Create region

```bash
curl \
    -X POST \
    -H "Content-Type: application/json" \
    -d '{"address":"Road street 16"}' \
    http://0.0.0.0:8080/regions
```

- List of available regions with their spots

```bash
curl http://0.0.0.0:8080/regions
```