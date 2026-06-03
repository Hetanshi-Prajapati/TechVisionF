import urllib.request
import urllib.parse
import json

def get_coursera_link(query):
    try:
        url = "https://itunes.apple.com/search?term=" + urllib.parse.quote("coursera " + query) + "&entity=software"
        # Just to test internet access
        req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
        response = urllib.request.urlopen(req, timeout=5)
        return response.read().decode('utf-8')[:100]
    except Exception as e:
        return str(e)

print(get_coursera_link("react"))
