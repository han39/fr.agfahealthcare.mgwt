/*
 * Copyright 2010 Daniel Kurka
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.googlecode.mgwt.ui.client.widget.list.widgetlist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.googlecode.mgwt.dom.client.event.tap.HasTapHandlers;
import com.googlecode.mgwt.dom.client.event.tap.TapEvent;
import com.googlecode.mgwt.dom.client.event.tap.TapHandler;
import com.googlecode.mgwt.dom.client.recognizer.TapRecognizer;
import com.googlecode.mgwt.ui.client.widget.list.celllist.CellList;
import com.googlecode.mgwt.ui.client.widget.touch.TouchWidgetImpl;

/**
 * A list that can contain widgets
 *
 * This class renders widgets into a list. The same thing is done by {@link CellList} much more efficient.
 *
 * <b>Note:</b> Normally you should be using {@link CellList}. Only if you really need Widgets inside the list (which won't be the case most of the time) you should be using WidgetList.
 *
 * The reference for styling can be found in CellList as well.
 *
 *
 * @author Daniel Kurka
 */
public class WidgetList extends Composite implements HasWidgets, HasSelectionHandlers<Integer> {

	public static class WidgetListEntry extends Composite implements AcceptsOneWidget, HasTapHandlers {

		private static final TouchWidgetImpl IMPL = GWT.create(TouchWidgetImpl.class);

		@UiField
		public Panel container;
		// UiBinder needs this field
		private WidgetListAppearance appearance;

		public WidgetListEntry(final WidgetListAppearance appearance) {
			this.appearance = appearance;
			initWidget(this.appearance.uiBinderEntry().createAndBindUi(this));
			IMPL.addTouchHandler(container, new TapRecognizer(this));
		}

		@Override
		public HandlerRegistration addTapHandler(final TapHandler handler) {
			return addHandler(handler, TapEvent.getType());
		}

		@UiFactory
		public WidgetListAppearance getAppearance() {
			return appearance;
		}

		@Override
		public void setWidget(final IsWidget w) {
			container.add(w);
		}
	}

	private static class Entry {
		WidgetListEntry entry;
		HandlerRegistration handlerRegistration;

		public Entry(final WidgetListEntry entry, final HandlerRegistration handlerRegistration) {
			this.entry = entry;
			this.handlerRegistration = handlerRegistration;
		}
	}

	private static final WidgetListAppearance DEFAULT_APPEARANCE = GWT.create(WidgetListAppearance.class);

	private int childCount;
	private List<WidgetListEntry> children = new ArrayList<>();

	@UiField
	public ComplexPanel container;

	@UiField
	public Panel headerContainer;

	private Map<Widget, Entry> map;

	private WidgetListAppearance appearance;

	private Widget header;

	public WidgetList() {
		this(DEFAULT_APPEARANCE);
	}

	public WidgetList(final WidgetListAppearance appearance) {
		this.appearance = appearance;
		initWidget(this.appearance.uiBinder().createAndBindUi(this));
		map = new HashMap<>();
	}

	@Override
	public void add(final Widget w) {

		final WidgetListEntry widgetListEntry = new WidgetListEntry(appearance);
		widgetListEntry.setWidget(w);
		final HandlerRegistration handlerRegistration = widgetListEntry.addTapHandler(event -> SelectionEvent.fire(WidgetList.this, container.getWidgetIndex(widgetListEntry)));
		if (childCount == 0) {
			widgetListEntry.addStyleName(appearance.css().widgetListEntryFirstChild());
		}
		map.put(w, new Entry(widgetListEntry, handlerRegistration));
		container.add(widgetListEntry);
		children.add(widgetListEntry);

		if (childCount > 0) {
			children.get(childCount - 1).removeStyleName(appearance.css().widgetListEntryLastChild());
		}
		widgetListEntry.addStyleName(appearance.css().widgetListEntryLastChild());

		childCount++;

	}

	@Override
	public HandlerRegistration addSelectionHandler(final SelectionHandler<Integer> handler) {
		return addHandler(handler, SelectionEvent.getType());
	}

	@Override
	public void clear() {
		container.clear();
		for (final Entry entry : map.values()) {
			entry.handlerRegistration.removeHandler();
		}
		map.clear();
		children.clear();
		childCount = 0;
		if (header != null) {
			add(header);
		}
	}

	@UiFactory
	public WidgetListAppearance getAppearance() {
		return appearance;
	}

	public int getWidgetCount() {
		return childCount;
	}

	@Override
	public Iterator<Widget> iterator() {
		return map.keySet().iterator();
	}

	@SuppressWarnings("unlikely-arg-type")
	@Override
	public boolean remove(final Widget w) {
		final Entry entry = map.remove(w);
		if (entry == null) {
			return false;
		}
		// did we remove the last child?
		if (children.get(childCount - 1) == entry.entry) {
			// are there others in the list?
			if (childCount - 2 >= 0) {
				children.get(childCount - 2).addStyleName(appearance.css().widgetListEntryLastChild());
			}
		}

		// did we remove the first child
		if (children.get(0) == entry.entry) {
			if (children.size() > 1) {
				children.get(1).addStyleName(appearance.css().widgetListEntryFirstChild());
			}
		}
		childCount--;
		children.remove(entry);
		entry.handlerRegistration.removeHandler();
		return container.remove(entry.entry);
	}

	@UiChild(limit = 1,
			tagname = "header")
	public void setHeader(final Widget header) {
		headerContainer.setVisible(header != null);
		headerContainer.clear();
		if (header != null) {
			headerContainer.add(header);
		}
	}

	/**
	 * Should the list be displayed with rounded corners
	 *
	 * @param round
	 *           true to display with rounded corners
	 */
	public void setRound(final boolean round) {
		if (round) {
			addStyleName(appearance.css().round());
		} else {
			removeStyleName(appearance.css().round());
		}
	}

	public void setSelectAble(final int index, final boolean group) {
		if (group) {
			children.get(index).addStyleName(appearance.css().widgetListEntrySelectedable());
		} else {
			children.get(index).removeStyleName(appearance.css().widgetListEntrySelectedable());
		}
	}
}
