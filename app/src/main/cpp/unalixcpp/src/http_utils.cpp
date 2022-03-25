#include <sys/types.h>
#include <sys/socket.h>
#include <unistd.h>

#include "http_utils.hpp"
#include "exceptions.hpp"

void set_socket_timeout(const int fd, const int seconds) {
	
	// Set socket timeout
	struct timeval timeout;
	timeout.tv_sec = seconds;
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
}
