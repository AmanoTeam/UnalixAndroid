#include <string>
#include <regex>
#include <filesystem>
#include <vector>

#include <netinet/in.h>
#include <arpa/inet.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <linux/tcp.h>
#include <unistd.h>
#include <netdb.h>

#include <bearssl.h>

#include "ssl.hpp"
#include "uri.hpp"
#include "clear_url.hpp"
#include "exceptions.hpp"

struct Response {
	private:
		float http_version;
		int status_code;
		std::string status_message;
		std::vector<std::tuple<std::string, std::string>> headers;
		std::string body;
	public:
		void set_http_version(const float value) {
			this -> http_version = value;
		}
		
		const float get_http_version() const {
			return this -> http_version;
		}
		
		void set_status_code(const int value) {
			this -> status_code = value;
		}
		
		const int get_status_code() const {
			return this -> status_code;
		}
		
		void set_status_message(const std::string value) {
			this -> status_message = value;
		}
		
		const std::string get_status_message() const {
			return this -> status_message;
		}
		
		void set_headers(const std::vector<std::tuple<std::string, std::string>> value) {
			this -> headers = value;
		}
		
		const std::vector<std::tuple<std::string, std::string>> get_headers() const {
			return this -> headers;
		}
		
		void set_body(const std::string value) {
			this -> body = value;
		}
		
		const std::string get_body() const {
			return this -> body;
		}
		
		const bool has_header(const std::string name) const {
			for (const std::tuple<std::string, std::string> header : this -> headers) {
				if (std::get<0>(header) == name) {
					return true;
				}
			}
			
			return false;
		}
		
		const std::string get_header(const std::string name) const {
			for (const std::tuple<std::string, std::string> header : this -> headers) {
				if (std::get<0>(header) == name) {
					return std::get<1>(header);
				}
			}
			
			return NULL;
		}
		
};

const std::string request_headers[][2] = {
	{"Accept", "*/*"},
	{"Accept-Encoding", "identity"},
	{"Connection", "close"},
	{"User-Agent", "UnalixAndroid/0.8 (+https://github.com/AmanoTeam/UnalixAndroid)"}
};

const std::string unshort_url(
	const std::string url,
	const bool ignore_referral_marketing = false,
	const bool ignore_rules = false,
	const bool ignore_exceptions = false,
	const bool ignore_raw_rules = false,
	const bool ignore_redirections  = false,
	const bool skip_blocked = false,
	const int timeout = 5,
	const int max_redirects = 13
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
		
		int fd = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
		
		if (fd == -1) {
			SocketError e;
			e.set_message(std::string("socket: ") + std::string(strerror(errno)));
			e.set_url(this_url);
			
			throw(e);
		}
		
		int rc;
		
		const int optval = 1;
		rc = setsockopt(fd, SOL_TCP, TCP_NODELAY, &optval, sizeof(optval));
		
		if (rc == -1) {
			close(fd);
			
			SocketError e;
			e.set_message(std::string("setsockopt: ") + std::string(strerror(errno)));
			e.set_url(this_url);
			
			throw(e);
		}
		
		// Set socket timeout
		struct timeval timeouts;
		timeouts.tv_sec = timeout;
		timeouts.tv_usec = 0;
		
		rc = setsockopt(fd, SOL_SOCKET, SO_RCVTIMEO, &timeouts, sizeof(timeouts));
		
		if (rc == -1) {
			close(fd);
			
			SocketError e;
			e.set_message(std::string("setsockopt: ") + std::string(strerror(errno)));
			e.set_url(this_url);
			
			throw(e);
		}
		
		rc = setsockopt(fd, SOL_SOCKET, SO_SNDTIMEO, &timeouts, sizeof(timeouts));
		
		if (rc == -1) {
			close(fd);
			
			SocketError e;
			e.set_message(std::string("setsockopt: ") + std::string(strerror(errno)));
			e.set_url(this_url);
			
			throw(e);
		}
		
		struct addrinfo hints = {};
		hints.ai_family = AF_INET;
		hints.ai_socktype = SOCK_STREAM;
		hints.ai_protocol = IPPROTO_TCP;
		
		struct addrinfo *res = {};
		
		rc = getaddrinfo((uri.get_host()).c_str(), std::to_string(port).c_str(), &hints, &res);
		
		if (rc != 0) {
			close(fd);
			
			DNSError e;
			e.set_message(std::string("getaddrinfo: ") + std::string(gai_strerror(rc)));
			e.set_url(this_url);
			
			throw(e);
		}
		
		rc = connect(fd, res -> ai_addr, res -> ai_addrlen);
		
		freeaddrinfo(res);
		
		if (rc == -1) {
			close(fd);
			
			SocketError e;
			e.set_message(std::string("connect: ") + std::string(strerror(errno)));
			e.set_url(this_url);
			
			throw(e);
		}
		
		char buffer[3096];
		
		if (uri.get_scheme() == "https") {
			br_ssl_client_context sc;
			br_x509_minimal_context xc;
			unsigned char iobuf[BR_SSL_BUFSIZE_BIDI];
			br_sslio_context ioc;
			
			br_ssl_client_init_full(&sc, &xc, TAs, TAs_NUM);
			br_ssl_engine_set_buffer(&sc.eng, iobuf, sizeof(iobuf), 1);
			br_ssl_client_reset(&sc, (uri.get_host()).c_str(), 0);
			br_sslio_init(&ioc, &sc.eng, sock_read, &fd, sock_write, &fd);
			
			br_sslio_write_all(&ioc, data.c_str(), data.length());
			br_sslio_flush(&ioc);
			
			br_sslio_read(&ioc, buffer, sizeof(buffer));
			
			close(fd);
			
			if (br_ssl_engine_current_state(&sc.eng) == BR_SSL_CLOSED) {
				const int err = br_ssl_engine_last_error(&sc.eng);
				
				if (err != 0) {
					SSLError e;
					e.set_message("br_ssl_engine_last_error: " + std::to_string(err));
					e.set_url(this_url);
					
					throw(e);
				}
			}
		} else {
			int length;
			
			length = send(fd, data.c_str(), data.length(), NULL);
			
			if (length == -1) {
				close(fd);
				
				SocketError e;
				e.set_message(std::string("send: ") + std::string(strerror(errno)));
				e.set_url(this_url);
				
				throw(e);
			}
			
			length = recv(fd, buffer, sizeof(buffer), NULL);
			
			if (length == -1) {
				close(fd);
				
				SocketError e;
				e.set_message(std::string("recv: ") + std::string(strerror(errno)));
				e.set_url(this_url);
				
				throw(e);
			}
			
			close(fd);
		}
		
		const std::string raw_response = buffer;
		
		const size_t index = raw_response.find("\r\n\r\n");
		const std::string raw_headers = raw_response.substr(0, index);
		const std::string body = raw_response.substr(index + 4, raw_response.length());
		
		Response response = Response();
		
		response.set_body(body);
		
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
		
		response.set_http_version(http_version);
		
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
		
		response.set_status_message(status_message);
		response.set_status_code(status_code);
		
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
		
		response.set_headers(headers);
		
		std::string location;
		
		if (response.get_status_code() >= 300 && response.get_status_code() <= 399 && response.has_header("Location")) {
			location = response.get_header("Location");
		} else if (response.get_status_code() >= 200 && response.get_status_code() <= 299 && response.has_header("Content-Location")) {
			location = response.get_header("Content-Location");
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
