from tensorflow.keras.models import load_model
from tensorflow.keras.preprocessing import image
import numpy as np

model = load_model("image_model.keras")

def predict(img_path):
    img = image.load_img(img_path, target_size=(224,224))
    img_array = image.img_to_array(img)/255.0
    img_array = np.expand_dims(img_array, axis=0)

    pred = model.predict(img_array)[0]

    non_tech = pred[0]
    tech = pred[1]

    print("Non-Technical Confidence:", non_tech)
    print("Technical Confidence:", tech)

    if tech > non_tech:
        print("✅ TECHNICAL")
    else:
        print("❌ NON-TECHNICAL")

predict("test.jpg")