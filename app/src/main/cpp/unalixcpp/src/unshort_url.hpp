#include <string>

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
);