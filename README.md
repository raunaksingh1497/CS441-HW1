# killmeLLM
**Building a Large Language Model (LLM) using Distributed Systems**

## Assignment 1 Project Documentation: 
**CS441 Engineering Distributed Objects for Cloud Computing**

### Input Data
For this project, I utilized IMDB reviews, which consist of both positive and negative feedback. The dataset contains **10,000 reviews**. The input data was split into five text shards, though tests were conducted with 3, 7, and 10 shards. The configuration using **5 shards** provided the optimal performance.

### Commands to Run the Project

To compile and run the project using SBT, use the following commands:

```bash
sbt clean compile test
sbt clean compile run
```

Here’s a refined README.md based on the information provided:

# killmeLLM
**Building a Large Language Model (LLM) using Distributed Systems**

## Assignment 1 Project Documentation: 
**CS441 Engineering Distributed Objects for Cloud Computing**

### Input Data
For this project, I utilized IMDB reviews, which consist of both positive and negative feedback. The dataset contains **10,000 reviews**. The input data was split into five text shards, though tests were conducted with 3, 7, and 10 shards. The configuration using **5 shards** provided the optimal performance.

### Commands to Run the Project

To compile and run the project using SBT, use the following commands:

```bash
sbt clean compile test
sbt clean compile run

MapReduce Pipeline

The project implements three MapReduce jobs to process the data, tokenize it, generate word embeddings, and calculate cosine similarity.

1. Tokenization

	•	Description: This step implements byte pair encoding (BPE) to tokenize the input reviews.
	•	Input:
	•	Key: LongWritable (the byte offset of the line in the text file).
	•	Value: Text (the actual review content).
	•	Output:
	•	Key: Text (a tokenized word).
	•	Value: Text (the count “1” or any other associated value for the token).
	•	Purpose: Prepares the text for downstream tasks by converting it into tokenized words.

2. Word2Vec Embedding Generation

	•	Description: This task uses the Word2Vec model to generate vector embeddings for each tokenized sentence.
	•	Input: A list of tokenized sentences.
	•	Output: Vector embeddings for each word, with each word’s vector having a size of 10 dimensions.
	•	Purpose: Converts words into numerical vectors for similarity measures and further calculations.

3. Cosine Similarity Calculation

	•	Description: The final MapReduce task calculates the cosine similarity between vector embeddings generated by Word2Vec. Based on the calculated cosine similarity, it classifies the word pairs into one of the following categories:
	•	A: 0.00 - 0.25
	•	B: 0.25 - 0.50
	•	C: 0.50 - 0.75
	•	D: 0.75 - 1.00
	•	Purpose: This step groups words based on their semantic similarity using the cosine of their vector angle.

Performance Insights

We experimented with shard counts of 3, 5, 7, and 10 to optimize performance. After testing, we found that 5 shards provided the most balanced results, offering optimal efficiency and resource utilization.

Output

	•	output.csv: Contains the frequency of tokens and words.
	•	tokenization_output/: Contains the tokenized output.
	•	word2vec_output/: Contains the Word2Vec embeddings.
	•	cosine_similarity_output/: Contains the cosine similarity results.

Repository & Resources

	•	Repository Link: GitHub Repository
	•	YouTube Walkthrough: YouTube Video

This pipeline processes large textual datasets, tokenizes the data, generates meaningful vector representations, and classifies the word pairs based on their similarity, making it an efficient framework for distributed text processing.
