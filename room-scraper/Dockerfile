FROM python:3.10-slim-buster

WORKDIR /app

RUN apt-get update && apt-get install -y libpq-dev gcc

COPY requirements.txt .

RUN pip install --no-cache-dir -r requirements.txt

COPY . .



CMD ["python", "-u", "main.py"]