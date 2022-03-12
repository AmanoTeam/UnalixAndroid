#include <fstream>
#include <vector>
#include <string>
#include <regex>

#include "uri.hpp"
#include "rulesets.hpp"
#include "utils.hpp"

const std::string RULE_PREFIX = "(%(?:26|23|3[Ff])|&|\\?)";
const std::string RULE_SUFFIX = "(?:(?:=|%3[Dd])[^&]*)";

const std::string clear_url(
	const std::string url,
	const bool ignore_referral_marketing = false,
	const bool ignore_rules = false,
	const bool ignore_exceptions = false,
	const bool ignore_raw_rules = false,
	const bool ignore_redirections  = false,
	const bool skip_blocked = false
) {
	
	std::string this_url = url;
	
	for (const Ruleset ruleset: rulesets) {
		if (skip_blocked && ruleset.get_complete_provider()) {
			continue;
		}
		
		const std::regex pattern = std::regex(ruleset.get_url_pattern());
		std::smatch groups;
		
		if (std::regex_search(this_url, groups, pattern)) {
			if (!ignore_exceptions) {
				bool exception_matched = false;
				
				for (const std::string exception : ruleset.get_exceptions()) {
					const std::regex pattern = std::regex(exception);
					std::smatch groups;
					
					if (std::regex_search(this_url, groups, pattern)) {
						exception_matched = true;
						break;
					}
				}
				
				if (exception_matched) {
					continue;
				}
			}
			
			if (!ignore_redirections) {
				for (const std::string redirection : ruleset.get_redirections()) {
					const std::regex pattern = std::regex(redirection);
					std::smatch groups;
					
					if (std::regex_search(this_url, groups, pattern)) {
						const std::string target_url = groups[1];
						const std::string redirection_result = requote_uri(urldecode(target_url));
						
						// Avoid empty URLs
						if (redirection_result == "") {
							continue;
						}
						
						// Avoid duplicate URLs
						if (redirection_result == this_url) {
							continue;
						}
						
						URI uri = URI::from_string(redirection_result);
						
						// Workaround for URLs without scheme (see https://github.com/ClearURLs/Addon/issues/71)
						if (uri.get_scheme() == "") {
							uri.set_scheme("http");
						}
						
						return clear_url(
							uri.to_string(),
							ignore_referral_marketing,
							ignore_rules,
							ignore_exceptions,
							ignore_raw_rules,
							ignore_redirections,
							skip_blocked
						);
						
					}
				}
			}

			URI uri = URI::from_string(this_url);

			if (uri.get_query() != "") {
				if (!ignore_rules) {
					for (const std::string rule : ruleset.get_rules()) {
						const std::regex pattern = std::regex(RULE_PREFIX + rule + RULE_SUFFIX);
						const std::string replacement = "$1";
						const std::string query = std::regex_replace(uri.get_query(), pattern, replacement);
						
						uri.set_query(query);
					}
				}
				
				if (!ignore_referral_marketing) {
					for (const std::string referral_marketing : ruleset.get_referral_marketing()) {
						const std::regex pattern = std::regex(RULE_PREFIX + referral_marketing + RULE_SUFFIX);
						const std::string replacement = "$1";
						const std::string query = std::regex_replace(uri.get_query(), pattern, replacement);
						
						uri.set_query(query);
					}
				}
			}
			
			// The fragment might contains tracking fields as well
			if (uri.get_fragment() != "") {
				if (!ignore_rules) {
					for (const std::string rule : ruleset.get_rules()) {
						const std::regex pattern = std::regex(rule);
						const std::string replacement = "$1";
						const std::string fragment = std::regex_replace(uri.get_fragment(), pattern, replacement);
						
						uri.set_fragment(fragment);
					}
				}
				
				if (!ignore_referral_marketing) {
					for (const std::string referral_marketing : ruleset.get_referral_marketing()) {
						const std::regex pattern = std::regex(referral_marketing);
						const std::string replacement = "$1";
						const std::string fragment = std::regex_replace(uri.get_fragment(), pattern, replacement);
						
						uri.set_fragment(fragment);
					}
				}
			}
			
			if (uri.get_path() != "") {
				for (const std::string raw_rule : ruleset.get_raw_rules()) {
					const std::regex pattern = std::regex(raw_rule);
					const std::string replacement = "$1";
					const std::string path = std::regex_replace(uri.get_path(), pattern, replacement);
					
					uri.set_path(path);
				}
			}
			
			if (uri.get_query() != "") {
				uri.set_query(strip_query(uri.get_query()));
			}
			
			if (uri.get_fragment() != "") {
				uri.set_fragment(strip_query(uri.get_fragment()));
			}
			
			this_url = uri.to_string();
		}
	}
	
	return this_url;
	
}
