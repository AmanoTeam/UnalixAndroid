#include <sys/types.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <linux/tcp.h>
#include <unistd.h>

#include "socket.hpp"
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

const void connect_socket(
	int fd, 
	const struct sockaddr *src_addr,
	const socklen_t addrlen
) {
	
	const int rc = connect(fd, src_addr, addrlen);
	
	if (rc < 0) {
		close(fd);
	
		ConnectError e;
		e.set_message(strerror(errno));
		
		throw(e);
	}
}

const ssize_t send_tcp_data(
	int fd, 
	const void *src,
	const size_t src_size,
	void *dst,
	const size_t dst_size
) {
	
	ssize_t size;
	
	size = send(fd, src, src_size, NULL);
	
	if (size != src_size) {
		close(fd);
	
		SendError e;
		e.set_message(strerror(errno));
		
		throw(e);
	}
	
	size = recv(fd, dst, dst_size, NULL);
	
	switch (size) {
		case -1:
			close(fd);
			
			{
				RecvError e;
				e.set_message(strerror(errno));
				
				throw(e);
			}
		case 0:
			close(fd);
			
			{
				RecvError e;
				e.set_message("Server closed connection unexpectedly");
				
				throw(e);
			}
	}
	
	return size;
}

const ssize_t send_udp_data(
	int fd, 
	const void *src,
	const size_t src_size,
	void *dst,
	const size_t dst_size,
	const struct sockaddr *src_addr,
	const socklen_t addrlen
) {
	
	ssize_t size;
	
	size = sendto(fd, src, src_size, NULL, src_addr, addrlen);
	
	if (size != src_size) {
		close(fd);
	
		SendError e;
		e.set_message(strerror(errno));
		
		throw(e);
	}
	
	size = recvfrom(fd, dst, dst_size, NULL, NULL, NULL);
	
	switch (size) {
		case -1:
			close(fd);
			
			{
				RecvError e;
				e.set_message(strerror(errno));
				
				throw(e);
			}
		case 0:
			close(fd);
			
			{
				RecvError e;
				e.set_message("Server closed connection unexpectedly");
				
				throw(e);
			}
	}
	
	return size;
}