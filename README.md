# killmeLLM
**Building a Large Language Model (LLM) using Distributed Systems**

## Assignment 1 Project Documentation: 
**CS441 Engineering Distributed Objects for Cloud Computing**

## Project Overview
This project leverages Apache Spark to create and train a large language model (LLM) on distributed cloud infrastructure. Utilizing Word2Vec embeddings from prior assignments, we aim to predict the next word embedding in a sequence through a neural network trained with sliding window sequences.

###Project Structure

Assignment 1 Recap

For the initial phase, we processed a dataset of 10,000 IMDB reviews containing both positive and negative feedback. The data was split into 5 text shards, though tests were conducted with shard counts of 3, 7, and 10 to determine the optimal configuration. Using 5 shards yielded the best performance balance between efficiency and resource utilization.
Here’s a refined README.md based on the information provided:

### Input Data
The program takes as input Word2Vec embeddings (dimension: E = 100) generated in Assignment 1. These embeddings form the foundation for sliding window sequences used in model training.

### Commands to Run the Project

To compile and run the project using SBT, use the following commands:

```bash
sbt clean compile test
sbt clean compile run

1. Sliding Window Processing

In this phase, the project utilizes Spark’s parallel processing to create sliding windows from the vector embeddings. Given a sequence length of 15 (L = 15) and approximately 30,000 embeddings, this generates a tensor of dimensions (2950, 15, 100) as input for the neural network.

2. Neural Network Training

The neural network is trained on the sliding window tensor, with computation distributed across the Spark cluster. Training produces a model (trained_model.zip), stored in the project’s root directory. The trained model is subsequently used to predict the next word embedding in a sequence, simulating LLM functionality.

Repository & Resources

	•	Repository Link: GitHub Repository
	•	Video: https://uic.zoom.us/rec/share/_4tPaRlRbNPCvwubO7HD-bpegQj0jpE8R6sYgf439THpS2US68bPNL3RlwnnVe0h.IsTMlJyo5yxueYKV?startTime=1730708820000 

This pipeline processes large textual datasets, tokenizes the data, generates meaningful vector representations, and classifies the word pairs based on their similarity, making it an efficient framework for distributed text processing.
