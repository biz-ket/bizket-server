FROM python:3.9-slim

WORKDIR /app

COPY pytrends_service/requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY pytrends_service .

EXPOSE 5000

ENTRYPOINT ["gunicorn", "app:app", "-b", "0.0.0.0:5000", "--workers", "2"]
