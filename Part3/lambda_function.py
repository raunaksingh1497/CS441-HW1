import json
import boto3

# Initialize Bedrock client
bedrock_client = boto3.client('bedrock-runtime')

def lambda_handler(event, context):
    try:
        # Log the incoming event to debug its structure
        print(f"Received event: {json.dumps(event)}")

        # If the event is the direct JSON object (without 'body')
        request_data = event  # Use the event directly since it contains the query

        # Extract the query from the request
        query = request_data.get('query', '')

        # Call Bedrock to process the query directly (no need for chat history)
        bedrock_response = call_bedrock(query)

        # Create the response structure
        response = {
            "query_result": bedrock_response,
            "statusCode": 200,
            "errorMessage": ""
        }

        # Return the JSON response
        return {
            'statusCode': 200,
            'headers': {'Content-Type': 'application/json'},
            'body': json.dumps(response)
        }

    except Exception as e:
        # Handle errors and return a 500 status code with the error message
        return {
            'statusCode': 500,
            'headers': {'Content-Type': 'application/json'},
            'body': json.dumps({
                "query_result": "",
                "statusCode": 500,
                "errorMessage": f"Error processing request: {str(e)}"
            })
        }

def call_bedrock(user_query):
    """
    Calls AWS Bedrock with the user query and returns the raw response body.
    """
    try:
        # Construct the payload for Bedrock (only sending the query)
        payload = {
            "message": user_query  # Just send the user query as the message
        }

        # Specify the Bedrock model ARN or endpoint
        model_id = "cohere.command-r-plus-v1:0"  # Replace with your model's identifier

        # Make the Bedrock call
        response = bedrock_client.invoke_model(
            modelId=model_id,
            contentType="application/json",
            accept="*/*",
            body=json.dumps(payload)
        )

        # Read the StreamingBody and return it directly
        response_body = response['body'].read().decode('utf-8')

        # Return the raw response body
        return response_body
    except Exception as e:
        raise Exception(f"Error calling Bedrock: {str(e)}")