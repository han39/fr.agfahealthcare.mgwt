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
	public void add(final Widget w) {
		impl.add(w);

	}

	public HandlerRegistration addBeforeScrollEndHandler(final BeforeScrollEndEvent.Handler handler) {
		return impl.addBeforeScrollEndHandler(handler);
	}

	public HandlerRegistration addBeforeScrollMoveHandler(final BeforeScrollMoveEvent.Handler handler) {
		return impl.addBeforeScrollMoveHandler(handler);
	}

	public HandlerRegistration addBeforeScrollStartHandler(final BeforeScrollStartEvent.Handler handler) {
		return impl.addBeforeScrollStartHandler(handler);
	}

	public HandlerRegistration addScrollAnimationEndHandler(final ScrollAnimationEndEvent.Handler handler) {
		return impl.addScrollAnimationEndHandler(handler);
	}

	public HandlerRegistration addScrollAnimationMoveHandler(final ScrollAnimationMoveEvent.Handler handler) {
		return impl.addScrollAnimationMoveHandler(handler);
	}

	public HandlerRegistration addScrollAnimationStartHandler(
			final ScrollAnimationStartEvent.Handler handler) {
		return impl.addScrollAnimationStartHandler(handler);
	}

	public HandlerRegistration addScrollEndHandler(final ScrollEndEvent.Handler handler) {
		return impl.addScrollEndHandler(handler);
	}

	public HandlerRegistration addScrollMoveHandler(final ScrollMoveEvent.Handler handler) {
		return impl.addScrollMoveHandler(handler);
	}

	public HandlerRegistration addScrollRefreshHandler(final ScrollRefreshEvent.Handler handler) {
		return impl.addScrollRefreshHandler(handler);
	}

	public HandlerRegistration addScrollStartHandler(final ScrollStartEvent.Handler handler) {
		return impl.addScrollStartHandler(handler);
	}

	public HandlerRegistration addScrollTouchEndHandler(final ScrollTouchEndEvent.Handler handler) {
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
	 * This method needs to be called if the content of the child widget has changed without calling {@link #setWidget(IsWidget)}
	 *
	 * ScrollPanel needs to recalculate sizes.
	 */
	public void refresh() {
		impl.refresh();

	}

	@Override
	public boolean remove(final Widget w) {
		return impl.remove(w);
	}

	public void scrollTo(final int x, final int y) {
		impl.scrollTo(x, y, 1);
	}

	public void scrollTo(final int x, final int y, final int time, final boolean relative) {
		impl.scrollTo(x, y, time, relative);
	}

	public void scrollToPage(final int pageX, final int pageY, final int time) {
		impl.scrollToPage(pageX, pageY, time);
	}

	public void scrollToPage(final int pageX, final int pageY, final int time, final boolean issueEvent) {
		impl.scrollToPage(pageX, pageY, time, issueEvent);
	}

	public void setAutoHandleResize(final boolean handle) {
		impl.setAutoHandleResize(handle);
	}

	public void setBounce(final boolean bounce) {
		impl.setBounce(bounce);
	}

	public void setBounceFactor(final double bounceFactor) {
		impl.setBounceFactor(bounceFactor);
	}

	public void setHideScrollBar(final boolean hideScrollBar) {
		impl.setHideScrollBar(hideScrollBar);
	}

	public void setMaxScrollY(final int y) {
		impl.setMaxScrollY(y);
	}

	public void setMinScrollY(final int y) {
		impl.setMinScrollY(y);
	}

	public void setMomentum(final boolean momentum) {
		impl.setMomentum(momentum);
	}

	public void setOffSetMaxY(final int height) {
		impl.setOffSetMaxY(height);

	}

	public void setOffSetY(final int y) {
		impl.setOffSetY(y);
	}

	/**
	 * Should scrolling in x-axis be enabled
	 *
	 * @param enabled
	 *           true to enable
	 */
	public void setScrollingEnabledX(final boolean enabled) {
		impl.setScrollingEnabledX(enabled);

	}

	/**
	 * Should scrolling in y-axis be enabled
	 *
	 * @param enabled
	 *           true to enable
	 */
	public void setScrollingEnabledY(final boolean enabled) {
		impl.setScrollingEnabledY(enabled);

	}

	public void setScrollLock(final boolean lock) {
		impl.setScrollLock(lock);
	}

	public void setShowHorizontalScrollBar(final boolean show) {
		impl.setShowHorizontalScrollBar(show);
	}

	public void setShowVerticalScrollBar(final boolean show) {
		impl.setShowVerticalScrollBar(show);
	}

	public void setSnap(final boolean snap) {
		impl.setSnap(snap);
	}

	public void setSnapSelector(final String selector) {
		impl.setSnapSelector(selector);
	}

	public void setSnapThreshold(final int threshold) {
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
	public void setUsePos(final boolean android) {
		impl.setUsePos(android);

	}

	public void setWidget(final IsWidget w) {
		impl.setWidget(w);
	}

	@Override
	public void setWidget(final Widget w) {
		impl.setWidget(w);
	}
}
