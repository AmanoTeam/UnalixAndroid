enum QType {
	A = 1,
	AAAA = 28
};

enum DNSSpecification {
	DNS_SPEC_DOH,
	DNS_SPEC_DOT,
	DNS_SPEC_WIREFORMAT_UDP,
	DNS_SPEC_WIREFORMAT_TCP,
};

const int DEFAULT_DOH_PORT = 443;
const int DEFAULT_DOT_PORT = 853;
const int DEFAULT_WIREFORMAT_PORT = 53;

const std::string dns_query(
	const std::string name,
	const QType qtype,
	const int timeout,
	const std::string server
);

const std::string get_address(
	const std::string hostname,
	int family
);