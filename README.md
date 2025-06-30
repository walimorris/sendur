<!-- PROJECT LOGO -->
<br />
<div align="center">
  <a href="https://bitbucket.org/intelligence-opensent/opensentop/src/master/">
    <img src="/images/sendur_worm.png" alt="Logo" width="400" height="300">
  </a>

<h3 align="center">Sender</h3>

  <p align="center">
    Automate business lead generation and outreach with Java and GenAI
    <br />
    <a href="#">View Demo</a>
    ·
    <a href="#">Report Bug</a>
    ·
    <a href="#">Request Feature</a>
  </p>
</div>

Sendur is an AI-augmented lead generation automation platform that leverages n8n workflows and AI agents to streamline the process of identifying, reviewing, and engaging potential business leads.

## How It Works

### Automated Lead Discovery
Sendur uses <a href="https://n8n.io/">n8n</a>, an AI Agent, and SerpAPI to search the web, on an automated schedule, for businesses based on a given 
description or industry profile. We've all heard of prompting... yes, you need to build a prompt to share with your AI 
Agent. Be warned, AI Agents can, and do, get things wrong so it's best that you are explicit in your details. In this 
setup, we ask our Agent to look up the business details of small businesses around the areas above North Seattle. 
We want our agent to send these leads back in a nicely built JSON structure (we can use this for persistence). The Agent
uses <a href="https://openai.com/">OpenAI</a> and SerpAPI as a tool. Tools are nice-to-haves that are available for your AI Agent to use, however your
Agent must have access to utilize these tools (API keys or other information you can let your Agent know about). If
you haven't, check out what <a href="https://serpapi.com/">SerpAPI</a> is. You see the node called `Structure the Leads`, well 
n8n allows you to manipulate any data output you receive. In this case our Agent returns some JSON, hopefully a list of 
leads. This Code node allows us to conduct any other parsing, validation or structure manipulation on this output data. 
Honestly, we just want to ensure the AI Agent is giving us some useful data, in the right structure we can use in our 
Springboot application. Which leads us to the last `Receive Scheduled Leads` node. This output is sent via a `POST` request to
Sendur (`/sendur/api/leads/receive-scheduled-leads`). You might be asking how we remove data duplication, I mean we don't 
want the same leads showing up all the time. You could use a `Memory` node on the AI Agent, however this is generally used
for chat history context, and unfortunately we're not building a chatbot. So, we filter. Going back,
you see the first `Get All leads` node? Here's where we send a `GET` request to Sendur (`/sendur/api/leads/find-all`) 
to receive all our leads. Next, in the `Blocked Leads` node, we parse all the business names, and we pass this to our AI Agent.
We tell our agent, "hey, you see this list of business names? If you run up on them while you're searching the web, 
ignore them and don't add them to the final output." I'm sure you can come up with a better prompt...
Lastly, as a final filter we do something similar in the Sendur application code to ensure we're not persisting 
duplicates.

<a href="https://n8n.io/integrations/agent/">
  <img src="images/scheduled_lead_generator_agent_v1.png" alt="Logo" width="800" height="350">
</a>

### Lead & Email Generation
Once discovered, leads are enriched with relevant details and paired with AI-generated email drafts. Maybe you will 
generate some nice cash flow with automation, or maybe you have deep pockets. Either way, be aware: your AI Agent 
uses API keys for tools like OpenAI and SerpAPI and this can become costly! However, maybe you only run these schedules
once a day, or maybe you change this to be manually triggered. Just run some calculations before you set this loose. Many 
tools have free tiers so check those out too, and be creative!

<a href="https://n8n.io/integrations/agent/">
  <img src="images/n8n_ai_agent_prompt.png" alt="Logo" width="800" height="350">
</a>

### Automating Data Record Updates
We discussed how AI Agents can get things wrong and create random false data (<a href="https://cloud.google.com/discover/what-are-ai-hallucinations">hallucinate</a>)
and/or incomplete data! Well, incorrect data is definitely a possibility when we use AI to automate actions. Part of the goal
is to receive correct, complete, and valuable business leads. So, we use n8n nodes like the `Code` node for validation and 
formatting, but what happens when our data is correct but unknown? Our AI Agent will still produce something. What we did
was protect our data output by explicitly telling our agent what to put when it can not find what it searches for: `"email": "Not available"`.
What if our search just wasn't deep enough, but we really want to produce as many business leads as possible? One option is to run 
another, deeper and more intentional, search. I don't know about you, but I don't want to do that work. We can, again, use the services of n8n and our
AI Agent. Here's another workflow, except this time we pull leads without emails from our datastore by calling our springboot API 
`/sendur/api/leads/no-email-scheduler`, we validate our data with the `Code` node, and send to our Agent. But we only want leads whose 
email has been found. Let's use another `Code` node to truncate the JSON returned from our Agent. Lastly, we can send these updated leads
back to our springboot api endpoint `/sendur/api/leads/update-emails` to update our datastore. Next, we'll talk about how we send off the 
generated emails with a click of a button. 

<a href="https://n8n.io/integrations/agent/">
  <img src="images/scheduled_lead_update_agent.png" alt="Logo" width="800" height="350">
</a>

### Human-in-the-Loop Review
You log into the Sendur web application to review and approve both the leads and their associated emails.

<a href="https://mui.com/material-ui/">
  <img src="images/sendur_landing_page.png" alt="Logo" width="800" height="350">
</a>

### Email Dispatch & Webhook Integration
Approved emails are sent via an n8n webhook trigger, initiating the outbound communication process.

<a href="https://n8n.io/integrations/agent/">
  <img src="images/send_approved_emails.png" alt="Logo" width="800" height="350">
</a>

### Lead Persistence & Workflow Expansion
All lead data is stored and updated in the system. As the project grows, additional automations and workflows (e.g., follow-ups, CRM sync, analytics) can be layered on top.

### Deploying n8n on AWS EC2
1. Spin up an EC2 instance (t2.micro is free 🤑)
- Chose Ubuntu 22.04 or Amazon Linux 2
- Ensure to configure security groups to allow traffic on port 5678 (n8n's default port)
- Keep in mind: server(s) running the application will need inbound permission, and you may want to access n8n locally
- Create and download the SSH key pair to access your instance
2. Install nodejs on EC2
- ssh into your instance
- Linux 2: (`curl -sL https://rpm.nodesource.com/setup_24.x | sudo bash -`) then run (`sudo yum install -y nodejs`)
- Ubuntu : (`sudo apt update && sudo apt install -y nodejs npm`)
3. Install and run n8n
- Install: `sudo npm install -g n8n`
- To test your setup (which is currently insecure: no TLS/HTTPS) for development you should disable secure cookies for the meantime
- Later we will add a load balancer to make this publicly accessible for (yourself, your team, your org)
- In Terminal : `export N8N_SECURE_COOKIE=false`
- Run         : `n8n`
4. Accessing n8n
- Browser     : <ec2-server-ip>:5678
- If you're slowly migrating from local to AWS be sure to update your application's configuration settings
- You'll also want to ensure your n8n Request Nodes are updated to point to your application server or local ip
5. Security Tips
- Restrict inbound traffic to n8n server using tight security groups. In dev mode this should be your IP address
- Later, we'll enable AWS WAF (Web Application Firewall) and tight log accounting
- n8n has useful AWS Nodes like the <a href="https://docs.n8n.io/integrations/builtin/app-nodes/n8n-nodes-base.awslambda/">Lambda Node</a> - we need to use IAM roles with least privilege for AWS integrations
- Our EC2 dev instance won't run up a charge (if you used t2.micro 🤑) so shut it down when not in use
- When we first log in we create server credentials. Please don't use admin/admin or admin/password.

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

