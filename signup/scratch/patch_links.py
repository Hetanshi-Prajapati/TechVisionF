import re

file_path = r'd:\update(27)\update(4)\update(27)\signup\src\main\resources\templates\Roadmap.html'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

new_links = """
      "HTML": "https://www.coursera.org/learn/html",
      "CSS": "https://www.coursera.org/learn/css",
      "JavaScript": "https://www.coursera.org/learn/javascript",
      "Angular": "https://www.coursera.org/learn/angular",
      "Vue": "https://www.coursera.org/learn/vue-js",
      "Java": "https://www.coursera.org/learn/java",
      "PHP": "https://www.coursera.org/learn/php",
      "Spring Boot": "https://www.coursera.org/learn/spring-framework",
      "Django": "https://www.coursera.org/learn/django",
      "Express": "https://www.coursera.org/learn/express",
      "Keras": "https://www.coursera.org/learn/keras",
      "PyTorch": "https://www.coursera.org/learn/pytorch",
      "Pandas": "https://www.coursera.org/learn/pandas",
      "NumPy": "https://www.coursera.org/learn/numpy",
      "Matplotlib": "https://www.coursera.org/learn/matplotlib",
      "Kotlin": "https://www.coursera.org/learn/kotlin-for-java-developers",
      "Swift": "https://www.coursera.org/learn/swift",
      "Docker": "https://www.coursera.org/learn/docker",
      "Jenkins": "https://www.coursera.org/learn/jenkins",
      "GitHub Actions": "https://www.coursera.org/learn/github-actions",
      "Google Cloud": "https://www.coursera.org/learn/google-cloud",
      "Kali Linux": "https://www.coursera.org/learn/kali-linux",
      "Metasploit": "https://www.coursera.org/learn/metasploit",
      "Burp Suite": "https://www.coursera.org/learn/burp-suite",
"""

# Find courseLinks definition
target = r'"Security Basics": "https://www.coursera.org/learn/it-security-basics"'
if target in content:
    replacement = target + ",\n" + new_links.rstrip(",\n")
    new_content = content.replace(target, replacement)
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(new_content)
    print("Course links patched successfully!")
else:
    print("Could not find Security Basics line.")
