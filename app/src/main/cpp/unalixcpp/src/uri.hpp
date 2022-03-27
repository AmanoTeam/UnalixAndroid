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
		
		// Getters
		const std::string get_scheme() const;
		
		const std::string get_host() const;
		
		const std::string get_ipv6_host() const;
		
		const int get_port() const;
		
		const std::string get_path() const;
		
		const std::string get_query() const;
		
		const std::string get_fragment() const;
		
		const bool is_ipv4() const;
		
		const bool is_ipv6() const;
		
		static URI from_string(const std::string &str);
		
		const std::string to_string() const;
		
		// Setters
		void set_scheme(const std::string value);
		
		void set_host(const std::string value);
		
		void set_port(const int value);
		
		void set_path(const std::string value);
		
		void set_query(const std::string value);
		
		void set_fragment(const std::string value);
};
