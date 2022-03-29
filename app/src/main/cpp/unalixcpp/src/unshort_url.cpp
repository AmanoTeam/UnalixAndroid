#include <regex>
#include <filesystem>
#include <string>

#include <netinet/in.h>
#include <arpa/inet.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <linux/tcp.h>
#include <unistd.h>
#include <netdb.h>

#include "clear_url.hpp"
#include "unshort_url.hpp"
#include "ssl.hpp"
#include "uri.hpp"
#include "dns.hpp"
#include "exceptions.hpp"
#include "socket.hpp"

// Setters
void Response::set_http_version(const float value) {
	this -> http_version = value;
}

void Response::set_status_code(const int value) {
	this -> status_code = value;
}

void Response::set_status_message(const std::string value) {
	this -> status_message = value;
}

void Response::set_headers(const std::vector<std::tuple<std::string, std::string>> value) {
	this -> headers = value;
}

void Response::set_body(const std::string value) {
	this -> body = value;
}

void Response::set_location(const std::string value) {
	this -> location = value;
}

// Getters
const float Response::get_http_version() const {
	return this -> http_version;
}

const int Response::get_status_code() const {
	return this -> status_code;
}

const std::string Response::get_location() const {
	return this -> location;
}

const std::string Response::get_status_message() const {
	return this -> status_message;
}

const std::vector<std::tuple<std::string, std::string>> Response::get_headers() const {
	return this -> headers;
}

const std::string Response::get_body() const {
	return this -> body;
}

const std::string Response::get_header(const std::string name) const {
	for (const std::tuple<std::string, std::string> header : this -> headers) {
		if (std::get<0>(header) == name) {
			return std::get<1>(header);
		}
	}
	
	return "";
}

const bool Response::has_header(const std::string name) const {
	for (const std::tuple<std::string, std::string> header : this -> headers) {
		if (std::get<0>(header) == name) {
			return true;
		}
	}
	
	return false;
}

const bool Response::is_redirect() const {
	return (this -> get_status_code() >= 300 && this -> get_status_code() <= 399 && this -> get_header("Location") != "");
}

const std::string unshort_url(
	const std::string url,
	const bool ignore_referral_marketing,
	const bool ignore_rules,
	const bool ignore_exceptions,
	const bool ignore_raw_rules,
	const bool ignore_redirections,
	const bool skip_blocked,
	const int timeout,
	const int max_redirects,
	const std::string dns
) {
	
	int total_redirects = 0;
	
	std::string this_url = clear_url(
		url,
		ignore_referral_marketing,
		ignore_rules,
		ignore_exceptions,
		ignore_raw_rules,
		ignore_redirections,
		skip_blocked
	);
	
	if (!(this_url.starts_with("http") || this_url.starts_with("https"))) {
		UnsupportedProtocolError e;
		e.set_message("Unrecognized URI or unsupported protocol");
		e.set_url(this_url);
		
		throw(e);
	}
	
	while (true) {
		URI uri = URI::from_string(this_url);
		
		int port = 0;
		
		if (uri.get_port() == 0) {
			if (uri.get_scheme() == "http") {
				port = 80;
			} else {
				port = 443;
			}
		} else {
			port = uri.get_port();
		}
		
		std::string hostname;
		
		if ((uri.get_scheme() == "http" && port != 80) || (uri.get_scheme() == "https" && port != 443)) {
			hostname = uri.get_host() + ":" + std::to_string(port);
		} else {
			hostname = uri.get_host();
		}
		
		std::string data = "GET ";
		
		if (uri.get_path() == "") {
			uri.set_path("/");
		}
		
		if (uri.get_query() == "") {
			data.append(uri.get_path());
		} else {
			data.append(uri.get_path() + uri.get_query());
		}
		
		data.append(" HTTP/1.0\r\nHost: " + hostname + "\r\n");
		
		for (auto header : request_headers) {
			const std::string key = header[0];
			const std::string value = header[1];
			
			data.append(key + ": " + value + "\r\n");
		}
		
		data.append("\r\n");
		
		std::string addr;
		int addr_family;
		
		if (uri.is_ipv4()) {
			addr = uri.get_host();
			addr_family = AF_INET;
		} else if (uri.is_ipv6()) {
			addr = uri.get_ipv6_host();
			addr_family = AF_INET6;
		} else if (dns == "") {
			addr = get_address(uri.get_host(), addr_family);
		} else {
			const QType qtypes[2] = {A, AAAA};
			
			for (const QType qtype : qtypes) {
				try {
					addr = dns_query(uri.get_host(), qtype, timeout, dns);
				} catch (UnalixException &e) {
					if (e.get_message() == "Domain doesn't exists") {
						e.set_url(this_url);
						throw;
					}
					
					if (qtype == A) {
						continue;
					}
					
					e.set_url(this_url);
					throw;
				}
				
				addr_family = (qtype == A) ? AF_INET : AF_INET6;
				
				break;
			}
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
		
		int fd;
		
		const char* request = data.c_str();
		const size_t request_size = data.length();
		
		char response[1024];
		const size_t response_size = sizeof(response);
		
		try {
			fd = create_socket(addr_family, SOCK_STREAM, IPPROTO_TCP, timeout);
			
			connect_socket(fd, socket_address, socket_address_size);
			
			if (uri.get_scheme() == "https") {
				send_encrypted_data(fd, uri.get_host().c_str(), request, request_size, response, response_size);
			} else {
				send_tcp_data(fd, request, request_size, response, response_size);
			}
		} catch (UnalixException &e) {
			e.set_url(this_url);
			throw;
		}
			
		close(fd);
		
		const std::string raw_response = response;
		
		const size_t index = raw_response.find("\r\n\r\n");
		const std::string raw_headers = raw_response.substr(0, index);
		const std::string body = raw_response.substr(index + 4, raw_response.length());
		
		Response r = Response();
		
		r.set_body(body);
		
		const std::string::const_iterator headers_start = raw_headers.begin();
		const std::string::const_iterator headers_end = raw_headers.end();
		
		const std::string::const_iterator http_version_start = std::find(headers_start, headers_end, '/') + 1;
		const std::string::const_iterator http_version_end = std::find(http_version_start, headers_end, ' ');
		
		const float http_version = std::stof(std::string(http_version_start, http_version_end).c_str());
		
		if (!(http_version == 1.0f || http_version == 1.1f)) {
			RemoteProtocolError e;
			e.set_message("Unsupported protocol version: " + std::to_string(http_version));
			e.set_url(this_url);
			
			throw(e);
		}
		
		r.set_http_version(http_version);
		
		const std::string::const_iterator status_code_start = std::find(http_version_end, headers_end, ' ') + 1;
		const std::string::const_iterator status_code_end = std::find(status_code_start, headers_end, ' ');
		
		const int status_code = std::stoi(std::string(status_code_start, status_code_end));
		
		std::string status_message;
		
		switch (status_code) {
			case 100:
				status_message = "Continue";
				break;
			case 101:
				status_message = "Switching Protocols";
				break;
			case 200:
				status_message = "OK";
				break;
			case 201:
				status_message = "Created";
				break;
			case 202:
				status_message = "Accepted";
				break;
			case 203:
				status_message = "Non-Authoritative Information";
				break;
			case 204:
				status_message = "Non-Authoritative Information";
				break;
			case 205:
				status_message = "Reset Content";
				break;
			case 206:
				status_message = "Partial Content";
				break;
			case 300:
				status_message = "Multiple Choices";
				break;
			case 301:
				status_message = "Moved Permanently";
				break;
			case 302:
				status_message = "Found";
				break;
			case 303:
				status_message = "See Other";
				break;
			case 304:
				status_message = "Not Modified";
				break;
			case 305:
				status_message = "Use Proxy";
				break;
			case 307:
				status_message = "Temporary Redirect";
				break;
			case 400:
				status_message = "Bad Request";
				break;
			case 401:
				status_message = "Unauthorized";
				break;
			case 402:
				status_message = "Payment Required";
				break;
			case 403:
				status_message = "Forbidden";
				break;
			case 404:
				status_message = "Not Found";
				break;
			case 405:
				status_message = "Method Not Allowed";
				break;
			case 406:
				status_message = "Not Acceptable";
				break;
			case 407:
				status_message = "Proxy Authentication Required";
				break;
			case 408:
				status_message = "Request Time-out";
				break;
			case 409:
				status_message = "Conflict";
				break;
			case 410:
				status_message = "Gone";
				break;
			case 411:
				status_message = "Length Required";
				break;
			case 412:
				status_message = "Precondition Failed";
				break;
			case 413:
				status_message = "Request Entity Too Large";
				break;
			case 414:
				status_message = "Request-URI Too Large";
				break;
			case 415:
				status_message = "Unsupported Media Type";
				break;
			case 416:
				status_message = "Requested range not satisfiable";
				break;
			case 417:
				status_message = "Expectation Failed";
				break;
			case 500:
				status_message = "Internal Server Error";
				break;
			case 501:
				status_message = "Not Implemented";
				break;
			case 502:
				status_message = "Bad Gateway";
				break;
			case 503:
				status_message = "Service Unavailable";
				break;
			case 504:
				status_message = "Gateway Time-out";
				break;
			case 505:
				status_message = "HTTP Version not supported";
				break;
			default:
				RemoteProtocolError e;
				e.set_message("Unknown status code: " + std::to_string(status_code));
				e.set_url(this_url);
				
				throw(e);
		}
		
		r.set_status_message(status_message);
		r.set_status_code(status_code);
		
		std::vector<std::tuple<std::string, std::string>> headers;
		std::string::const_iterator header_end = status_code_end;
		
		while (header_end != headers_end) {
			const std::string::const_iterator header_start = std::find(header_end, headers_end, '\r') + 2;
			header_end = std::find(header_start, headers_end, '\r');
			
			const std::string header = std::string(header_start, header_end);
			
			const std::string::const_iterator key_end = std::find(header_start, header_end, ':');
			
			const std::string key = std::string(header_start, key_end);
			const std::string value = std::string(key_end + 2, header_end);
			
			headers.push_back(std::make_tuple(key, value));
		}
		
		r.set_headers(headers);
		
		std::string location;
		
		if (r.get_status_code() >= 300 && r.get_status_code() <= 399 && r.has_header("Location")) {
			location = r.get_header("Location");
		} else if (r.get_status_code() >= 200 && r.get_status_code() <= 299 && r.has_header("Content-Location")) {
			location = r.get_header("Content-Location");
		}
		
		if (location != "") {
			size_t pos = location.find(' ');
			
			while (pos != std::string::npos) {
				location.replace(pos, 1, "%20");
				pos = location.find(' ');
			}
			
			if (!(location.starts_with("https://") || location.starts_with("http://"))) {
				if (location.starts_with("//")) {
					location = (
						uri.get_scheme() +
						"://" +
						std::regex_replace(location, std::regex("^/*"), "")
					);
				} else if (location.starts_with("/")) {
					location = (
						uri.get_scheme() +
						"://" +
						hostname +
						location
					);
				} else {
					location = (
						uri.get_scheme() +
						"://" +
						hostname +
						((uri.get_path() == "/") ? uri.get_path() : std::string(std::filesystem::path(uri.get_path()).parent_path())) +
						location
					);
				}
			}
			
			// Avoid redirect loops
			if (location == this_url) {
				return this_url;
			}
			
			// Strip tracking fields from the redirect URL
			this_url = clear_url(
				location,
				ignore_referral_marketing,
				ignore_rules,
				ignore_exceptions,
				ignore_raw_rules,
				ignore_redirections,
				skip_blocked
			);
			
			total_redirects++;
			
			if (total_redirects > max_redirects) {
				TooManyRedirectsError e;
				e.set_message("Exceeded maximum allowed redirects");
				e.set_url(this_url);
				
				throw(e);
			}
			
			continue;
		}
		
		break;
	}
	
	return this_url;
}
