package com.vinsol.expensetracker;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vinsol.expensetracker.helpers.LocationHelper;
import com.vinsol.expensetracker.utils.DateHelper;

class SeparatedListAdapter extends BaseAdapter {

	public final Map<String, Adapter> sections = new LinkedHashMap<String, Adapter>();
	public final ArrayAdapter<String> headers;
	public final ArrayAdapter<String> footers;
	public final static int TYPE_SECTION_HEADER = 0;
	public final static int TYPE_SECTION_FOOTER = 0;
	private Context mContext;
	private List<HashMap<String, String>> mDatadateList;
	private LayoutInflater mInflater;
	private UnknownEntryDialog unknownEntryDialog;

	public SeparatedListAdapter(Context context) {
		mContext = context;
		headers = new ArrayAdapter<String>(context,R.layout.mainlist_header_view);
		footers = new ArrayAdapter<String>(context,R.layout.main_list_footerview);
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void addSection(String section, Adapter adapter,List<HashMap<String, String>> _mDataDateList) {
		
		mDatadateList = _mDataDateList;
		notifyDataSetChanged();
		this.headers.add(section);
		this.footers.add(section);
		this.sections.put(section, adapter);
	}

	public Object getItem(int position) {
		for (Object section : this.sections.keySet()) {
			Adapter adapter = sections.get(section);
			int size = adapter.getCount() + 2;

			// check if position inside this section
			if (position == 0) {
				return section;
			}
			if (position < size - 1)
				return adapter.getItem(position - 1);
			if (position < size)
				return section;

			// otherwise jump into next section
			position -= size;
		}
		return null;
	}

	public int getCount() {
		// total together all sections, plus one for each section header
		int total = 0;
		for (Adapter adapter : this.sections.values())
			total += adapter.getCount() + 2;
		return total;
	}

	public int getViewTypeCount() {
		// assume that headers count as one, then total all sections
		int total = 2;
		for (Adapter adapter : this.sections.values())
			total += adapter.getViewTypeCount();
		return total;
	}

	public int getItemViewType(int position) {
		int type = 1;
		for (Object section : this.sections.keySet()) {
			Adapter adapter = sections.get(section);
			int size = adapter.getCount() + 2;

			// check if position inside this section
			if (position == 0)
				return TYPE_SECTION_HEADER;
			if (position < size - 1) {
				return type + adapter.getItemViewType(position - 1);
			}
			if (position < size)
				return TYPE_SECTION_FOOTER;
			// otherwise jump into next section
			position -= size;
			type += adapter.getViewTypeCount();
		}
		return -1;
	}

	public boolean areAllItemsSelectable() {
		return false;
	}

	public boolean isEnabled(int position) {
		return (getItemViewType(position) != TYPE_SECTION_HEADER);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		int sectionnum = 0;
		ViewHolderHeader holderHeader;
		final ViewHolderBody holderBody;
		ViewHolderFooter holderFooter;

		for (Object section : this.sections.keySet()) {
			Adapter adapter = sections.get(section);
			int size = adapter.getCount() + 2;

			// check if position inside this section
			if (position == 0) {
				holderHeader = new ViewHolderHeader();
				convertView = mInflater.inflate(R.layout.mainlist_header_view,null);
				holderHeader.expenses_listing_list_date_view = (TextView) convertView.findViewById(R.id.expenses_listing_list_date_view);
				holderHeader.expenses_listing_list_amount_view = (TextView) convertView.findViewById(R.id.expenses_listing_list_amount_view);
				holderHeader.expenses_listing_list_date_view.setText(mDatadateList.get(sectionnum).get(DatabaseAdapter.KEY_DATE_TIME));
				holderHeader.expenses_listing_list_amount_view.setText(mDatadateList.get(sectionnum).get(DatabaseAdapter.KEY_AMOUNT));
				return convertView;
			}
			if (position < size - 1) {
				if (convertView == null || position != 0) {
					holderBody = new ViewHolderBody();
					convertView = mInflater.inflate(R.layout.expense_listing_inflated_row, null);
					holderBody.expense_listing_inflated_row_location_time = (TextView) convertView.findViewById(R.id.expense_listing_inflated_row_location_time);
					holderBody.expense_listing_inflated_row_tag = (TextView) convertView.findViewById(R.id.expense_listing_inflated_row_tag);
					holderBody.expense_listing_inflated_row_amount = (TextView) convertView.findViewById(R.id.expense_listing_inflated_row_amount);
					holderBody.expense_listing_inflated_row_imageview = (ImageView) convertView.findViewById(R.id.expense_listing_inflated_row_imageview);
					holderBody.expense_listing_inflated_row_favorite_icon = (ImageView) convertView.findViewById(R.id.expense_listing_inflated_row_favorite_icon);
					holderBody.expense_listing_inflated_row_listview = (RelativeLayout) convertView.findViewById(R.id.expense_listing_inflated_row_listview);
				} else {
					holderBody = (ViewHolderBody) convertView.getTag();
				}
				
				
				@SuppressWarnings("unchecked")
				List<String> mlist = (List<String>) adapter.getItem(position - 1);
				
				
				if (mlist.get(5).equals(mContext.getString(R.string.camera))) {
					
					if(!isEntryComplete((ArrayList<String>) mlist)){
						holderBody.expense_listing_inflated_row_listview.setBackgroundResource(R.drawable.listing_row_unfinished_states);
					}
					
					if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
						try {
							File mFile = new File("/sdcard/ExpenseTracker/"+ mlist.get(0) + "_thumbnail.jpg");
							if (mFile.canRead()) {
								Drawable drawable = Drawable.createFromPath(mFile.getPath());
								holderBody.expense_listing_inflated_row_imageview.setImageDrawable(drawable);
							} else {
								holderBody.expense_listing_inflated_row_imageview.setImageResource(R.drawable.no_image_thumbnail);
							}
						} catch (Exception e) {
							holderBody.expense_listing_inflated_row_imageview.setImageResource(R.drawable.no_image_thumbnail);
							e.printStackTrace();
						}
					} else {
						holderBody.expense_listing_inflated_row_imageview.setImageResource(R.drawable.no_image_thumbnail);
					}
				} else if (mlist.get(5).equals(mContext.getString(R.string.text))) {

					if(!isEntryComplete((ArrayList<String>) mlist)){
						holderBody.expense_listing_inflated_row_listview.setBackgroundResource(R.drawable.listing_row_unfinished_states);
					}
					if (!mlist.get(1).equals(mContext.getString(R.string.unfinished_textentry)) && !mlist.get(1).equals(mContext.getString(R.string.finished_textentry))) {
						holderBody.expense_listing_inflated_row_imageview.setImageResource(R.drawable.listing_text_entry_icon);
					} else {
						holderBody.expense_listing_inflated_row_imageview.setImageResource(R.drawable.text_list_icon_no_tag);
					}

				} else if (mlist.get(5).equals(mContext.getString(R.string.unknown))) {
					holderBody.expense_listing_inflated_row_imageview.setImageResource(R.drawable.listing_reminder_icon);
					holderBody.expense_listing_inflated_row_listview.setBackgroundResource(R.drawable.listing_row_unknown_states);
				} else if (mlist.get(5).equals(mContext.getString(R.string.voice))) {

					if(!isEntryComplete((ArrayList<String>) mlist)){
						holderBody.expense_listing_inflated_row_listview.setBackgroundResource(R.drawable.listing_row_unfinished_states);
					}
					File mFile = new File("/sdcard/ExpenseTracker/Audio/"+ mlist.get(0) + ".amr");
					if (mFile.canRead()) {
						holderBody.expense_listing_inflated_row_imageview.setImageResource(R.drawable.listing_voice_entry_icon);
					} else {
						holderBody.expense_listing_inflated_row_imageview.setImageResource(R.drawable.no_voice_file_thumbnail);
					}
				} 
				if (mlist.get(4) != null) {
					
					if(!mlist.get(4).equals("")){
						try{
							if(isCurrentWeek(mDatadateList.get(sectionnum).get(DatabaseAdapter.KEY_DATE_TIME))){
								holderBody.expense_listing_inflated_row_favorite_icon.setVisibility(View.VISIBLE);
							}
						}catch(Exception e){
							
						}
					}
				}
				
				holderBody.expense_listing_inflated_row_imageview.setFocusable(false);
				holderBody.expense_listing_inflated_row_imageview.setOnClickListener(new MyClickListener(mlist));
				holderBody.expense_listing_inflated_row_location_time.setText(mlist.get(3));
				holderBody.expense_listing_inflated_row_tag.setText(mlist.get(1));
				holderBody.expense_listing_inflated_row_amount.setText(mlist.get(2));
				if ((mlist.get(5).equals(mContext.getString(R.string.sublist_daywise))) || mlist.get(5).equals(mContext.getString(R.string.sublist_monthwise)) || mlist.get(5).equals(mContext.getString(R.string.sublist_yearwise))|| mlist.get(5).equals(mContext.getString(R.string.sublist_weekwise))) {
					holderBody.expense_listing_inflated_row_imageview.setVisibility(View.GONE);
					holderBody.expense_listing_inflated_row_location_time.setVisibility(View.GONE);
				}
				return convertView;
			}

			if (position < size) {
				if (convertView == null || position < size) {
					holderFooter = new ViewHolderFooter();
					convertView = mInflater.inflate(R.layout.main_list_footerview, null);
					holderFooter.expenses_listing_add_expenses_button = (Button) convertView.findViewById(R.id.expenses_listing_add_expenses_button);
					holderFooter.expense_listing_list_add_expenses = (LinearLayout) convertView.findViewById(R.id.expense_listing_list_add_expenses);
				} else {
					holderFooter = (ViewHolderFooter) convertView.getTag();
				}

				if (!isCurrentWeek(mDatadateList.get(sectionnum).get(DatabaseAdapter.KEY_DATE_TIME))) {
					holderFooter.expense_listing_list_add_expenses.setBackgroundResource(0);
					holderFooter.expense_listing_list_add_expenses.setVisibility(View.GONE);
					holderFooter.expenses_listing_add_expenses_button.setVisibility(View.GONE);
				} else {
					holderFooter.expenses_listing_add_expenses_button.setText("Add expenses to "+ mDatadateList.get(sectionnum).get(DatabaseAdapter.KEY_DATE_TIME));
					holderFooter.expenses_listing_add_expenses_button.setFocusable(false);
					holderFooter.expenses_listing_add_expenses_button.setOnClickListener(new MyClickListener(sectionnum));
				}

				return convertView;
			}
			position -= size;
			sectionnum++;

		}
		return null;
	}

	private boolean isCurrentWeek(String dateViewString) {
		try {
			DateHelper mDateHelper = new DateHelper(dateViewString);
			mDateHelper.getTimeMillis();
			return true;
		} catch (Exception e) {
		}

		return false;
	}

	private class ViewHolderBody {
		TextView expense_listing_inflated_row_location_time;
		TextView expense_listing_inflated_row_tag;
		TextView expense_listing_inflated_row_amount;
		ImageView expense_listing_inflated_row_imageview;
		ImageView expense_listing_inflated_row_favorite_icon;
		RelativeLayout expense_listing_inflated_row_listview;
	}

	private class ViewHolderHeader {
		TextView expenses_listing_list_date_view;
		TextView expenses_listing_list_amount_view;
	}

	private class ViewHolderFooter {
		Button expenses_listing_add_expenses_button;
		LinearLayout expense_listing_list_add_expenses;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	private class MyClickListener implements OnClickListener {

		List<String> mListenerList;
		int mPosition;

		public MyClickListener(List<String> mlist) {
			mListenerList = mlist;
		}

		public MyClickListener(int position) {
			mPosition = position;
		}

		@Override
		public void onClick(View v) {
			if (v.getId() == R.id.expense_listing_inflated_row_imageview) {
				if (mListenerList != null)
					if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
						if (mListenerList.get(5).equals(mContext.getString(R.string.voice))) {
							File mFile = new File("/sdcard/ExpenseTracker/Audio/" + mListenerList.get(0) + ".amr");
							if (mFile.canRead()) {
								new AudioPlayDialog(mContext, mListenerList.get(0));
							}
						} else if (mListenerList.get(5).equals(mContext.getString(R.string.camera))) {
							File mFile = new File("/sdcard/ExpenseTracker/" + mListenerList.get(0) + ".jpg");
							if (mFile.canRead()) {
								
								Intent intent = new Intent(mContext, ImagePreview.class);
								intent.putExtra("id", Long.parseLong(mListenerList.get(0)));
								mContext.startActivity(intent);

							}
						}
					}
				if (mListenerList.get(5).equals(mContext.getString(R.string.text))) {
					if (!mListenerList.get(1).equals(mContext.getString(R.string.unfinished_textentry))) {
						new DescriptionDialog(mContext, mListenerList.get(1));
					}
				}
			}

			if (v.getId() == R.id.expenses_listing_add_expenses_button) {
				
				DateHelper mDateHelper = new DateHelper(mDatadateList.get(mPosition).get(DatabaseAdapter.KEY_DATE_TIME));
				final ArrayList<String> mArrayList = insertToDatabase(mDateHelper.getTimeMillis());
				unknownEntryDialog = new UnknownEntryDialog(mContext, mArrayList, new android.view.View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						DatabaseAdapter mDatabaseAdapter = new DatabaseAdapter(mContext);
						mDatabaseAdapter.open();
						mDatabaseAdapter.deleteDatabaseEntryID(mArrayList.get(0));
						mDatabaseAdapter.close();
						unknownEntryDialog.dismiss();
						Toast.makeText(mContext, "Deleted", Toast.LENGTH_SHORT).show();
					}
				});
				
				unknownEntryDialog.setOnCancelListener(new OnCancelListener() {
					
					@Override
					public void onCancel(DialogInterface dialog) {
						DatabaseAdapter mDatabaseAdapter = new DatabaseAdapter(mContext);
						mDatabaseAdapter.open();
						mDatabaseAdapter.deleteDatabaseEntryID(mArrayList.get(0));
						mDatabaseAdapter.close();
						unknownEntryDialog.dismiss();
					}
				});
			}
		}
	}
	
	private ArrayList<String> insertToDatabase(Long timeInMillis) {
		ArrayList<String> mArrayList = new ArrayList<String>();
		for(int i = 0;i<8;i++){
			mArrayList.add("");
		}
		DatabaseAdapter mDatabaseAdapter = new DatabaseAdapter(mContext);
		HashMap<String, String> _list = new HashMap<String, String>();
		Calendar mCalendar = Calendar.getInstance();
		mCalendar.setFirstDayOfWeek(Calendar.MONDAY);
		if(timeInMillis != null){
			_list.put(DatabaseAdapter.KEY_DATE_TIME,Long.toString(timeInMillis));
		} else {
			_list.put(DatabaseAdapter.KEY_DATE_TIME,mCalendar.getTimeInMillis()+"");
		}
		mArrayList.set(6, _list.get(DatabaseAdapter.KEY_DATE_TIME));
		if (LocationHelper.currentAddress != null && LocationHelper.currentAddress.trim() != "") {
			_list.put(DatabaseAdapter.KEY_LOCATION, LocationHelper.currentAddress);
			mArrayList.set(7, LocationHelper.currentAddress);
		}
		_list.put(DatabaseAdapter.KEY_TYPE, mContext.getString(R.string.unknown));
		mArrayList.set(5, _list.get(DatabaseAdapter.KEY_TYPE));
		mDatabaseAdapter.open();
		long _id = mDatabaseAdapter.insert_to_database(_list);
		mDatabaseAdapter.close();
		mArrayList.set(0,Long.toString(_id));
		return mArrayList;
	}
	
	private boolean isEntryComplete(ArrayList<String> toCheckList) {
		if (toCheckList.get(5).equals(mContext.getString(R.string.camera))) {
			if(toCheckList.get(2) != null){
				if (toCheckList.get(2).contains("?")) {
					return false;
				}
			}
			File mFileSmall = new File("/sdcard/ExpenseTracker/" + toCheckList.get(0) + "_small.jpg");
			File mFile = new File("/sdcard/ExpenseTracker/" + toCheckList.get(0) + ".jpg");
			File mFileThumbnail = new File("/sdcard/ExpenseTracker/" + toCheckList.get(0) + "_thumbnail.jpg");
			if (mFile.canRead() && mFileSmall.canRead() && mFileThumbnail.canRead()) {
				return true;
			} else {
				return false;
			}
		} else if (toCheckList.get(5).equals(mContext.getString(R.string.voice))) {
			if(toCheckList.get(2) != null){
				if (toCheckList.get(2).contains("?")) {
					return false;
				}
			}
			File mFile = new File("/sdcard/ExpenseTracker/Audio/" + toCheckList.get(0) + ".amr");
			if (mFile.canRead()) {
				return true;
			} else {
				return false;
			}
		} else if (toCheckList.get(5).equals(mContext.getString(R.string.text))) {
			if(toCheckList.get(2) != null){
				if (toCheckList.get(2).contains("?")) {
					return false;
				}
			}
			if(toCheckList.get(1) != null){
				if (toCheckList.get(1).equals(mContext.getString(R.string.unfinished_textentry)) || toCheckList.get(1).equals(mContext.getString(R.string.finished_textentry))) {
					return false;
				} else {
					return true;
				}
			}
		}
		return false;
	}

}
