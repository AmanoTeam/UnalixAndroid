#include <string>
#include <algorithm>

#include "uri.hpp"

const std::string URI::to_string() {
	return (
		this -> scheme +
		"://" +
		this -> host +
		((this -> port == 0) ? "" : ":"  + std::to_string(this -> port)) +
		path +
		query +
		fragment
	);
}

URI URI::from_string(const std::string &str) {
	
	std::string scheme;
	std::string host;
	int port = 0;
	std::string path;
	std::string query;
	std::string fragment;
	
	std::string::const_iterator str_end = str.end();

	std::string::const_iterator scheme_start = str.begin();
	std::string::const_iterator scheme_end = std::find(scheme_start, str_end, ':');			//"://");

	if (scheme_end != str_end) {
		const std::string scheme_str = &*(scheme_end);
		
		if ((scheme_str.length() > 3) && (scheme_str.substr(0, 3) == "://")) {
			scheme = std::string(scheme_start, scheme_end);
			scheme_end += 3;
		} else {
			scheme_end = str.begin();
		}
	} else {
		scheme_end = str.begin();
	}
	
	std::string::const_iterator host_start = scheme_end;
	std::string::const_iterator path_start = std::find(host_start, str_end, '/');
	std::string::const_iterator query_start = std::find(str.begin(), str.end(), '?');
	std::string::const_iterator fragment_start = std::find(str.begin(), str.end(), '#');
	std::string::const_iterator host_end = std::find(scheme_end, (path_start != str_end) ? path_start : query_start, ':');

	host = std::string(host_start, host_end);

	if ((host_end != str_end) && ((&*(host_end))[0] == ':')) {
		host_end++;
		std::string::const_iterator port_end = (path_start != str_end) ? path_start : query_start;
		port = std::stoi(std::string(host_end, port_end));
	}

	if (path_start != str_end) {
		path = std::string(path_start, query_start);
	}

	if (query_start != str_end) {
		query = std::string(query_start, fragment_start);
	}
	
	if (fragment_start != str_end) {
		fragment = std::string(fragment_start, str.end());
	}
	
	URI uri = URI(
		scheme,
		host,
		port,
		path,
		query,
		fragment
	);
	
	return uri;

}
