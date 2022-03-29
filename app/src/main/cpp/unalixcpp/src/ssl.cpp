#include <string>

#include <unistd.h>

#include <bearssl.h>

#include "ssl.hpp"
#include "ssl_certs.hpp"
#include "exceptions.hpp"

/*
 * Low-level data write callback for the simplified SSL I/O API.
 */
static int sock_write(void *ctx, const unsigned char *buf, size_t len)
{
	for (;;) {
		ssize_t wlen;

		wlen = write(*(int *)ctx, buf, len);
		if (wlen <= 0) {
			if (wlen < 0 && errno == EINTR) {
				continue;
			}
			return -1;
		}
		return (int)wlen;
	}
}

/*
 * Low-level data read callback for the simplified SSL I/O API.
 */
static int sock_read(void *ctx, unsigned char *buf, size_t len)
{
	for (;;) {
		ssize_t rlen;

		rlen = read(*(int *)ctx, buf, len);
		if (rlen <= 0) {
			if (rlen < 0 && errno == EINTR) {
				continue;
			}
			return -1;
		}
		return (int)rlen;
	}
}

const int send_encrypted_data(
	int fd,
	const char* server_name, 
	const void *src,
	const size_t src_size,
	void *dst,
	const size_t dst_size
) {
	
	br_ssl_client_context sc;
	br_x509_minimal_context xc;
	unsigned char iobuf[BR_SSL_BUFSIZE_BIDI];
	br_sslio_context ioc;
	
	br_ssl_client_init_full(&sc, &xc, TAs, TAs_NUM);
	br_ssl_engine_set_buffer(&sc.eng, iobuf, sizeof(iobuf), 1);
	br_ssl_client_reset(&sc, server_name, 0);
	br_sslio_init(&ioc, &sc.eng, sock_read, &fd, sock_write, &fd);
	
	br_sslio_write_all(&ioc, src, src_size);
	br_sslio_flush(&ioc);
	
	const int size = br_sslio_read(&ioc, dst, dst_size);
	
	const int rc = br_ssl_engine_last_error(&sc.eng);
	
	if (rc != 0) {
		close(fd);
		
		SSLError e;
		e.set_message("ssl error " + std::to_string(rc));
		
		throw(e);
	}
	
	return size;
}