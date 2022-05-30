package com.amanoteam.unalix.databases;

import android.database.sqlite.SQLiteDatabase;
import android.content.Context;
import android.provider.BaseColumns;
import android.database.sqlite.SQLiteOpenHelper;

import com.amanoteam.unalix.databases.RulesetContract.RulesetEntry;

public class RulesetsDatabaseHelper extends SQLiteOpenHelper {
	
	private static final String SQL_CREATE_ENTRIES = 
		"CREATE TABLE " + RulesetEntry.TABLE_NAME + " (" +
		RulesetEntry._ID + " INTEGER PRIMARY KEY," +
		RulesetEntry.COLUMN_RULESET_NAME + " TEXT," +
		RulesetEntry.COLUMN_RULESET_URL + " TEXT," +
		RulesetEntry.COLUMN_RULESET_HASH_URL + " TEXT," +
		RulesetEntry.COLUMN_RULESET_FILENAME + " TEXT," +
		RulesetEntry.COLUMN_RULESET_LAST_UPDATED + " INTEGER," +
		RulesetEntry.COLUMN_RULESET_IS_ENABLED + " INTEGER)";
	
	private static final String SQL_DELETE_ENTRIES =
		"DROP TABLE IF EXISTS " + RulesetEntry.TABLE_NAME;
	
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "rulesets.db";
	
	public RulesetsDatabaseHelper(final Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(final SQLiteDatabase database) {
		database.execSQL(SQL_CREATE_ENTRIES);
	}
	
	@Override
	public void onUpgrade(final SQLiteDatabase database, final int oldVersion, final int newVersion) {
		database.execSQL(SQL_DELETE_ENTRIES);
		onCreate(database);
	}
	
	@Override
	public void onDowngrade(final SQLiteDatabase database, final int oldVersion, final int newVersion) {
		onUpgrade(database, oldVersion, newVersion);
	}
	
}