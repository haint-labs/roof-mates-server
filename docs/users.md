# ROOF MATES API

### Registration

- Create guest user

```bash
curl \
    -X POST \
    -H "Content-Type: application/json" \
    -d '{"type":"guest", "name": "Johny", "surname": "Boy", "phoneNumber": "+37121231233", "device": {"type": "iOS", "token": "asdfnkdn5645ASGgw"}}' \
    http://0.0.0.0:8080/users/register
```

- Create owner user (has parking to share)

```bash
curl \
    -X POST \
    -H "Content-Type: application/json" \
    -d '{"type":"owner", "name": "Sara", "surname": "Connor", "phoneNumber": "+3714765757", "device": {"type": "Android", "token": "asdfnkdn56sdfgssdfg"}, "parkingNumber": 45}' \
    http://0.0.0.0:8080/users/register
```

- List registered users

```bash
curl http://0.0.0.0:8080/users
```