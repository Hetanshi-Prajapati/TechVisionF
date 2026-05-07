import requests
import base64

# convert image to base64
with open("test.jpeg", "rb") as img:
    encoded = base64.b64encode(img.read()).decode()

url = "http://localhost:5000/predict"

data = {
    "text": "",
    "image": encoded
}

response = requests.post(url, json=data)

print(response.json())