import subprocess
import tempfile
import os
import http.client
import urllib.parse
import hashlib

CA_CERT_URL = "https://curl.se/ca/cacert-2022-04-26.pem"
CA_CERT_SHA256 = "08df40e8f528ed283b0e480ba4bcdbfdd2fdcf695a7ada1668243072d80f8b6f"

source = """\
#include <bearssl.h>

/*
Automatically generated trust anchors. Use '../external/ssl_certs.hpp.py' to modify/regenerate.
*/
{}"""

url = urllib.parse.urlparse(url = CA_CERT_URL)

print("Fetching data from %s" % CA_CERT_URL)

connection = http.client.HTTPSConnection(
	host = url.netloc,
	port = url.port
)

connection.request(
	method = "GET",
	url = url.path
)
response = connection.getresponse()
content = response.read()

connection.close()

assert hashlib.sha256(content).hexdigest() == CA_CERT_SHA256, "SHA 256 didn't match"

with tempfile.TemporaryDirectory() as directory:
	file = os.path.join(directory, "cacert.pem")
	
	with open(file = file, mode = "wb") as cacert:
		cacert.write(content)
	
	header = subprocess.check_output(["brssl", "ta", file])

source_file = os.path.realpath("../src/ssl_certs.hpp")

print("Exporting to %s" % source_file)

with open(file = source_file, mode = "w") as hpp:
	hpp.write(source.format(header.decode()))