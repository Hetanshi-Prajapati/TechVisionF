from flask import Flask, request, jsonify
import pickle
import base64
import io
from PIL import Image

# 🔥 NEW (for image model)
from tensorflow.keras.models import load_model
import numpy as np

app = Flask(__name__)

print("🔥 Flask starting...")
print("Loading model...")
image_model = load_model("D:/update(27)/update(27)/AI Model/image_model.keras")
print("Model loaded successfully ✅")

# 🔹 Text model (UNCHANGED)
model = pickle.load(open("model.pkl", "rb"))
vectorizer = pickle.load(open("vectorizer.pkl", "rb"))

@app.route("/")
def health():
    return "OK"

@app.route("/home")
def home():
    return "AI API is running successfully 🚀"


# 🔹 TEXT LOGIC (UNCHANGED)
def is_spam(text):
    words = text.split()
    return len(set(words)) == 1


def is_code(text):
    keywords = ["def", "public", "SELECT", "console.log", "{", "}", ";", "<div>"]
    return any(k in text for k in keywords)


# 🔥 NEW IMAGE PREDICTION (NO OCR)
def predict_image_from_base64(image_base64):
    try:
        image_bytes = base64.b64decode(image_base64)
        # 🔥 RULE FIRST (IMPORTANT)
        logo_flag = is_logo_like(image_bytes)

        if logo_flag:
            print("Logo-like detected → but checking with model...")   # LOWER confidence
        img = Image.open(io.BytesIO(image_bytes)).convert("RGB")
        img = img.resize((224, 224))

        img_array = np.array(img) / 255.0
        img_array = np.expand_dims(img_array, axis=0)

        pred = image_model.predict(img_array)[0]

        non_tech = pred[0]
        tech = pred[1]

        confidence = float(max(tech, non_tech))

        # FINAL DECISION (SMART)
        if logo_flag and tech < 0.75:
            return "Rejected", confidence

        if tech > non_tech:
            return "Allowed", confidence
        else:
            return "Rejected", confidence

    except Exception as e:
        print("Image error:", e)
        return "Rejected", 0.5   # not 1.0


@app.route("/predict", methods=["POST"])
def predict():
    data = request.json
    text = data.get("text", "")
    image = data.get("image", None)

    # 🔥 IMAGE LOGIC (UPDATED)
    image_result = None
    image_confidence = 0

    if image:
        image_result, image_confidence = predict_image_from_base64(image)

    # 🔹 TEXT LOGIC (UNCHANGED)
    text_result = None
    text_confidence = 0

    if text:
        if not text.strip():
            text_result = "Rejected"
            text_confidence = 1.0

        elif len(text.split()) < 5:
            text_result = "Rejected"
            text_confidence = 1.0

        elif is_spam(text):
            text_result = "Rejected"
            text_confidence = 1.0

        elif is_code(text):
            text_result = "Allowed"
            text_confidence = 1.0

        else:
            x = vectorizer.transform([text])
            proba = model.predict_proba(x)[0]
            text_confidence = float(max(proba))
            result = model.predict(x)[0]

            text_result = "Allowed" if result == "technical" else "Rejected"

    # 🔥 FINAL DECISION

    # CASE 1: BOTH TEXT + IMAGE
    if text and image:
        if text_result == "Allowed" and image_result == "Allowed":
            return jsonify({
                "result": "Allowed",
                "confidence": min(text_confidence, image_confidence)
            })
        else:
            return jsonify({
                "result": "Rejected",
                "confidence": max(text_confidence, image_confidence)
            })

    # CASE 2: ONLY IMAGE
    if image and not text:
        return jsonify({
            "result": image_result,
            "confidence": image_confidence
        })

    # CASE 3: ONLY TEXT
    if text and not image:
        return jsonify({
            "result": text_result,
            "confidence": text_confidence
        })

    # CASE 4: NOTHING
    return jsonify({
        "result": "Rejected",
        "confidence": 1.0
    })

import cv2

def is_logo_like(image_bytes):
    try:
        nparr = np.frombuffer(image_bytes, np.uint8)
        img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

        gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)

        # Edge detection
        edges = cv2.Canny(gray, 100, 200)

        # Count edges
        edge_density = np.sum(edges > 0) / (img.shape[0] * img.shape[1])

        # 🔥 logos usually have LOW edge complexity
        # stricter condition for logos
        if edge_density < 0.01 and img.shape[0] < 200:
            return True

        # Reject only if image is also small (logos usually small/simple)
        if edge_density < 0.02 and img.shape[0] < 300:
            return True
        return False

    except:
        return False

if __name__ == "__main__":
    app.run(port=5000)