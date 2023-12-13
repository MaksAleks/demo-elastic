FROM python:3.8-slim

# Set working directory
WORKDIR /app

# Copy requirements file
COPY requirements.txt requirements.txt

# Install dependencies
RUN pip install -r requirements.txt

# Download SpaCy model
RUN python -m spacy download ru_core_news_sm

# Copy project files
COPY .. .

# Expose port
EXPOSE 8765

# Command to run the application
CMD ["flask", "run", "--host=0.0.0.0", "--port=8765"]