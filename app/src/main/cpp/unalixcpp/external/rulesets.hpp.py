import json
import os
import http.client
import urllib.parse

source = """\
#include <string>
#include <vector>

/*
Automatically generated rules. Use 'rulesets.hpp.py' to modify/regenerate.
*/

struct Ruleset {{
	private:
		const std::string urlPattern;
		const bool completeProvider;
		const std::vector<std::string> rules;
		const std::vector<std::string> rawRules;
		const std::vector<std::string> referralMarketing;
		const std::vector<std::string> exceptions;
		const std::vector<std::string> redirections;
	
	public:
		Ruleset(
			const std::string urlPattern,
			const bool completeProvider,
			const std::vector<std::string> rules,
			const std::vector<std::string> rawRules,
			const std::vector<std::string> referralMarketing,
			const std::vector<std::string> exceptions,
			const std::vector<std::string> redirections
		) :
			urlPattern(urlPattern),
			completeProvider(completeProvider),
			rules(rules),
			rawRules(rawRules),
			referralMarketing(referralMarketing),
			exceptions(exceptions),
			redirections(redirections)
		{{}}
		
		const std::string get_url_pattern() const {{
			return this -> urlPattern;
		}}
		
		const bool get_complete_provider() const {{
			return this -> completeProvider;
		}}
		
		const std::vector<std::string> get_rules() const {{
			return this -> rules;
		}}
		
		const std::vector<std::string> get_raw_rules() const {{
			return this -> rawRules;
		}}
		
		const std::vector<std::string> get_referral_marketing() const {{
			return this -> referralMarketing;
		}}
		
		const std::vector<std::string> get_exceptions() const {{
			return this -> exceptions;
		}}
		
		const std::vector<std::string> get_redirections() const {{
			return this -> redirections;
		}}
}};

static const std::vector<Ruleset> rulesets = {{
	{}
}};
"""

urls = (
	"https://rules1.clearurls.xyz/data/data.minify.json",
	"https://raw.githubusercontent.com/AmanoTeam/Unalix/master/unalix/package_data/rulesets/unalix.json"
)

ignored_providers = (
    "ClearURLsTest",
    "ClearURLsTestBlock",
    "ClearURLsTest2",
    "ClearURLsTestBlock2"
)

rulesets = []

for raw_url in urls:
	print(f"Fetching data from {raw_url}...")

	url = urllib.parse.urlparse(url = raw_url)

	connection = http.client.HTTPSConnection(
		host = url.netloc,
		port = url.port
	)

	connection.request(
		method = "GET",
		url = url.path
	)
	response = connection.getresponse()
	content = response.read()
	
	connection.close()

	rules = json.loads(s = content)
	
	for providerName in rules["providers"].keys():
		if not rules["providers"][providerName]["urlPattern"]:
			continue

		if providerName in ignored_providers:
			continue

		rulesets.append({
			"urlPattern": rules["providers"][providerName]["urlPattern"],
			"completeProvider": rules["providers"][providerName].get("completeProvider", False),
			"rules": rules["providers"][providerName].get("rules", []),
			"rawRules": rules["providers"][providerName].get("rawRules", []),
			"referralMarketing": rules["providers"][providerName].get("referralMarketing", []),
			"exceptions": rules["providers"][providerName].get("exceptions", []),
			"redirections": rules["providers"][providerName].get("redirections", [])
		})

rulesets = (
	(
		json.dumps(rulesets, indent=4, ensure_ascii=False).replace("""        "urlPattern": """, "        ")
			.replace(""",\n        "completeProvider": """, ",\n        ")
			.replace("""        "rules": [\n            """, "        {\n            ")
			.replace("""        "rules": []""", "        {}")
			.replace("""        "rawRules": [\n            """, "        {\n            ")
			.replace("""        "rawRules": []""", "        {}")
			.replace("""        "referralMarketing": [\n            """, "        {\n            ")
			.replace("""        "referralMarketing": []""", "        {}")
			.replace("""        "exceptions": [\n            """, "        {\n            ")
			.replace("""        "exceptions": []""", "        {}")
			.replace("""        "redirections": [\n            """, "        {\n            ")
			.replace("""        "redirections": []""", "        {}")
			.replace("    },\n    {", "    ),\n    Ruleset(")
			.replace("\n        ],", "\n        },")
			.replace("[\n    {", "\n    Ruleset(")
			.replace("    }\n]", "    )\n")
			.replace("]\n    ),", "}\n    ),")
			.replace("\n        }\n    ),", "\n        }\n    ),")
	)
).strip()

file = os.path.realpath("../src/rulesets.hpp")

print("Exporting to %s" % file)

with open(file = file, mode = "w") as file:
	file.write(source.format(rulesets))
