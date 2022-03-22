#include <string>

#include "clear_url.hpp"
#include "unshort_url.hpp"
#include "http.hpp"
#include "exceptions.hpp"

const std::string unshort_url(
	const std::string url,
	const bool ignore_referral_marketing,
	const bool ignore_rules,
	const bool ignore_exceptions,
	const bool ignore_raw_rules,
	const bool ignore_redirections,
	const bool skip_blocked,
	const int timeout,
	const int max_redirects
) {
	
	if (!(url.starts_with("http") || url.starts_with("https"))) {
		UnsupportedProtocolError e;
		e.set_message("Unrecognized URI or unsupported protocol");
		e.set_url(url);
		
		throw(e);
	}
	
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
	
	while (true) {
		const Response response = send_request(this_url, timeout);
		
		if (response.is_redirect()) {
			const std::string location = response.get_location();
			
			// Avoid redirect loops
			if (location == this_url) {
				return this_url;
			}
			
			total_redirects++;
			
			if (total_redirects > max_redirects) {
				TooManyRedirectsError e;
				e.set_message("Exceeded maximum allowed redirects");
				e.set_url(location);
				
				throw(e);
			}
			
			this_url = clear_url(
				location,
				ignore_referral_marketing,
				ignore_rules,
				ignore_exceptions,
				ignore_raw_rules,
				ignore_redirections,
				skip_blocked
			);
			
			continue;
		}
		
		return this_url;
	}
}
