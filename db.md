```bash
docker run --name cinehua  \
-p 5432:5432 \
-e POSTGRES_PASSWORD=cinehua123 \
-e POSTGRES_USER=cinehua_user \
-e POSTGRES_DB=cinehua_db \
postgres:16
```