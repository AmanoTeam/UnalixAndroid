package com.amanoteam.unalix.fragments;

import androidx.work.WorkRequest;
import androidx.work.WorkManager;
import androidx.work.OneTimeWorkRequest;
import android.content.ContentValues;
import android.content.Intent;
import com.amanoteam.unalix.services.RulesetsUpdateService;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.amanoteam.unalix.core.Ruleset;
import com.amanoteam.unalix.utilities.RulesetsUtils;

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

final class RulesetViewHolder extends ViewHolder {

	public final MaterialTextView name;
	public final MaterialTextView url;
	public final MaterialTextView lastUpdate;

	public final SwitchMaterial switchm;
	public final MaterialButton overflowMenu;

	public RulesetViewHolder(final View view) {
		super(view);

		name = view.findViewById(R.id.ruleset_name);
		url = view.findViewById(R.id.ruleset_url);
		lastUpdate = view.findViewById(R.id.ruleset_last_update);

		for (final MaterialTextView item : new MaterialTextView[] {name, url, lastUpdate}) {
			item.setOnLongClickListener((final View itemView) -> {
				PackageUtils.copyToClipboard(view.getContext(), item.getText().toString());
				PackageUtils.showSnackbar(view, "Copied to clipboard");
				
				return true;
			});
		}
		
		switchm = view.findViewById(R.id.ruleset_switch);
		overflowMenu = view.findViewById(R.id.ruleset_overflow_menu);
	}
}

public class RulesetsFragment extends Fragment {

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		final RulesetsFragmentBinding binding = RulesetsFragmentBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(final View fragmentView, final Bundle savedInstanceState) {
		final FragmentActivity activity = getActivity();
		final Context context = activity.getApplicationContext();
		
		final LayoutInflater layoutInflater = activity.getLayoutInflater();
		final androidx.coordinatorlayout.widget.CoordinatorLayout addRulesetDialog = (androidx.coordinatorlayout.widget.CoordinatorLayout) layoutInflater.inflate(R.layout.add_ruleset, null);
		
		final TextInputEditText nameInput = addRulesetDialog.findViewById(R.id.ruleset_name_input);
		final TextInputEditText urlInput = addRulesetDialog.findViewById(R.id.ruleset_url_input);
		final TextInputEditText hashUrlInput = addRulesetDialog.findViewById(R.id.hash_url_input);
		final CheckBox checkbox = addRulesetDialog.findViewById(R.id.ruleset_integrity_checkbox);
		
		checkbox.setOnClickListener((final View view) -> {
			hashUrlInput.setError(null);
			hashUrlInput.setEnabled(checkbox.isChecked());
		});
		
		final AlertDialog alertDialog = new MaterialAlertDialogBuilder(activity)
			.setView(addRulesetDialog)
			.setNegativeButton("Cancel", null)
			.setPositiveButton("Save", null)
			.create();
		
		alertDialog.setOnDismissListener((final DialogInterface dialogInterface) -> {
			for (final TextInputEditText input : new TextInputEditText[] {nameInput, urlInput, hashUrlInput}) {
				input.setError(null);
				input.getText().clear();
			}
			
			hashUrlInput.setEnabled(false);
			checkbox.setChecked(false);
		});
		
		final class RulesetAdapter extends Adapter<RulesetViewHolder> {
			private final List<Ruleset> rulesets;
			
			public RulesetAdapter(final ArrayList<Ruleset> rulesets) {
				this.rulesets = rulesets;
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
				viewHolder.lastUpdate.setText((lastUpdate == 0) ? "never updated": String.valueOf(lastUpdate));
		
				viewHolder.switchm.setChecked(isEnabled);
		
				viewHolder.switchm.setOnClickListener((final View checkboxView) -> {
					final SQLiteDatabase database = new RulesetsDatabaseHelper(context)
						.getWritableDatabase();
		
					final ContentValues values = new ContentValues();
		
					values.put(RulesetEntry.COLUMN_RULESET_IS_ENABLED, viewHolder.switchm.isChecked() ? 1 : 0);
		
					final String whereClause = String.format("%s = ?", RulesetEntry.COLUMN_RULESET_URL);
					final String[] whereArgs = {
						url
					};
		
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
								final String oldName = item.getName();
								nameInput.setText(oldName);
								nameInput.setSelection(oldName.length());
								
								final String oldUrl = item.getUrl();
								urlInput.setText(oldUrl);
								urlInput.setSelection(oldUrl.length());
		
								final String oldHashUrl = item.getHashUrl();
								
								if (oldHashUrl != null) {
									hashUrlInput.setText(oldHashUrl);
									hashUrlInput.setSelection(oldHashUrl.length());
		
									hashUrlInput.setEnabled(true);
									checkbox.setChecked(true);
								}
		
								alertDialog.setOnShowListener((final DialogInterface dialogInterface) -> {
		
									final AppCompatButton button = (AppCompatButton) alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
									
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
								
								alertDialog.setTitle("Edit ruleset");
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
				return (rulesets != null ? rulesets.size() : 0);
			}
		
			public void addRuleset(final Ruleset ruleset, final boolean saveOnDatabase) {
				if (saveOnDatabase) {
					// Add ruleset to SQLite database
					final SQLiteDatabase database = new RulesetsDatabaseHelper(context)
						.getWritableDatabase();
		
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
				final SQLiteDatabase database = new RulesetsDatabaseHelper(context)
					.getWritableDatabase();
		
				final Ruleset ruleset = rulesets.get(position);
				final String url = ruleset.getUrl();
		
				final String whereClause = String.format("%s = ?", RulesetEntry.COLUMN_RULESET_URL);
				final String[] whereArgs = {
					url
				};
		
				database.delete(
					RulesetEntry.TABLE_NAME,
					whereClause,
					whereArgs
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
				final SQLiteDatabase database = new RulesetsDatabaseHelper(context)
					.getWritableDatabase();
		
				final ContentValues values = new ContentValues();
		
				values.put(RulesetEntry.COLUMN_RULESET_NAME, newName);
				values.put(RulesetEntry.COLUMN_RULESET_URL, newUrl);
		
				if (newHashUrl == null) {
					values.putNull(RulesetEntry.COLUMN_RULESET_HASH_URL);
				} else {
					values.put(RulesetEntry.COLUMN_RULESET_HASH_URL, newHashUrl);
				}
		
				final String whereClause = String.format("%s = ?", RulesetEntry.COLUMN_RULESET_URL);
				final String[] whereArgs = {
					oldUrl
				};
		
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
		
		final RulesetAdapter rulesetAdapter = new RulesetAdapter(new ArrayList<>());
		
		final RecyclerView recyclerView = fragmentView.findViewById(R.id.rulesets_list);
		recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
		recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.HORIZONTAL));
		recyclerView.setAdapter(rulesetAdapter);
		
		final SwipeRefreshLayout swipeRefresh = fragmentView.findViewById(R.id.swipe_to_refresh);
		
		swipeRefresh.setOnRefreshListener(() -> {
			
			WorkRequest uploadWorkRequest =
				new OneTimeWorkRequest.Builder(RulesetsUpdateService.class)
					.build();
			WorkManager
				.getInstance(context)
				.enqueue(uploadWorkRequest);

			
			swipeRefresh.setRefreshing(false);
		});
		
		final MenuProvider menuProvider = new MenuProvider() {
			@Override
			public void onCreateMenu(final Menu menu, final MenuInflater menuInflater) {
				menuInflater.inflate(R.menu.rulesets_appbar_menu, menu);
			}
			
			@Override
			public boolean onMenuItemSelected(final MenuItem menuItem) {
				
				switch (menuItem.getItemId()) {
					case R.id.rulesets_add_action: {
						IntentChooserDialogFragment d = IntentChooserDialogFragment.newInstance();
						//IntentChooserDialogFragment d = new IntentChooserDialogFragment();
						d.show(getActivity().getSupportFragmentManager(), IntentChooserDialogFragment.TAG);
						
						if (1 == 1) {
							return true;
						}
						alertDialog.setOnShowListener(dialogInterface -> {
							final AppCompatButton button = (AppCompatButton) alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
							
							button.setOnClickListener((final View buttonView) -> {
								final Ruleset ruleset = InputUtils.getRulesetFromInputs(nameInput, urlInput, hashUrlInput, checkbox);
			
								if (ruleset == null) {
									return;
								}
			
								if (RulesetsUtils.isAlreadyInDatabase(ruleset, context)) {
									urlInput.requestFocus();
			
									urlInput.setError(null);
									urlInput.setError("This entry already exists in database");
			
									return;
								}
			
								alertDialog.dismiss();
			
								rulesetAdapter.addRuleset(ruleset, true);
							});
						});
						
						alertDialog.setTitle("Add ruleset");
						alertDialog.show();
						
						return true;
					}
					case R.id.rulesets_update_action:
						final Intent service = new Intent(activity, RulesetsUpdateService.class);
						activity.startService(service);
						return true;
					default:
						return false;
				}
			}
		};
		
		activity.addMenuProvider(menuProvider, getViewLifecycleOwner());
		
		if (RulesetsUtils.isColumnEmpty(context)) {
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
			final ArrayList<Ruleset> rulesets = RulesetsUtils.getRulesetsFromDatabase(context);

			for (final Ruleset ruleset : rulesets) {
				rulesetAdapter.addRuleset(ruleset, false);
			}
		}
	}
	
}
