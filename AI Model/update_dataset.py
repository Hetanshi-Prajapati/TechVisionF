import pandas as pd
import random

# Load existing dataset
df = pd.read_csv('vtech_final_dataset_2500.csv')

# Existing technical samples
technical_keywords = [
    "Java", "Python", "C++", "JavaScript", "SQL", "APIs", "backend", "frontend",
    "System design", "architecture", "microservices", "ERD", "Entity Relationship Diagram",
    "DFD", "Data Flow Diagram", "CFD", "Control Flow Diagram", "UML diagrams", "flowcharts",
    "Power BI", "Tableau", "dashboards", "charts", "bar chart", "line chart", "pie chart",
    "data visualization", "analytics reports", "machine learning", "deep learning",
    "neural networks", "SQL queries", "schema", "normalization", "Kubernetes", "Docker",
    "AWS", "GCP", "Azure", "CI/CD", "JUnit", "Mockito", "React", "Node.js", "Express",
    "Tailwind CSS", "Redux", "TypeScript", "Django", "Flask", "PostgreSQL", "MongoDB"
]

technical_phrases = [
    "How to implement {} in project",
    "Explain {} with examples",
    "Best practices for {}",
    "Troubleshooting {} issues",
    "{} tutorial for beginners",
    "Comparison between {} and {}",
    "Integrating {} with other tools",
    "Optimizing {} performance",
    "{} configuration and setup",
    "{} documentation and guides",
    "{} architecture for scalable apps",
    "Designing {} for enterprise systems",
    "{} schema for database management",
    "{} visualization for data analysis",
    "{} dashboard for real-time monitoring",
    "{} diagram for process mapping",
    "{} queries for data retrieval",
    "{} model for predictive analytics",
    "{} testing for quality assurance",
    "{} deployment on cloud platforms"
]

# Generate more technical samples
new_technical = []
for _ in range(1200):
    keyword = random.choice(technical_keywords)
    phrase = random.choice(technical_phrases)
    try:
        if phrase.count("{}") == 2:
            keyword2 = random.choice(technical_keywords)
            new_technical.append(phrase.format(keyword, keyword2))
        else:
            new_technical.append(phrase.format(keyword))
    except:
        new_technical.append(f"{keyword} {phrase}")

# Add specific technical samples requested by user
user_technical = [
    "Draw ER diagram for hospital management system",
    "Explain DFD for e-commerce system",
    "Power BI dashboard showing sales analytics",
    "Tableau visualization of revenue trends",
    "SQL query for joining tables",
    "System architecture diagram",
    "Flowchart of login system",
    "Neural network model diagram",
    "Technical skills: The abilities and knowledge needed to accomplish complex actions",
    "Definition of technical proficiency and soft skills in engineering",
    "What are technical skills? Examples include programming and data analysis",
    "Infographic explaining core technical competencies for developers"
]
new_technical.extend(user_technical)

# Add specific non-technical samples requested by user
user_non_technical = [
    "good morning",
    "hello bro",
    "nice pic",
    "happy birthday",
    "love this image"
]

# Create a new dataframe with new samples
new_df_tech = pd.DataFrame({'content': new_technical, 'label': 'technical'})
new_df_non_tech = pd.DataFrame({'content': user_non_technical, 'label': 'non_technical'})

# Combine with existing dataframe
df = pd.concat([df, new_df_tech, new_df_non_tech], ignore_index=True)

# Clean dataset: lowercase, remove duplicates
df['content'] = df['content'].str.lower()
df = df.drop_duplicates(subset=['content'])

# Balance the dataset to roughly 50/50
tech_count = len(df[df['label'] == 'technical'])
non_tech_count = len(df[df['label'] == 'non_technical'])

print(f"Initial Tech: {tech_count}, Non-Tech: {non_tech_count}")

target_count = min(tech_count, non_tech_count)

df_tech = df[df['label'] == 'technical'].sample(n=target_count, random_state=42)
df_non_tech = df[df['label'] == 'non_technical'].sample(n=target_count, random_state=42)

df_balanced = pd.concat([df_tech, df_non_tech]).sample(frac=1, random_state=42)

print(f"Final Balanced Dataset: {len(df_balanced)} rows")
print(df_balanced['label'].value_counts())

# Save balanced dataset
df_balanced.to_csv('vtech_final_dataset_2500.csv', index=False)
