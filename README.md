## Simple movie db

Run application with:

```
mvn spring-boot:run
```

Run tests:
```
mvn test
```

Register user:
```
curl -s -H "Content-Type: application/json" -X POST -d '{"email":"email@company.com", "password":"0123456789"}' http://localhost:8080/user
```

Login:
```
curl -s -H "Content-Type: application/json" -X POST -d '{"email":"email@company.com", "password":"0123456789"}' http://localhost:8080/login
```

Remove user:
```
curl -s -H "Content-Type: application/json" -H "Authorization: ${TOKEN}" -X DELETE http://localhost:8080/user
```

Add movie:
```
curl -s -H "Content-Type: application/json" -H "Authorization: ${TOKEN}" -X POST -d '{"title": "title", "description": "description", "watched": false}' http://localhost:8080/movies
```

List all movies:
```
curl -s -H "Content-Type: application/json" -H "Authorization: ${TOKEN}" -X GET http://localhost:8080/movies
```

List watched movies:
```
curl -s -H "Content-Type: application/json" -H "Authorization: ${TOKEN}" -X GET -d '{"watched": true}' http://localhost:8080/movies
```

List unwatched movies:
```
curl -s -H "Content-Type: application/json" -H "Authorization: ${TOKEN}" -X GET -d '{"watched": true}' http://localhost:8080/movies
```

Edit movie:
```
curl -s -H "Content-Type: application/json" -H "Authorization: ${TOKEN}" -X PUT -d '{"title": "title", "description": "description", "watched": true}' http://localhost:8080/movies/402881f95de6977a015de697918a0001
```

Delete movie:
```
curl -s -H "Content-Type: application/json" -H "Authorization: ${TOKEN}" -X DELETE http://localhost:8080/movies/402881f95de6977a015de697918a0001
```
