/*
RFC 1035 - DOMAIN NAMES - IMPLEMENTATION AND SPECIFICATION
RFC 8484 - DNS Queries over HTTPS (DoH)
RFC 7858 - Specification for DNS over Transport Layer Security (TLS)
*/

#include <string>
#include <sstream>
#include <iomanip>

#include <netinet/in.h>
#include <arpa/inet.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <linux/tcp.h>
#include <unistd.h>
#include <netdb.h>

#include <bearssl.h>

#include "dns.hpp"
#include "uri.hpp"
#include "ssl.hpp"
#include "exceptions.hpp"
#include "http_utils.hpp"

const std::string dns_query(
	const std::string name,
	const QType qtype,
	const int timeout,
	const std::string server
) {
	
	const unsigned int prefix[] = {
		0xAA, 0xAA, 0x01, 0x00,
		0x00, 0x01, 0x00, 0x00,
		0x00, 0x00, 0x00, 0x00
	};
	
	const unsigned int suffix[] = {
		0x00, 0x00, qtype, 0x00, 0x01
	};
	
	std::string domain = name + ".";
	
	const int buffer_length = sizeof(prefix) / sizeof(*prefix) + domain.size() + sizeof(suffix) / sizeof(*suffix);
	unsigned char buffer[buffer_length];
	
	int buffer_índex = 0;
	
	// Header
	for (const unsigned int byte : prefix) {
		buffer[buffer_índex] = byte;
		buffer_índex++;
	}
	
	// Question
	std::string part;
	
	for (const unsigned char ch : domain) {
		if (ch == '.') {
			buffer[buffer_índex] = (unsigned int) part.size();
			buffer_índex++;
			
			for (const unsigned char ch : part) {
				buffer[buffer_índex] = (unsigned int) ch;
				buffer_índex++;
			}
			
			part = "";
		} else {
			part += ch;
		}
	}
	
	for (const unsigned int byte : suffix) {
		buffer[buffer_índex] = byte;
		buffer_índex++;
	}
	
	if (buffer_length < 20) {
		DNSError e;
		e.set_message("DNS packet size too small");
		throw(e);
	}
	
	if (buffer_length > 512) {
		DNSError e;
		e.set_message("DNS packet size too big");
		throw(e);
	}
	
	URI uri = URI::from_string(server);
	
	const bool is_doh = (uri.get_scheme() == "https");
	const bool is_dot = (uri.get_scheme() == "tls");
	const bool is_plain = (uri.get_scheme() == "dns");
	
	const bool is_tcp = (is_doh || is_dot);
	
	int port = 0;
	
	if (uri.get_port() == 0) {
		if (is_dot) {
			port = DOT_PORT;
		} else if (is_doh) {
			port = DOH_PORT;
		} else if (is_plain) {
			port = PLAIN_WIREFORMAT_PORT;
		}
	} else {
		port = uri.get_port();
	}
	
	unsigned char response[1024];
	int response_size;
	
	if (is_tcp) {
		struct addrinfo hints = {};
		hints.ai_family = PF_UNSPEC;
		hints.ai_socktype = SOCK_STREAM;
		
		struct addrinfo *res = {};
	
		int rc = getaddrinfo(uri.get_host().c_str(), std::to_string(port).c_str(), &hints, &res);
		
		if (rc != 0) {
			DNSError e;
			e.set_message(std::string("getaddrinfo: ") + std::string(gai_strerror(rc)));
			throw(e);
		}
		
		int fd = socket(res -> ai_family, res -> ai_socktype, res -> ai_protocol);
		
		if (fd < 0) {
			SocketError e;
			e.set_message(std::string("socket: ") + std::string(strerror(errno)));
			throw(e);
		}
		
		const int optval = 1;
		rc = setsockopt(fd, SOL_TCP, TCP_NODELAY, &optval, sizeof(optval));
		
		if (rc < 0) {
			close(fd);
			
			SocketError e;
			e.set_message(std::string("setsockopt: ") + std::string(strerror(errno)));
			throw(e);
		}
		
		set_socket_timeout(fd, timeout);
		
		rc = connect(fd, res -> ai_addr, res -> ai_addrlen);
		
		freeaddrinfo(res);
		
		if (rc < 0) {
			close(fd);
			
			SocketError e;
			e.set_message(std::string("connect: ") + std::string(strerror(errno)));
			throw(e);
		}
		
		br_ssl_client_context sc;
		br_x509_minimal_context xc;
		unsigned char iobuf[BR_SSL_BUFSIZE_BIDI];
		br_sslio_context ioc;
		
		br_ssl_client_init_full(&sc, &xc, TAs, TAs_NUM);
		br_ssl_engine_set_buffer(&sc.eng, iobuf, sizeof(iobuf), 1);
		br_ssl_client_reset(&sc, uri.get_host().c_str(), 0);
		br_sslio_init(&ioc, &sc.eng, sock_read, &fd, sock_write, &fd);
		
		if (is_dot) {
			// https://datatracker.ietf.org/doc/html/rfc1035#section-4.2.2
			unsigned char buffer_size_prefix[2] = {
				0x00,
				(unsigned char) buffer_length
			};
			
			br_sslio_write_all(&ioc, buffer_size_prefix, sizeof(buffer_size_prefix));
		} else {
			std::string hostname = (port != 443) ? uri.get_host() + ":" + std::to_string(port) : uri.get_host();
			
			if (uri.get_path() == "") {
				uri.set_path("/");
			}
			
			std::string path = (uri.get_query() == "") ? uri.get_path() : uri.get_path() + uri.get_query();
			
			std::string headers = (
				"POST " + path + " HTTP/1.0\r\n" +
				"Host: " + hostname + "\r\n" +
				"Accept: application/dns-udpwireformat\r\n" +
				"Accept-Encoding: identity\r\n" +
				"Connection: close\r\n" +
				"Content-Type: application/dns-udpwireformat\r\n" +
				"Content-Length: " + std::to_string(buffer_length) + "\r\n\r\n"
			);
			
			br_sslio_write_all(&ioc, headers.c_str(), headers.size());
		}
		
		br_sslio_write_all(&ioc, buffer, buffer_length);
		br_sslio_flush(&ioc);
		
		response_size = br_sslio_read(&ioc, response, sizeof(response));
		
		close(fd);
		
		if (br_ssl_engine_current_state(&sc.eng) == BR_SSL_CLOSED) {
			const int err = br_ssl_engine_last_error(&sc.eng);
			
			if (err != 0) {
				SSLError e;
				e.set_message("br_ssl_engine_last_error: " + std::to_string(err));
				throw(e);
			}
		}
	} else {
		const bool is_ipv6 = uri.is_ipv6();
		
		int fd = socket((is_ipv6) ? AF_INET6 : AF_INET, SOCK_DGRAM, IPPROTO_IP);
		
		if (fd < 0) {
			SocketError e;
			e.set_message(std::string("socket: ") + std::string(strerror(errno)));
			throw(e);
		}
		
		set_socket_timeout(fd, timeout);
		
		if (is_ipv6) {
			struct sockaddr_in6 addr;
			addr.sin6_family = AF_INET6;
			inet_pton(AF_INET6, uri.get_ipv6_host().c_str(), &addr.sin6_addr);
			addr.sin6_port = htons(port);
			
			const int size = sendto(fd, buffer, buffer_length, NULL, (struct sockaddr*) &addr, sizeof(addr));
			
			if (size < 0) {
				close(fd);
				
				SocketError e;
				e.set_message(std::string("sendto: ") + std::string(strerror(errno)));
				throw(e);
			}
		} else {
			struct sockaddr_in addr;
			addr.sin_family = AF_INET;
			addr.sin_addr.s_addr = inet_addr(uri.get_host().c_str());
			addr.sin_port = htons(port);
			
			const int size = sendto(fd, buffer, buffer_length, NULL, (struct sockaddr*) &addr, sizeof(addr));
			
			if (size < 0) {
				close(fd);
				
				SocketError e;
				e.set_message(std::string("sendto: ") + std::string(strerror(errno)));
				throw(e);
			}
		}
		
		response_size = recvfrom(fd, response, sizeof(response), 0, NULL, NULL);
		
		if (response_size < 0) {
			close(fd);
			
			SocketError e;
			e.set_message(std::string("recvfrom: ") + std::string(strerror(errno)));
			throw(e);
		}
		
		close(fd);
	}
	
	// https://datatracker.ietf.org/doc/html/rfc1035#section-4.1.1
	unsigned int rcode[2];
	
	for (int index = 0; index < response_size; index++) {
		if (response[index] == 0xAA && response[index + 1] == 0xAA) {
			rcode[0] = response[index + 2];
			rcode[1] = response[index + 3];
			break;
		}
	}
	
	if (!(rcode[0] == 0x81 && rcode[1] == 0x80)) {
		DNSError e;
		e.set_message("Domain doesn't exists");
		throw(e);
	}
	
	std::ostringstream address;
	
	switch (qtype) {
		case A:
			{
				const char rdlength = response[response_size - 5];
				
				if (rdlength != 0x04) {
					DNSError e;
					e.set_message("No address found");
					throw(e);
				}
			}
			
			address << (int) response[response_size - 4] << '.' << (int) response[response_size - 3] << '.' << (int) response[response_size - 2] << '.' << (int) response[response_size - 1];
			
			break;
		case AAAA:
			{
				const char rdlength = response[response_size - 17];
				
				if (rdlength != 0x10) {
					DNSError e;
					e.set_message("No address found");
					throw(e);
				}
			}
			
			const int address_start = response_size - 16;
			
			for (int index = address_start; index < response_size; index += 2) {
				if (index != address_start) {
					address << ':';
				}
				
				address << std::hex << std::setw(2) << std::setfill('0') << (unsigned int) response[index];
				address << std::hex << std::setw(2) << std::setfill('0') << (unsigned int) response[index + 1];
			}
			
			break;
	}
	
	return address.str();
}
