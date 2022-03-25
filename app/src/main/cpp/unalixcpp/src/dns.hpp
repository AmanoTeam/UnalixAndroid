enum QType {
	A = 1,
	AAAA = 28
};

const int DOH_PORT = 443;
const int DOT_PORT = 853;
const int PLAIN_WIREFORMAT_PORT = 53;

const std::string dns_query(
	const std::string name,
	const QType qtype,
	const int timeout,
	const std::string server
);