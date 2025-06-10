# Sendur

### Building and Running the Project

#### 1. Build the Project
Note: Ensure application.properties file is populated!
To build the backend and frontend, run the following commands:
``` bash
mvn clean install
```
```bash
npm run build
```

#### 2. Running the Project
After building, you can run the backend Spring Boot application using:
```bash
mvn spring-boot:run
```

The frontend can be served running:
```bash
npm run watch
```

#### 3. Testing the Project
Once the project is running, open up a browser and navigate to `localhost:8082` and you should see text saying `All is Good!`.

