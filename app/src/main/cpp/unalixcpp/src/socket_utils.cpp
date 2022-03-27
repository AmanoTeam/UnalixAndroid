#include <sys/types.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <linux/tcp.h>
#include <unistd.h>

#include "socket_utils.hpp"
#include "exceptions.hpp"

const int create_socket(
	const int domain,
	const int type,
	const int protocol,
	const int timeout_in_seconds
) {
	
	int fd = socket(domain, type, protocol);
	
	if (fd < 0) {
		SocketError e;
		e.set_message(strerror(errno));
		
		throw(e);
	}
	
	if (type == SOCK_STREAM) {
		const int optval = 1;
		const int rc = setsockopt(fd, SOL_TCP, TCP_NODELAY, &optval, sizeof(optval));
	
		if (rc < 0) {
			close(fd);
			
			SocketError e;
			e.set_message(std::string("setsockopt: ") + std::string(strerror(errno)));
			
			throw(e);
		}
	}
	
	// Set socket timeout
	struct timeval timeout;
	timeout.tv_sec = timeout_in_seconds;
	timeout.tv_usec = 0;
	
	int rc = setsockopt(fd, SOL_SOCKET, SO_RCVTIMEO, &timeout, sizeof(timeout));
	
	if (rc < 0) {
		close(fd);
		
		SocketError e;
		e.set_message(std::string("setsockopt: ") + std::string(strerror(errno)));
		throw(e);
	}
	
	rc = setsockopt(fd, SOL_SOCKET, SO_SNDTIMEO, &timeout, sizeof(timeout));
	
	if (rc < 0) {
		close(fd);
		
		SocketError e;
		e.set_message(std::string("setsockopt: ") + std::string(strerror(errno)));
		throw(e);
	}
	
	return fd;
}
