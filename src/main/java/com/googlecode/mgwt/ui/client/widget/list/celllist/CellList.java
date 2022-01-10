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
package com.googlecode.mgwt.ui.client.widget.list.celllist;

import java.util.List;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.Touch;
import com.google.gwt.event.dom.client.TouchCancelEvent;
import com.google.gwt.event.dom.client.TouchEndEvent;
import com.google.gwt.event.dom.client.TouchMoveEvent;
import com.google.gwt.event.dom.client.TouchStartEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Widget;
import com.googlecode.mgwt.dom.client.event.tap.Tap;
import com.googlecode.mgwt.dom.client.event.touch.TouchHandler;
import com.googlecode.mgwt.dom.client.recognizer.EventPropagator;
import com.googlecode.mgwt.ui.client.widget.touch.TouchWidgetImpl;

/**
 *
 * A widget that renders its children as a list
 *
 * You can control the markup of the children by using the Cell interface, therefore you can render any kind of arbitrary markup
 *
 * @param <T>
 *           the type of the model to render
 */
public class CellList<T> extends Widget implements HasCellSelectedHandler {

	public interface EntryTemplate {
		SafeHtml li(int idx, String classes, SafeHtml cellContents);
	}

	private class InternalTouchHandler implements TouchHandler {

		private boolean moved;
		private int index;
		private Element node;
		private int x;
		private int y;
		private boolean started;
		private Element originalElement;

		@Override
		public void onTouchCancel(final TouchCancelEvent event) {

		}

		@Override
		public void onTouchEnd(final TouchEndEvent event) {
			if (node != null) {
				node.removeClassName(CellList.this.appearance.css().selected());
				stopTimer();
			}
			if (started && !moved && index != -1) {
				fireSelectionAtIndex(index, originalElement);
			}
			node = null;
			started = false;

		}

		@Override
		public void onTouchMove(final TouchMoveEvent event) {
			final Touch touch = event.getTouches().get(0);
			if (Math.abs(touch.getPageX() - x) > Tap.RADIUS
					|| Math.abs(touch.getPageY() - y) > Tap.RADIUS) {
				moved = true;
				// deselect
				if (node != null) {
					node.removeClassName(CellList.this.appearance.css().selected());
					stopTimer();
				}

			}

		}

		@Override
		public void onTouchStart(final TouchStartEvent event) {
			started = true;

			x = event.getTouches().get(0).getPageX();
			y = event.getTouches().get(0).getPageY();

			if (node != null) {
				node.removeClassName(CellList.this.appearance.css().selected());
			}
			moved = false;
			index = -1;
			// Get the event target.
			EventTarget eventTarget = event.getNativeEvent().getEventTarget();
			if (eventTarget == null) {
				return;
			}

			// no textnode or element node
			if (!Node.is(eventTarget) && !Element.is(eventTarget)) {
				return;
			}

			// text node use the parent..
			if (Node.is(eventTarget) && !Element.is(eventTarget)) {
				final Node target = Node.as(eventTarget);
				eventTarget = target.getParentElement().cast();
			}

			// no element
			if (!Element.is(eventTarget)) {
				return;
			}
			Element target = eventTarget.cast();

			originalElement = target;

			// Find cell
			String idxString = "";
			while (target != null && (idxString = target.getAttribute("__idx")).length() == 0) {

				target = target.getParentElement();
			}
			if (idxString.length() > 0) {
				try {
					index = Integer.parseInt(idxString);
					node = target;
					startTimer(node);
				} catch (final Exception e) {}
			}

		}
	}

	protected static final EventPropagator EVENT_PROPAGATOR = GWT.create(EventPropagator.class);

	private static final CellListAppearance DEFAULT_APPEARANCE = GWT.create(CellListAppearance.class);

	private static final TouchWidgetImpl impl = GWT.create(TouchWidgetImpl.class);

	protected final Cell<T> cell;

	protected Timer timer;

	@UiField
	public Element container;
	private CellListAppearance appearance;

	protected EntryTemplate entryTemplate;

	/**
	 * Construct a CellList
	 *
	 * @param cell
	 *           the cell to use
	 */
	public CellList(final Cell<T> cell) {
		this(cell, DEFAULT_APPEARANCE);
	}

	/**
	 * Construct a celllist with a given cell and css
	 *
	 * @param cell
	 *           the cell to use
	 * @param css
	 *           the css to use
	 */
	public CellList(final Cell<T> cell, final CellListAppearance appearance) {

		this.cell = cell;
		this.appearance = appearance;

		setElement(this.appearance.uiBinder().createAndBindUi(this));
		entryTemplate = this.appearance.getEntryTemplate();

		final InternalTouchHandler touchHandler = new InternalTouchHandler();
		impl.addTouchHandler(this, touchHandler);
	}

	@Override
	public HandlerRegistration addCellSelectedHandler(final CellSelectedHandler cellSelectedHandler) {
		return addHandler(cellSelectedHandler, CellSelectedEvent.getType());
	}

	@UiFactory
	public CellListAppearance getAppearance() {
		return appearance;
	}

	/**
	 * Render a List of models in this cell list
	 *
	 * @param models
	 *           the list of models to render
	 */
	public void render(final List<T> models) {

		final SafeHtmlBuilder sb = new SafeHtmlBuilder();

		for (int i = 0 ; i < models.size() ; i++) {

			final T model = models.get(i);

			final SafeHtmlBuilder cellBuilder = new SafeHtmlBuilder();

			String clazz = this.appearance.css().entry() + " ";
			if (cell.canBeSelected(model)) {
				clazz += this.appearance.css().canbeSelected() + " ";
			}

			if (i == 0) {
				clazz += this.appearance.css().first() + " ";
			}

			if (models.size() - 1 == i) {
				clazz += this.appearance.css().last() + " ";
			}

			cell.render(cellBuilder, model);

			sb.append(entryTemplate.li(i, clazz, cellBuilder.toSafeHtml()));
		}

		final String html = sb.toSafeHtml().asString();

		getElement().setInnerHTML(html);

		if (models.size() > 0) {
			final String innerHTML = getElement().getInnerHTML();
			if ("".equals(innerHTML.trim())) {
				fixBug(html);
			}
		}

	}

	/**
	 * Set a selected element in the celllist
	 *
	 * @param index
	 *           the index of the element
	 * @param selected
	 *           true to select the element, false to deselect
	 */
	public void setSelectedIndex(final int index, final boolean selected) {
		final Node node = getElement().getChild(index);
		final Element li = Element.as(node);
		if (selected) {
			li.addClassName(this.appearance.css().selected());
		} else {
			li.removeClassName(this.appearance.css().selected());
		}
	}

	protected void fireSelectionAtIndex(final int index, final Element element) {
		EVENT_PROPAGATOR.fireEvent(this, new CellSelectedEvent(index, element));
	}

	protected void fixBug(final String html) {
		new Timer() {

			@Override
			public void run() {
				getElement().setInnerHTML(html);
				final String innerHTML = getElement().getInnerHTML();
				if ("".equals(innerHTML.trim())) {
					fixBug(html);

				}

			}
		}.schedule(100);
	}

	protected void startTimer(final Element node) {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}

		timer = new Timer() {

			@Override
			public void run() {
				node.addClassName(CellList.this.appearance.css().selected());
			}
		};
		timer.schedule(150);
	}

	protected void stopTimer() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}
}
