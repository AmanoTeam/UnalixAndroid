package com.amanoteam.unalix.utilities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import androidx.appcompat.widget.AppCompatImageButton;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.material.button.MaterialButton;
import android.view.Menu;
import androidx.core.view.MenuProvider;
import com.google.android.material.switchmaterial.SwitchMaterial;
import androidx.lifecycle.Lifecycle;
import android.view.ViewParent;
import android.widget.CheckBox;
import androidx.appcompat.widget.AppCompatButton;
import android.content.DialogInterface;
import android.widget.ScrollView;
import com.amanoteam.unalix.fragments.IntentChooserDialogFragment;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.amanoteam.unalix.R;
import com.amanoteam.unalix.databases.RulesetContract.RulesetEntry;
import com.amanoteam.unalix.databases.RulesetsDatabaseHelper;
import com.amanoteam.unalix.databinding.RulesetsFragmentBinding;
import com.amanoteam.unalix.utilities.PackageUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.amanoteam.unalix.core.Ruleset;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final public class RulesetsUtils {
	
	public static boolean isColumnEmpty(final Context context) {
		final SQLiteDatabase database = new RulesetsDatabaseHelper(context)
			.getReadableDatabase();

		final String query = String.format("SELECT COUNT(*) FROM %s", RulesetEntry.TABLE_NAME);
		final Cursor cursor = database.rawQuery(query, null);

		cursor.moveToFirst();

		final boolean isEmpty = (cursor.getInt(0) < 1);

		cursor.close();
		database.close();

		return isEmpty;
	}

	public static ArrayList<Ruleset> getRulesetsFromDatabase(final Context context) {
		final SQLiteDatabase database = new RulesetsDatabaseHelper(context)
			.getReadableDatabase();

		final ArrayList<Ruleset> rulesets = new ArrayList<Ruleset>();

		final Cursor cursor = database.query(RulesetEntry.TABLE_NAME, null, null, null, null, null, null);

		while (cursor.moveToNext()) {
			final String name = cursor.getString(cursor.getColumnIndex(RulesetEntry.COLUMN_RULESET_NAME));
			final String url = cursor.getString(cursor.getColumnIndex(RulesetEntry.COLUMN_RULESET_URL));
			final String hashUrl = cursor.getString(cursor.getColumnIndex(RulesetEntry.COLUMN_RULESET_HASH_URL));
			final String filename = cursor.getString(cursor.getColumnIndex(RulesetEntry.COLUMN_RULESET_FILENAME));
			final boolean isEnabled = cursor.getLong(cursor.getColumnIndex(RulesetEntry.COLUMN_RULESET_IS_ENABLED)) == 1;

			final int index = cursor.getColumnIndex(RulesetEntry.COLUMN_RULESET_LAST_UPDATED);
			final long lastUpdated = cursor.isNull(index) ? 0 : cursor.getLong(index);

			final Ruleset ruleset = new Ruleset(
				name,
				url,
				hashUrl,
				filename,
				lastUpdated,
				isEnabled
			);

			rulesets.add(ruleset);
		}

		cursor.close();
		database.close();

		return rulesets;
	}

	public static boolean isAlreadyInDatabase(final Ruleset ruleset, final Context context) {
		final SQLiteDatabase database = new RulesetsDatabaseHelper(context)
			.getReadableDatabase();

		final String whereClause = String.format("%s = ?", RulesetEntry.COLUMN_RULESET_URL);
		final String[] whereArgs = {
			ruleset.getUrl()
		};

		final Cursor cursor = database.query(
			RulesetEntry.TABLE_NAME,
			null,
			whereClause,
			whereArgs,
			null,
			null,
			null
		);

		final boolean alreadyExists = (cursor.getCount() > 0);

		cursor.close();
		database.close();

		return alreadyExists;
	}
	
}