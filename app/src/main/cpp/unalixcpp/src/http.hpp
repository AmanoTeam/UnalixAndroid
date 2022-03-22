#include <string>
#include <vector>

struct Response {
	private:
		float http_version;
		int status_code;
		std::string status_message;
		std::vector<std::tuple<std::string, std::string>> headers;
		std::string body;
		std::string location;
	
	public:
		// Setters
		void set_http_version(const float value);
		
		void set_status_code(const int value);
		
		void set_status_message(const std::string value);
		
		void set_headers(const std::vector<std::tuple<std::string, std::string>> value);
		
		void set_body(const std::string value);
		
		void set_location(const std::string value);
		
		// Getters
		const float get_http_version() const;
		
		const int get_status_code() const;
		
		const std::string get_location() const;
		
		const std::string get_status_message() const;
		
		const std::vector<std::tuple<std::string, std::string>> get_headers() const;
		
		const std::string get_body() const;
		
		const std::string get_header(const std::string name) const;
		
		const bool has_header(const std::string name) const;
		
		const bool is_redirect() const;
};

const Response send_request(
	const std::string url,
	const int timeout = 5
);

const std::string request_headers[][2] = {
	{"Accept", "*/*"},
	{"Accept-Encoding", "identity"},
	{"Connection", "close"},
	{"User-Agent", "UnalixAndroid/0.8 (+https://github.com/AmanoTeam/UnalixAndroid)"}
};
