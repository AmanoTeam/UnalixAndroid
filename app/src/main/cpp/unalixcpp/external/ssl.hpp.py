import subprocess
import tempfile
import os
import http.client
import urllib.parse
import hashlib

CA_CERT_URL = "https://curl.se/ca/cacert-2022-03-18.pem"
CA_CERT_SHA256 = "2d0575e481482551a6a4f9152e7d2ab4bafaeaee5f2606edb829c2fdb3713336"

source = """\
#include <unistd.h>
#include <errno.h>

#include <bearssl.h>

/*
Automatically generated trust anchors. Use '../external/ssl.hpp.py' to modify/regenerate.
*/
{}

/*
 * Low-level data write callback for the simplified SSL I/O API.
 */
static int sock_write(void *ctx, const unsigned char *buf, size_t len)
{{
	for (;;) {{
		ssize_t wlen;

		wlen = write(*(int *)ctx, buf, len);
		if (wlen <= 0) {{
			if (wlen < 0 && errno == EINTR) {{
				continue;
			}}
			return -1;
		}}
		return (int)wlen;
	}}
}}

/*
 * Low-level data read callback for the simplified SSL I/O API.
 */
static int sock_read(void *ctx, unsigned char *buf, size_t len)
{{
	for (;;) {{
		ssize_t rlen;

		rlen = read(*(int *)ctx, buf, len);
		if (rlen <= 0) {{
			if (rlen < 0 && errno == EINTR) {{
				continue;
			}}
			return -1;
		}}
		return (int)rlen;
	}}
}}

"""

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

source_file = os.path.realpath("../src/ssl.hpp")

print("Exporting to %s" % source_file)

with open(file = source_file, mode = "w") as hpp:
	hpp.write(source.format(header.decode()))