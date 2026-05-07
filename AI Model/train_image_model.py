import tensorflow as tf
from tensorflow.keras.preprocessing.image import ImageDataGenerator
from tensorflow.keras import layers, models

DATASET_PATH = "image_dataset"

IMG_SIZE = 224
BATCH_SIZE = 32

# 🔥 Custom function to treat subfolders as same class
def get_data_generators():

    datagen = ImageDataGenerator(
    rescale=1./255,
    validation_split=0.2,
    rotation_range=20,
    zoom_range=0.2,
    horizontal_flip=True
    )

    train = datagen.flow_from_directory(
        DATASET_PATH,
        target_size=(IMG_SIZE, IMG_SIZE),
        batch_size=BATCH_SIZE,
        class_mode='categorical',   # 🔥 important
        subset='training'
    )

    val = datagen.flow_from_directory(
        DATASET_PATH,
        target_size=(IMG_SIZE, IMG_SIZE),
        batch_size=BATCH_SIZE,
        class_mode='categorical',
        subset='validation'
    )

    return train, val


train_data, val_data = get_data_generators()

print("Classes found:", train_data.class_indices)

# 🔥 Model
base_model = tf.keras.applications.MobileNetV2(
    input_shape=(224,224,3),
    include_top=False,
    weights='imagenet'
)

base_model.trainable = False

model = models.Sequential([
    base_model,
    layers.GlobalAveragePooling2D(),
    layers.Dense(128, activation='relu'),
    layers.Dense(len(train_data.class_indices), activation='softmax')  # 🔥 dynamic classes
])

model.compile(
    optimizer='adam',
    loss='categorical_crossentropy',
    metrics=['accuracy']
)

print("🚀 Training model...")

model.fit(
    train_data,
    validation_data=val_data,
    epochs=5
)

model.save("image_model.keras")

print("✅ Model trained and saved!")
print(train_data.class_indices)