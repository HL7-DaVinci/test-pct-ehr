import requests
import time
import sys
from os import walk

sleep = True
duration = 1

# Post the Bundle
if len(sys.argv) > 1:
    base_url = sys.argv[1]
else:
    base_url = 'http://localhost:8081/fhir'

def post_resources(prefix):
    filenames = next(walk(prefix), (None, None, []))[2]  # [] if no file
    headers = {'Accept' : 'application/json', 'Content-Type' : 'application/json'}

    # Post the structure definitions
    for name in filenames:
        split_list = name.split("-")
        url = base_url + "/" + split_list.pop(0)
        id = "-".join(split_list).split(".")[0]
        url += "/" + id
        print(id)
        r = requests.put(url, data=open(prefix + name, 'rb'), headers=headers)
        print(url)

post_resources('./resources/')
time.sleep(5)
post_resources('./references/')
time.sleep(2)
post_resources('./Other/')
