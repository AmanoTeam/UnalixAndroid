#include <string>
#include <exception>

struct UnalixException : public std::exception {
	private:
		std::string message;
		std::string url;
	
	public:
		const void set_message(const std::string value) {
			this -> message = value;
		}
		
		const std::string get_message() const {
			return this -> message;
		}
		
		const void set_url(const std::string value) {
			this -> url = value;
		}
		
		const std::string get_url() const {
			return this -> url;
		}
		
		const char* what() const throw () {
			return message.c_str();
		}
};

struct GAIError : public UnalixException {};
struct SocketError : public UnalixException {};
struct ConnectError : public SocketError {};
struct SendError : public SocketError {};
struct RecvError : public SocketError {};
struct SSLError : public UnalixException {};
struct UnsupportedProtocolError : public UnalixException {};
struct RemoteProtocolError : public UnalixException {};
struct TooManyRedirectsError : public UnalixException {};
struct DNSError : public UnalixException {};
struct ValueError : public UnalixException {};
