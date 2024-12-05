# CS441 Distributed LLM
**Building a Large Language Model (LLM) using Distributed Systems**
**CS441 Engineering Distributed Objects for Cloud Computing**

## Part 3: Integration with Amazon Bedrock for LLM Processing:

## Project Overview
This part of the project integrates the previously developed Large Language Model (LLM) with cloud services. The goal is to use Amazon Bedrock, a managed AI service from AWS, to enhance the functionality of the LLM. The LLM processes queries from the Ollama client, performs necessary computations, and then uses Amazon Bedrock for advanced language tasks such as text generation and summarization.

### Video
https://drive.google.com/file/d/10sTK0YP0KyIyjX9gldB25qg7vPook8d0/view?usp=sharing 

To compile and run the project using SBT, use the following commands:

### Commands to Run the Project

To compile and run the project using SBT, use the following commands:

```bash
sbt clean compile test
sbt clean compile run
```


## Architecture Flow

The following is the high-level architecture of the system:
###	1.	Ollama Client:
        •	Initiates the request by sending a query to the LLM Server for processing.
        •	The Ollama Client could be a front-end web or mobile application or any system interacting with the backend.
###	2.	LLM Server:
        •	The LLM Server processes the incoming requests from the Ollama Client.
        •	It serializes the request data and logs structured data for tracking purposes.
        •	The LLM Server forwards the request to the API Gateway.
###	3.	API Gateway:
        •	The API Gateway exposes HTTP endpoints for secure and scalable handling of incoming requests.
        •	It routes the request to the MainService hosted on EC2 for further processing.
###	4.	MainService (EC2):
        •	The MainService performs additional operations like request formatting, logging, validation, and managing workflows.
        •	It interacts with AWS services using AWS credentials, prepares a payload, and invokes the Lambda Function.
###	5.	Lambda Function:
        •	The Lambda Function processes the request further and invokes Amazon Bedrock to perform advanced language processing tasks.
        •	It could include custom logic for data transformation, analysis, or forwarding the request to Bedrock.
###	6.	Amazon Bedrock:
        •	Amazon Bedrock processes the request using one of its foundational AI models (such as text generation, summarization, etc.).
        •	It sends the processed response back to the Lambda Function.
