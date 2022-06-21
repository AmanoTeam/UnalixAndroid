package com.amanoteam.unalix.fragments;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class Ruleset {

	private String name;
	private String url;
	private String hashUrl;
	private String filename;
	private long lastUpdated;
	private boolean isEnabled;

	public Ruleset(
			final String name,
			final String url,
			final String hashUrl,
			final String filename,
			final long lastUpdated,
			final boolean isEnabled
	) {
		this.name = name;
		this.url = url;
		this.hashUrl = hashUrl;
		this.filename = filename;
		this.lastUpdated = lastUpdated;
		this.isEnabled = isEnabled;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(final String url) {
		this.url = url;
	}

	public String getHashUrl() {
		return hashUrl;
	}

	public void setHashUrl(final String hashUrl) {
		this.hashUrl = hashUrl;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(final String filename) {
		this.filename = filename;
	}

	public long getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdate(final long lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public void setEnabled(final boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	public boolean equals(final Ruleset other) {
		final String thisName = getName();
		final String otherName = other.getName();

		final String thisUrl = getUrl();
		final String otherUrl = other.getUrl();

		final String thisHashUrl = getHashUrl();
		final String otherHashUrl = other.getHashUrl();

		if (thisName.equals(otherName) && thisUrl.equals(otherUrl)) {
			return Objects.equals(thisHashUrl, otherHashUrl);
		} else {
			return false;
		}

	}

}

class InputUtils {

	public static Ruleset getRulesetFromInputs(final TextInputEditText nameInput, final TextInputEditText urlInput, final TextInputEditText hashUrlInput, final CheckBox checkbox) {
		final String name = PackageUtils.getName(nameInput);

		if (name == null) {
			return null;
		}

		final String url = PackageUtils.getURL(urlInput);

		if (url == null) {
			return null;
		}

		final Ruleset ruleset = new Ruleset(name, url, null, null, 0, true);

		if (checkbox.isChecked()) {
			final String hashUrl = PackageUtils.getURL(hashUrlInput);

			if (hashUrl == null) {
				return null;
			}

			ruleset.setHashUrl(hashUrl);
		}

		return ruleset;
	}

}

class RulesetViewHolder extends ViewHolder {

	public MaterialTextView name;
	public MaterialTextView url;
	public MaterialTextView lastUpdate;

	public CheckBox checkbox;
	public MaterialTextView overflowMenu;

	public RulesetViewHolder(final View view) {
		super(view);

		name = view.findViewById(R.id.ruleset_name);
		url = view.findViewById(R.id.ruleset_url);
		lastUpdate = view.findViewById(R.id.ruleset_last_update);

		checkbox = view.findViewById(R.id.ruleset_checkbox);
		overflowMenu = view.findViewById(R.id.ruleset_overflow_menu);
	}
}

class RulesetAdapter extends Adapter<RulesetViewHolder> {

	private final List<Ruleset> rulesets;
	private final FragmentActivity activity;

	public RulesetAdapter(final ArrayList<Ruleset> rulesets, final FragmentActivity activity) {
		this.rulesets = rulesets;
		this.activity = activity;
	}

	@Override
	public RulesetViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
		return new RulesetViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.rulesets_list_item, parent, false));
	}

	@Override
	public void onBindViewHolder(final RulesetViewHolder viewHolder, final int position) {
		final Ruleset item = rulesets.get(position);

		final String name = item.getName();
		final String url = item.getUrl();
		final boolean isEnabled = item.isEnabled();
		final long lastUpdate = item.getLastUpdated();

		viewHolder.name.setText(name);
		viewHolder.url.setText(url);
		viewHolder.lastUpdate.setText(String.valueOf(lastUpdate));

		viewHolder.checkbox.setChecked(isEnabled);

		viewHolder.checkbox.setOnClickListener((final View checkboxView) -> {
			final Context context = activity.getApplicationContext();

			final RulesetsDatabaseHelper databaseHelper = new RulesetsDatabaseHelper(context);
			final SQLiteDatabase database = databaseHelper.getWritableDatabase();

			final ContentValues values = new ContentValues();

			values.put(RulesetEntry.COLUMN_RULESET_IS_ENABLED, viewHolder.checkbox.isChecked() ? 1 : 0);

			final String whereClause = String.format("%s = ?", RulesetEntry.COLUMN_RULESET_URL);
			final String[] whereArgs = {url};

			database.update(
					RulesetEntry.TABLE_NAME,
					values,
					whereClause,
					whereArgs
			);

			database.close();
		});

		viewHolder.overflowMenu.setOnClickListener((final View itemView) -> {
			final Context context = itemView.getContext();

			final PopupMenu popupMenu = new PopupMenu(context, itemView);
			popupMenu.setForceShowIcon(true);
			popupMenu.inflate(R.menu.ruleset_menu);
			popupMenu.show();

			popupMenu.setOnMenuItemClickListener((final MenuItem menuItem) -> {
				switch (menuItem.getItemId()) {
					case R.id.ruleset_edit:
						final LayoutInflater layoutInflater = activity.getLayoutInflater();
						final View addRulesetDialog = layoutInflater.inflate(R.layout.add_ruleset, null);

						final String oldName = item.getName();

						final TextInputEditText nameInput = addRulesetDialog.findViewById(R.id.ruleset_name_input);
						nameInput.setText(oldName);
						nameInput.setSelection(oldName.length());

						final String oldUrl = item.getUrl();

						final TextInputEditText urlInput = addRulesetDialog.findViewById(R.id.ruleset_url_input);
						urlInput.setText(oldUrl);
						urlInput.setSelection(oldUrl.length());

						final String oldHashUrl = item.getHashUrl();

						final TextInputEditText hashUrlInput = addRulesetDialog.findViewById(R.id.hash_url_input);

						final CheckBox checkbox = addRulesetDialog.findViewById(R.id.ruleset_integrity_checkbox);

						checkbox.setOnClickListener((final View checkboxView) -> {
							hashUrlInput.setError(null);
							hashUrlInput.setEnabled(checkbox.isChecked());
						});

						if (oldHashUrl != null) {
							hashUrlInput.setText(oldHashUrl);
							hashUrlInput.setSelection(oldHashUrl.length());

							hashUrlInput.setEnabled(true);
							checkbox.setChecked(true);
						}

						final AlertDialog alertDialog = new MaterialAlertDialogBuilder(activity)
								.setView(addRulesetDialog)
								.setTitle("Edit ruleset")
								.setNegativeButton("Cancel", null)
								.setPositiveButton("Save", null)
								.create();

						alertDialog.setOnShowListener(dialogInterface -> {

							Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
							button.setOnClickListener(buttonView -> {
								final Ruleset ruleset = InputUtils.getRulesetFromInputs(nameInput, urlInput, hashUrlInput, checkbox);

								if (ruleset == null) {
									return;
								}

								if (item.equals(ruleset)) {
									nameInput.requestFocus();

									nameInput.setError(null);
									nameInput.setError("No changes have been made");

									return;
								}

								updateRuleset(position, ruleset);

								alertDialog.dismiss();
							});
						});

						alertDialog.show();

						return true;
					case R.id.ruleset_delete:
						removeRuleset(position);
						return true;
					default:
						return false;
				}
			});
		});
	}

	@Override
	public int getItemCount() {
		return ((rulesets != null) ? rulesets.size() : 0);
	}

	public void addRuleset(final Ruleset ruleset, final boolean saveOnDatabase) {
		if (saveOnDatabase) {
			// Add ruleset to SQLite database
			final Context context = activity.getApplicationContext();

			final RulesetsDatabaseHelper databaseHelper = new RulesetsDatabaseHelper(context);
			final SQLiteDatabase database = databaseHelper.getWritableDatabase();

			final String name = ruleset.getName();
			final String url = ruleset.getUrl();
			final String hashUrl = ruleset.getHashUrl();
			final String filename = ruleset.getFilename();
			final boolean isEnabled = ruleset.isEnabled();
			final long lastUpdated = ruleset.getLastUpdated();

			final ContentValues values = new ContentValues();

			values.put(RulesetEntry.COLUMN_RULESET_NAME, name);
			values.put(RulesetEntry.COLUMN_RULESET_URL, url);

			if (hashUrl == null) {
				values.putNull(RulesetEntry.COLUMN_RULESET_HASH_URL);
			} else {
				values.put(RulesetEntry.COLUMN_RULESET_HASH_URL, hashUrl);
			}

			if (filename == null) {
				values.putNull(RulesetEntry.COLUMN_RULESET_FILENAME);
			} else {
				values.put(RulesetEntry.COLUMN_RULESET_FILENAME, PackageUtils.generateFilename(context));
			}

			values.put(RulesetEntry.COLUMN_RULESET_IS_ENABLED, isEnabled ? 1 : 0);

			if (lastUpdated < 1) {
				values.putNull(RulesetEntry.COLUMN_RULESET_LAST_UPDATED);
			} else {
				values.put(RulesetEntry.COLUMN_RULESET_LAST_UPDATED, lastUpdated);
			}

			values.put(RulesetEntry.COLUMN_RULESET_IS_ENABLED, 1);

			database.insert(RulesetEntry.TABLE_NAME, null, values);
			database.close();
		}

		// Add ruleset to RecycleView
		rulesets.add(ruleset);
		notifyItemInserted(getItemCount());
	}

	public void removeRuleset(final int position) {
		// Remove ruleset from SQLite database
		final Context context = activity.getApplicationContext();

		final RulesetsDatabaseHelper databaseHelper = new RulesetsDatabaseHelper(context);
		final SQLiteDatabase database = databaseHelper.getWritableDatabase();

		final Ruleset ruleset = rulesets.get(position);
		final String url = ruleset.getUrl();

		database.delete(
				RulesetEntry.TABLE_NAME,
				String.format("%s = ?", RulesetEntry.COLUMN_RULESET_URL),
				new String[]{url}
		);

		database.close();

		// Remove ruleset from RecyclerView
		rulesets.remove(position);
		notifyItemRemoved(position);
		notifyItemRangeChanged(position, getItemCount());
	}

	private void updateRuleset(final int position, final Ruleset newRuleset) {
		final Ruleset oldRuleset = rulesets.get(position);
		final String oldUrl = oldRuleset.getUrl();

		final String newName = newRuleset.getName();
		final String newUrl = newRuleset.getUrl();
		final String newHashUrl = newRuleset.getHashUrl();

		// Update ruleset in SQLite database
		final Context context = activity.getApplicationContext();

		final RulesetsDatabaseHelper databaseHelper = new RulesetsDatabaseHelper(context);
		final SQLiteDatabase database = databaseHelper.getWritableDatabase();

		final ContentValues values = new ContentValues();

		values.put(RulesetEntry.COLUMN_RULESET_NAME, newName);
		values.put(RulesetEntry.COLUMN_RULESET_URL, newUrl);

		if (newHashUrl == null) {
			values.putNull(RulesetEntry.COLUMN_RULESET_HASH_URL);
		} else {
			values.put(RulesetEntry.COLUMN_RULESET_HASH_URL, newHashUrl);
		}

		final String whereClause = String.format("%s = ?", RulesetEntry.COLUMN_RULESET_URL);
		final String[] whereArgs = {oldUrl};

		database.update(
				RulesetEntry.TABLE_NAME,
				values,
				whereClause,
				whereArgs
		);

		database.close();

		// Update ruleset in RecycleView
		oldRuleset.setName(newName);
		oldRuleset.setUrl(newUrl);
		oldRuleset.setHashUrl(newHashUrl);

		notifyItemChanged(position);
	}
}

public class RulesetsFragment extends Fragment {

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		RulesetsFragmentBinding binding = RulesetsFragmentBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(final View root, final Bundle savedInstanceState) {
		final FragmentActivity activity = getActivity();
		final Context context = activity.getApplicationContext();

		final RecyclerView recyclerView = root.findViewById(R.id.rulesets_list);
		recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
		recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.HORIZONTAL));

		final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
		recyclerView.setLayoutManager(linearLayoutManager);

		final RulesetAdapter rulesetAdapter = new RulesetAdapter(new ArrayList<>(), activity);
		recyclerView.setAdapter(rulesetAdapter);

		if (isColumnEmpty()) {
			final Ruleset[] rulesets = {
					new Ruleset(
							"ClearURLs",
							"https://rules2.clearurls.xyz/data.minify.json",
							"https://rules2.clearurls.xyz/rules.minify.hash",
							null,
							0,
							true
					),
					new Ruleset(
							"Unalix",
							"https://unalixr.amanoteam.com/unalix.json",
							"https://unalixr.amanoteam.com/unalix.json.sha256",
							null,
							0,
							true
					)
			};

			for (final Ruleset ruleset : rulesets) {
				rulesetAdapter.addRuleset(ruleset, true);
			}
		} else {
			final ArrayList<Ruleset> rulesets = getRulesetsFromDatabase();

			for (final Ruleset ruleset : rulesets) {
				rulesetAdapter.addRuleset(ruleset, false);
			}
		}

		final FloatingActionButton addRulesetButton = activity.findViewById(R.id.add_ruleset_button);

		recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(final RecyclerView recyclerView, final int dx, final int dy) {
				if (dy > 0) {
					addRulesetButton.hide();
				} else if (dy < 0) {
					addRulesetButton.show();
				}
			}
		});

		// "Add ruleset" button listener
		addRulesetButton.setOnClickListener((final View view) -> {
			final LayoutInflater layoutInflater = activity.getLayoutInflater();
			final View addRulesetDialog = layoutInflater.inflate(R.layout.add_ruleset, null);

			final TextInputEditText nameInput = addRulesetDialog.findViewById(R.id.ruleset_name_input);
			final TextInputEditText urlInput = addRulesetDialog.findViewById(R.id.ruleset_url_input);
			final TextInputEditText hashUrlInput = addRulesetDialog.findViewById(R.id.hash_url_input);

			final CheckBox checkbox = addRulesetDialog.findViewById(R.id.ruleset_integrity_checkbox);

			checkbox.setOnClickListener((final View checkboxView) -> {
				hashUrlInput.setError(null);
				hashUrlInput.setEnabled(checkbox.isChecked());
			});

			final AlertDialog alertDialog = new MaterialAlertDialogBuilder(activity)
					.setView(addRulesetDialog)
					.setTitle("New ruleset")
					.setNegativeButton("Cancel", null)
					.setPositiveButton("Save", null)
					.create();

			alertDialog.setOnShowListener(dialogInterface -> {

				Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
				button.setOnClickListener(buttonView -> {
					final Ruleset ruleset = InputUtils.getRulesetFromInputs(nameInput, urlInput, hashUrlInput, checkbox);

					if (ruleset == null) {
						return;
					}

					if (isAlreadyInDatabase(ruleset)) {
						urlInput.requestFocus();

						urlInput.setError(null);
						urlInput.setError("This entry already exists in database");

						return;
					}

					alertDialog.dismiss();

					rulesetAdapter.addRuleset(ruleset, true);
				});
			});

			alertDialog.show();
		});
	}

	private boolean isColumnEmpty() {
		final Context context = getActivity().getApplicationContext();

		final RulesetsDatabaseHelper databaseHelper = new RulesetsDatabaseHelper(context);
		final SQLiteDatabase database = databaseHelper.getReadableDatabase();

		final String query = String.format("SELECT COUNT(*) FROM %s", RulesetEntry.TABLE_NAME);
		final Cursor cursor = database.rawQuery(query, null);

		cursor.moveToFirst();

		final boolean isEmpty = (cursor.getInt(0) < 1);

		cursor.close();
		database.close();

		return isEmpty;
	}

	private ArrayList<Ruleset> getRulesetsFromDatabase() {
		final Context context = getActivity().getApplicationContext();

		final RulesetsDatabaseHelper databaseHelper = new RulesetsDatabaseHelper(context);
		final SQLiteDatabase database = databaseHelper.getReadableDatabase();

		final ArrayList<Ruleset> rulesets = new ArrayList<>();

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

	private boolean isAlreadyInDatabase(final Ruleset ruleset) {
		final Context context = getActivity().getApplicationContext();

		final RulesetsDatabaseHelper databaseHelper = new RulesetsDatabaseHelper(context);
		final SQLiteDatabase database = databaseHelper.getReadableDatabase();

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
