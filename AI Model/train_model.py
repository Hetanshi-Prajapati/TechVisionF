import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score, precision_score, recall_score, classification_report
import pickle

print("Loading dataset...")

# Load dataset
df = pd.read_csv("vtech_final_dataset_2500.csv")

# Clean dataset
df['content'] = df['content'].str.lower()
df = df.drop_duplicates(subset=['content'])

print("Dataset loaded and cleaned:", df.shape)
print("Label counts:\n", df['label'].value_counts())

# Split data
X_train_raw, X_test_raw, y_train, y_test = train_test_split(
    df["content"], df["label"], test_size=0.2, random_state=42
)

# Convert text to numbers
vectorizer = TfidfVectorizer(ngram_range=(1, 2), max_features=5000)
X_train = vectorizer.fit_transform(X_train_raw)
X_test = vectorizer.transform(X_test_raw)

print("Training model...")

# Train model
model = LogisticRegression(max_iter=1000)
model.fit(X_train, y_train)

# Evaluate model
y_pred = model.predict(X_test)
print("\n--- Model Evaluation ---")
print(f"Accuracy: {accuracy_score(y_test, y_pred):.4f}")
print(f"Precision: {precision_score(y_test, y_pred, pos_label='technical'):.4f}")
print(f"Recall: {recall_score(y_test, y_pred, pos_label='technical'):.4f}")
print("\nClassification Report:\n", classification_report(y_test, y_pred))

print("Saving model...")

# Save model
pickle.dump(model, open("model.pkl", "wb"))
pickle.dump(vectorizer, open("vectorizer.pkl", "wb"))

print("Model trained and saved successfully!")
