# ROOF MATES API

### Spot shares

- Create shared spot

```bash
curl \
    -X POST \
    -H "Content-Type: application/json" \
    -d '{"spot_id":1, "user_id": 2, "from_timestamp": 1234, "to_timestamp": 1300}' \
    http://0.0.0.0:8080/spots/share
```
