# Sendur

Sendur is an AI-augmented lead generation automation platform that leverages N8N workflows and an AI agent to streamline the process of identifying, reviewing, and engaging potential business leads.

## How It Works

### Automated Lead Discovery
Sendur uses AI and N8N to search the web for businesses based on a given description or industry profile.

### Lead & Email Generation
Once discovered, leads are enriched with relevant details and paired with AI-generated email drafts.

### Human-in-the-Loop Review
You log into the Sendur web application to review and approve both the leads and their associated emails.

### Email Dispatch & Webhook Integration
Approved emails are sent via an N8N webhook trigger, initiating the outbound communication process.

### Lead Persistence & Workflow Expansion
All lead data is stored and updated in the system. As the project grows, additional automations and workflows (e.g., follow-ups, CRM sync, analytics) can be layered on top.

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

