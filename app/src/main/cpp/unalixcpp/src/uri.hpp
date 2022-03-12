#include <string>

struct URI {
	private:
		std::string scheme;
		std::string host;
		int port;
		std::string path;
		std::string query;
		std::string fragment;
	public:
		URI(
			const std::string scheme,
			const std::string host,
			const int port,
			const std::string path,
			const std::string query,
			const std::string fragment
		) :
			scheme(scheme),
			host(host),
			port(port),
			path(path),
			query(query),
			fragment(fragment)
		{}
		
		const std::string get_scheme() const {
			return this -> scheme;
		}
		
		void set_scheme(const std::string value) {
			this -> scheme = value;
		}
		
		const std::string get_host() const {
			return this -> host;
		}
		
		void set_host(const std::string value) {
			this -> host = value;
		}
		
		const int get_port() const {
			return this -> port;
		}
		
		void set_port(const int value) {
			this -> port = value;
		}
		
		const std::string get_path() const {
			return this -> path;
		}
		
		void set_path(const std::string value) {
			this -> path = value;
		}
		
		const std::string get_query() const {
			return this -> query;
		}
		
		void set_query(const std::string value) {
			this -> query = value;
		}
		
		const std::string get_fragment() const {
			return this -> fragment;
		}
		
		void set_fragment(const std::string value) {
			this -> fragment = value;
		}
		
		static URI from_string(const std::string &str);
		
		const std::string to_string();
		
};
