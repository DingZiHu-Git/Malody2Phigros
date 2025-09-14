package com.dzh.m2p;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FaqFragment extends Fragment {
	private Activity activity;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_faq, container, false);
		activity = getActivity();
		ExpandableListView elv = root.findViewById(R.id.fragment_faq_expandable_list_view);
		List<Map<String, Object>> groups = new ArrayList<>();
		Map<String, Object> group1 = new HashMap<>();
		group1.put("group1", getString(R.string.fragment_faq_chart_packing_title));
		groups.add(group1);
		List<List<Map<String, Object>>> childs = new ArrayList<>();
		List<Map<String, Object>> child1 = new ArrayList<>();
		Map<String, Object> child1I = new HashMap<>();
		child1I.put("child1", getString(R.string.fragment_faq_chart_packing_content));
		child1.add(child1I);
		childs.add(child1);
		elv.setAdapter(new SimpleExpandableListAdapter(activity, groups, android.R.layout.simple_expandable_list_item_1, new String[]{ "group1" }, new int[]{ android.R.id.text1 }, childs, android.R.layout.simple_list_item_1, new String[]{ "child1" }, new int[]{ android.R.id.text1 }));
		return root;
	}
}
