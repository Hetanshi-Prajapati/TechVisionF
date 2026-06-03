file_path = r'd:\update(27)\update(4)\update(27)\signup\src\main\resources\templates\Roadmap.html'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Replace Node.js link
content = content.replace(
    '"Node.js": "https://www.coursera.org/learn/server-side-nodejs"',
    '"Node.js": "https://www.codecademy.com/learn/learn-node-js"'
)

# Replace React link
content = content.replace(
    '"React": "https://www.coursera.org/learn/react"',
    '"React": "https://www.codecademy.com/learn/react-101"'
)

# Replace React Native link
content = content.replace(
    '"React Native": "https://www.coursera.org/learn/react-native"',
    '"React Native": "https://www.codecademy.com/learn/learn-react-native"'
)

# Replace Azure link
content = content.replace(
    '"Azure": "https://www.coursera.org/learn/microsoft-azure-fundamentals"',
    '"Azure": "https://learn.microsoft.com/en-us/training/azure/"'
)

# Add domain specific Python and Java links
new_entries = """
      "Backend_Python": "https://www.coursera.org/learn/django-web-framework",
      "Data Science_Python": "https://www.coursera.org/learn/python-data-analysis",
      "AI_Python": "https://www.coursera.org/learn/python-for-applied-data-science-ai",
      "Machine Learning_Python": "https://www.coursera.org/learn/machine-learning-with-python",
      "Backend_Java": "https://www.coursera.org/learn/java-programming",
      "Mobile_Java": "https://www.coursera.org/learn/android-app-development-with-java",
      "Python": "https://www.coursera.org/learn/python",
"""
content = content.replace('"Python": "https://www.coursera.org/learn/python",', new_entries)

# Update the lookup logic
content = content.replace(
    'const link = courseLinks[currentLanguage];',
    'const link = courseLinks[`${currentDomain}_${currentLanguage}`] || courseLinks[currentLanguage];'
)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Updated links and logic!")
