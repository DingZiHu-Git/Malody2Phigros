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
		List<List<Map<String, Object>>> childs = new ArrayList<>();
		Map<String, Object> group1 = new HashMap<>();
		group1.put("group1", getString(R.string.fragment_faq_chart_packing_title));
		groups.add(group1);
		List<Map<String, Object>> child1 = new ArrayList<>();
		Map<String, Object> child1I = new HashMap<>();
		child1I.put("child1", getString(R.string.fragment_faq_chart_packing_content));
		child1.add(child1I);
		childs.add(child1);
		Map<String, Object> group2 = new HashMap<>();
		group2.put("group1", getString(R.string.fragment_faq_drag_interval_title));
		groups.add(group2);
		List<Map<String, Object>> child2 = new ArrayList<>();
		Map<String, Object> child2I = new HashMap<>();
		child2I.put("child1", getString(R.string.fragment_faq_drag_interval_content));
		child2.add(child2I);
		childs.add(child2);
		Map<String, Object> group3 = new HashMap<>();
		group3.put("group1", getString(R.string.fragment_faq_how_to_make_a_plugin_title));
		groups.add(group3);
		List<Map<String, Object>> child3 = new ArrayList<>();
		Map<String, Object> child3I = new HashMap<>();
		child3I.put("child1", getString(R.string.fragment_faq_how_to_make_a_plugin_content));
		child3.add(child3I);
		childs.add(child3);
		elv.setAdapter(new SimpleExpandableListAdapter(activity, groups, android.R.layout.simple_expandable_list_item_1, new String[]{ "group1" }, new int[]{ android.R.id.text1 }, childs, android.R.layout.simple_list_item_1, new String[]{ "child1" }, new int[]{ android.R.id.text1 }));
		return root;
	}
}
