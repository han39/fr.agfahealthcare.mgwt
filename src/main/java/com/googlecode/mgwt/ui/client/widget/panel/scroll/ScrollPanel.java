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
package com.googlecode.mgwt.ui.client.widget.panel.scroll;

import java.util.Iterator;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.googlecode.mgwt.collection.shared.LightArrayInt;
import com.googlecode.mgwt.ui.client.widget.panel.flex.IsFlexible;
import com.googlecode.mgwt.ui.client.widget.panel.scroll.impl.ScrollPanelImpl;

/**
 * A scroll panel that can handle touch input and has momentum
 *
 * The scrollbars maybe missing from the DOM if scrolling is disabled for a certain direction.
 *
 * @author Daniel Kurka
 *
 */
public class ScrollPanel extends Composite implements HasWidgets, IsFlexible {

	protected final ScrollPanelImpl impl = GWT.create(ScrollPanelImpl.class);

	public ScrollPanel() {
		initWidget(impl);
	}

	/**
	 * Methods only exists to make scroll panel work with UiBinder @use {@link #setWidget(IsWidget)}
	 */
	@Override
	public void add(Widget w) {
		impl.add(w);

	}

	public HandlerRegistration addBeforeScrollEndHandler(BeforeScrollEndEvent.Handler handler) {
		return impl.addBeforeScrollEndHandler(handler);
	}

	public HandlerRegistration addBeforeScrollMoveHandler(BeforeScrollMoveEvent.Handler handler) {
		return impl.addBeforeScrollMoveHandler(handler);
	}

	public HandlerRegistration addBeforeScrollStartHandler(BeforeScrollStartEvent.Handler handler) {
		return impl.addBeforeScrollStartHandler(handler);
	}

	public HandlerRegistration addScrollAnimationEndHandler(ScrollAnimationEndEvent.Handler handler) {
		return impl.addScrollAnimationEndHandler(handler);
	}

	public HandlerRegistration addScrollAnimationMoveHandler(ScrollAnimationMoveEvent.Handler handler) {
		return impl.addScrollAnimationMoveHandler(handler);
	}

	public HandlerRegistration addScrollAnimationStartHandler(
			ScrollAnimationStartEvent.Handler handler) {
		return impl.addScrollAnimationStartHandler(handler);
	}

	public HandlerRegistration addScrollEndHandler(ScrollEndEvent.Handler handler) {
		return impl.addScrollEndHandler(handler);
	}

	public HandlerRegistration addScrollMoveHandler(ScrollMoveEvent.Handler handler) {
		return impl.addScrollMoveHandler(handler);
	}

	public HandlerRegistration addScrollRefreshHandler(ScrollRefreshEvent.Handler handler) {
		return impl.addScrollRefreshHandler(handler);
	}

	public HandlerRegistration addScrollStartHandler(ScrollStartEvent.Handler handler) {
		return impl.addScrollStartHandler(handler);
	}

	public HandlerRegistration addScrollTouchEndHandler(ScrollTouchEndEvent.Handler handler) {
		return impl.addScrollTouchEndHandler(handler);
	}

	@Override
	public void clear() {
		impl.clear();

	}

	public int getCurrentPageX() {
		return impl.getCurrentPageX();
	}

	public int getCurrentPageY() {
		return impl.getCurrentPageY();
	}

	public int getMaxScrollY() {
		return impl.getMaxScrollY();
	}

	public int getMinScrollY() {
		return impl.getMinScrollY();
	}

	public LightArrayInt getPagesX() {
		return impl.getPagesX();
	}

	public LightArrayInt getPagesY() {
		return impl.getPagesY();
	}

	public int getX() {
		return impl.getX();
	}

	public int getY() {
		return impl.getY();
	}

	public boolean isScrollingEnabledY() {
		return impl.isScrollingEnabledY();
	}

	@Override
	public Iterator<Widget> iterator() {
		return impl.iterator();
	}

	/**
	 * Refresh the scroll panel
	 *
	 * This method needs to be called if the content of the child widget has changed without calling
	 * {@link #setWidget(IsWidget)}
	 *
	 * ScrollPanel needs to recalculate sizes.
	 */
	public void refresh() {
		impl.refresh();

	}

	@Override
	public boolean remove(Widget w) {
		return impl.remove(w);
	}

	public void scrollTo(int x, int y) {
		impl.scrollTo(x, y, 1);
	}

	public void scrollTo(int x, int y, int time, boolean relative) {
		impl.scrollTo(x, y, time, relative);
	}

	public void scrollToPage(int pageX, int pageY, int time) {
		impl.scrollToPage(pageX, pageY, time);
	}

	public void scrollToPage(int pageX, int pageY, int time, boolean issueEvent) {
		impl.scrollToPage(pageX, pageY, time, issueEvent);
	}

	public void setAutoHandleResize(boolean handle) {
		impl.setAutoHandleResize(handle);
	}

	public void setBounce(boolean bounce) {
		impl.setBounce(bounce);
	}

	public void setBounceFactor(double bounceFactor) {
		impl.setBounceFactor(bounceFactor);
	}

	public void setHideScrollBar(boolean hideScrollBar) {
		impl.setHideScrollBar(hideScrollBar);
	}

	public void setMaxScrollY(int y) {
		impl.setMaxScrollY(y);
	}

	public void setMinScrollY(int y) {
		impl.setMinScrollY(y);
	}

	public void setMomentum(boolean momentum) {
		impl.setMomentum(momentum);
	}

	public void setOffSetMaxY(int height) {
		impl.setOffSetMaxY(height);

	}

	public void setOffSetY(int y) {
		impl.setOffSetY(y);
	}

	/**
	 * Should scrolling in x-axis be enabled
	 *
	 * @param enabled
	 *           true to enable
	 */
	public void setScrollingEnabledX(boolean enabled) {
		impl.setScrollingEnabledX(enabled);

	}

	/**
	 * Should scrolling in y-axis be enabled
	 *
	 * @param enabled
	 *           true to enable
	 */
	public void setScrollingEnabledY(boolean enabled) {
		impl.setScrollingEnabledY(enabled);

	}

	public void setScrollLock(boolean lock) {
		impl.setScrollLock(lock);
	}

	public void setShowHorizontalScrollBar(boolean show) {
		impl.setShowHorizontalScrollBar(show);
	}

	/**
	 * @deprecated use {@link #setShowVerticalScrollBar(boolean)}
	 */
	@Deprecated
	public void setShowScrollBarX(boolean b) {
		impl.setShowScrollBarX(b);
	}

	/**
	 * @deprecated use {@link #setShowHorizontalScrollBar(boolean)}
	 */
	@Deprecated
	public void setShowScrollBarY(boolean b) {
		impl.setShowScrollBarY(b);
	}

	public void setShowVerticalScrollBar(boolean show) {
		impl.setShowVerticalScrollBar(show);
	}

	public void setSnap(boolean snap) {
		impl.setSnap(snap);
	}

	public void setSnapSelector(String selector) {
		impl.setSnapSelector(selector);
	}

	public void setSnapThreshold(int threshold) {
		impl.setSnapThreshold(threshold);
	}

	/**
	 * Use position absolute instead of -webkit-translate
	 *
	 * This is required on android if the scrolling area contains input elements
	 *
	 * default: false
	 *
	 * @param android
	 *           a boolean.
	 */
	public void setUsePos(boolean android) {
		impl.setUsePos(android);

	}

	public void setWidget(IsWidget w) {
		impl.setWidget(w);
	}

	@Override
	public void setWidget(Widget w) {
		impl.setWidget(w);
	}
}
