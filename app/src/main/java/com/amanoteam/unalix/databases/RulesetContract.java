package com.amanoteam.unalix.databases;

import android.provider.BaseColumns;

public final class RulesetContract {
	// To prevent someone from accidentally instantiating the contract class,
	// make the constructor private.
	private RulesetContract() {
	}

	/* Inner class that defines the table contents */
	public static class RulesetEntry implements BaseColumns {
		public static final String TABLE_NAME = "rulesets";
		public static final String COLUMN_RULESET_NAME = "name";
		public static final String COLUMN_RULESET_URL = "url";
		public static final String COLUMN_RULESET_HASH_URL = "hash_url";
		public static final String COLUMN_RULESET_FILENAME = "file_name";
		public static final String COLUMN_RULESET_LAST_UPDATED = "last_updated";
		public static final String COLUMN_RULESET_IS_ENABLED = "is_enabled";
	}
}
