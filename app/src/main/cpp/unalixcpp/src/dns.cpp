/*
RFC 1035 - DOMAIN NAMES - IMPLEMENTATION AND SPECIFICATION
RFC 8484 - DNS Queries over HTTPS (DoH)
RFC 7858 - Specification for DNS over Transport Layer Security (TLS)
*/

#include <string>
#include <sstream>
#include <iomanip>
#include <vector>
#include <iostream>

#include <netinet/in.h>
#include <arpa/inet.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <linux/tcp.h>
#include <unistd.h>
#include <netdb.h>

#include "dns.hpp"
#include "uri.hpp"
#include "ssl.hpp"
#include "exceptions.hpp"
#include "socket.hpp"

const std::string get_address(
	const std::string hostname,
	int family
) {
	
	struct addrinfo hints = {};
	hints.ai_family = PF_UNSPEC;
	hints.ai_socktype = SOCK_STREAM;
	
	struct addrinfo *res = {};
	
	int rc;
	
	rc = getaddrinfo(hostname.c_str(), "0", &hints, &res);
		
	if (rc != 0) {
		GAIError e;
		e.set_message(gai_strerror(rc));
		
		throw(e);
	}
	
	family = res -> ai_family;
	
	char host[NI_MAXHOST];
	rc = getnameinfo(res -> ai_addr, res -> ai_addrlen, host, NI_MAXHOST, NULL, 0, NI_NUMERICHOST);
	
	freeaddrinfo(res);
	
	if (rc != 0) {
		GAIError e;
		e.set_message(gai_strerror(rc));
		
		throw(e);
	}
	
	return std::string(host);
}

const std::vector<char> encode_query(
	const std::string name,
	const QType qtype
) {
	
	// Header section
	std::vector<char> buffer = {
		(char) 0xAA, (char) 0xAA, 0x01, 0x00,
		0x00, 0x01, 0x00, 0x00,
		0x00, 0x00, 0x00, 0x00
	};
	
	std::string domain = name + ".";
	
	// Question section
	std::vector<char> data;
	
	for (const char ch : domain) {
		if (ch == '.') {
			buffer.push_back(data.size());
			buffer.insert(buffer.end(), data.begin(), data.end());
			
			data.clear();
		} else {
			data.push_back(ch);
		}
	}
	
	buffer.insert(buffer.end(), {0x00, 0x00, (char) qtype, 0x00, 0x01});
	
	const int buffer_size = buffer.size();
	
	if (buffer_size < 20) {
		ValueError e;
		e.set_message("DNS packet size too small");
		throw(e);
	}
	
	if (buffer_size > 512) {
		ValueError e;
		e.set_message("DNS packet size too big");
		throw(e);
	}
	
	return buffer;
}

const std::vector<char> encode_doh_query(
	const std::string name,
	const QType qtype,
	const std::string server
) {
	
	std::vector<char> buffer = encode_query(name, qtype);
	
	URI uri = URI::from_string(server);
	
	const int port = (uri.get_port() == 0) ? DEFAULT_DOH_PORT : uri.get_port();
	const std::string hostname = (port == 443) ? uri.get_host() : uri.get_host() + ":" + std::to_string(port);
	
	if (uri.get_path() == "") {
		uri.set_path("/");
	}
	
	const std::string path = (uri.get_query() == "") ? uri.get_path() : uri.get_path() + uri.get_query();
	
	const std::string headers = (
		"POST " + path + " HTTP/1.0\r\n" +
		"Host: " + hostname + "\r\n" +
		"Accept: application/dns-udpwireformat\r\n" +
		"Accept-Encoding: identity\r\n" +
		"Connection: close\r\n" +
		"Content-Type: application/dns-udpwireformat\r\n" +
		"Content-Length: " + std::to_string(buffer.size()) + "\r\n\r\n"
	);
	
	buffer.insert(buffer.begin(), headers.begin(), headers.end());
	
	return buffer;
}

const std::vector<char> encode_tcp_query(
	const std::string name,
	const QType qtype
) {
	std::vector<char> buffer = encode_query(name, qtype);
	const int buffer_size = buffer.size();
	
	buffer.insert(buffer.begin(), {0x00, (char) buffer_size});
	
	return buffer;
}
	
const std::string dns_query(
	const std::string name,
	const QType qtype,
	const int timeout,
	const std::string server
) {
	
	DNSSpecification spec;
	
	int socket_type;
	
	URI uri = URI::from_string(server);
	
	if (uri.get_scheme() == "https") {
		spec = DNS_SPEC_DOH;
		socket_type = SOCK_STREAM;
	} else if (uri.get_scheme() == "tls") {
		spec = DNS_SPEC_DOT;
		socket_type = SOCK_STREAM;
	} else if (uri.get_scheme() == "udp") {
		spec = DNS_SPEC_WIREFORMAT_UDP;
		socket_type = SOCK_DGRAM;
	} else if (uri.get_scheme() == "tcp") {
		spec = DNS_SPEC_WIREFORMAT_TCP;
		socket_type = SOCK_STREAM;
	} else {
		UnsupportedProtocolError e;
		e.set_message("Unrecognized URI or unsupported protocol");
		
		throw(e);
	}
	
	// Encode raw DNS query
	std::vector<char> query;
	
	switch (spec) {
		case DNS_SPEC_DOH:
			query = encode_doh_query(name, qtype, server);
			break;
		case DNS_SPEC_WIREFORMAT_TCP:
		case DNS_SPEC_DOT:
			query = encode_tcp_query(name, qtype);
			break;
		case DNS_SPEC_WIREFORMAT_UDP:
			query = encode_query(name, qtype);
			break;
	}
	
	const char* buffer = query.data();
	const int buffer_size = query.size();
	
	// Get address of the DNS server
	int port = uri.get_port();
		
	if (port == 0) {
		switch (spec) {
			case DNS_SPEC_DOH:
				port = DEFAULT_DOH_PORT;
				break;
			case DNS_SPEC_DOT:
				port = DEFAULT_DOT_PORT;
				break;
			case DNS_SPEC_WIREFORMAT_UDP:
			case DNS_SPEC_WIREFORMAT_TCP:
				port = DEFAULT_WIREFORMAT_PORT;
				break;
		}
	}
	
	std::string addr;
	int addr_family;
	
	if (uri.is_ipv4()) {
		addr = uri.get_host();
		addr_family = AF_INET;
	} else if (uri.is_ipv6()) {
		addr = uri.get_ipv6_host();
		addr_family = AF_INET6;
	} else {
		addr = get_address(uri.get_host(), addr_family);
	}
	
	struct sockaddr* socket_address;
	int socket_address_size;
	
	switch (addr_family) {
		case AF_INET:
			struct sockaddr_in addr_v4;
			addr_v4.sin_family = addr_family;
			addr_v4.sin_addr.s_addr = inet_addr(addr.c_str());
			addr_v4.sin_port = htons(port);
			
			socket_address = (struct sockaddr*) &addr_v4;
			socket_address_size = sizeof(struct sockaddr_in);
			
			break;
		case AF_INET6:
			struct sockaddr_in6 addr_v6;
			addr_v6.sin6_family = addr_family;
			inet_pton(addr_family, addr.c_str(), &addr_v6.sin6_addr);
			addr_v6.sin6_port = htons(port);
			
			socket_address = (struct sockaddr*) &addr_v6;
			socket_address_size = sizeof(sockaddr_in6);
			
			break;
	}
	
	// Send query
	char response[1024];
	size_t response_size;
	
	int fd = create_socket(addr_family, socket_type, IPPROTO_IP, timeout);
	
	if (socket_type == SOCK_STREAM) {
		connect_socket(fd, socket_address, socket_address_size);
		
		if (spec == DNS_SPEC_DOH || spec == DNS_SPEC_DOT) {
			response_size = send_encrypted_data(fd, uri.get_host().c_str(), buffer, buffer_size, response, sizeof(response));
		} else {
			response_size = send_tcp_data(fd, buffer, buffer_size, response, sizeof(response));
		}
	} else {
		response_size = send_udp_data(fd, buffer, buffer_size, response, sizeof(response), socket_address, socket_address_size);
	}
	
	close(fd);
	
	// Parse query
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
