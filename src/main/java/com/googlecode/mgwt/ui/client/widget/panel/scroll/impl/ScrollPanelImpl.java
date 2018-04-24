/*
 * Copyright 2011 Daniel Kurka
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
package com.googlecode.mgwt.ui.client.widget.panel.scroll.impl;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.googlecode.mgwt.collection.shared.LightArrayInt;
import com.googlecode.mgwt.ui.client.widget.panel.scroll.BeforeScrollEndEvent;
import com.googlecode.mgwt.ui.client.widget.panel.scroll.BeforeScrollMoveEvent;
import com.googlecode.mgwt.ui.client.widget.panel.scroll.BeforeScrollStartEvent;
import com.googlecode.mgwt.ui.client.widget.panel.scroll.ScrollAnimationEndEvent;
import com.googlecode.mgwt.ui.client.widget.panel.scroll.ScrollAnimationMoveEvent;
import com.googlecode.mgwt.ui.client.widget.panel.scroll.ScrollAnimationStartEvent;
import com.googlecode.mgwt.ui.client.widget.panel.scroll.ScrollAnimationStartEvent.Handler;
import com.googlecode.mgwt.ui.client.widget.panel.scroll.ScrollEndEvent;
import com.googlecode.mgwt.ui.client.widget.panel.scroll.ScrollMoveEvent;
import com.googlecode.mgwt.ui.client.widget.panel.scroll.ScrollRefreshEvent;
import com.googlecode.mgwt.ui.client.widget.panel.scroll.ScrollStartEvent;
import com.googlecode.mgwt.ui.client.widget.panel.scroll.ScrollTouchEndEvent;

/**
 * ScrollPanelImpl abstracts different implementations for scrolling behaviour
 *
 * @author Daniel Kurka
 */
public abstract class ScrollPanelImpl extends Composite implements HasWidgets {

	public HandlerRegistration addBeforeScrollEndHandler(final BeforeScrollEndEvent.Handler handler) {
		return addHandler(handler, BeforeScrollEndEvent.getTYPE());
	}

	public HandlerRegistration addBeforeScrollMoveHandler(final BeforeScrollMoveEvent.Handler handler) {
		return addHandler(handler, BeforeScrollMoveEvent.getTYPE());
	}

	public HandlerRegistration addBeforeScrollStartHandler(final BeforeScrollStartEvent.Handler handler) {
		return addHandler(handler, BeforeScrollStartEvent.getTYPE());
	}

	public HandlerRegistration addScrollAnimationEndHandler(final ScrollAnimationEndEvent.Handler handler) {
		return addHandler(handler, ScrollAnimationEndEvent.getTYPE());
	}

	public HandlerRegistration addScrollAnimationMoveHandler(final ScrollAnimationMoveEvent.Handler handler) {
		return addHandler(handler, ScrollAnimationMoveEvent.getTYPE());
	}

	public HandlerRegistration addScrollAnimationStartHandler(final Handler handler) {
		return addHandler(handler, ScrollAnimationStartEvent.getTYPE());
	}

	public HandlerRegistration addScrollEndHandler(final ScrollEndEvent.Handler handler) {
		return addHandler(handler, ScrollEndEvent.getTYPE());
	}

	public HandlerRegistration addScrollMoveHandler(final ScrollMoveEvent.Handler handler) {
		return addHandler(handler, ScrollMoveEvent.getTYPE());
	}

	public HandlerRegistration addScrollRefreshHandler(final ScrollRefreshEvent.Handler handler) {
		return addHandler(handler, ScrollRefreshEvent.getTYPE());
	}

	public HandlerRegistration addScrollStartHandler(final ScrollStartEvent.Handler handler) {
		return addHandler(handler, ScrollStartEvent.getTYPE());
	}

	public HandlerRegistration addScrollTouchEndHandler(final ScrollTouchEndEvent.Handler handler) {
		return addHandler(handler, ScrollTouchEndEvent.getTYPE());
	}

	public abstract int getCurrentPageX();

	public abstract int getCurrentPageY();

	public abstract int getMaxScrollY();

	public abstract int getMinScrollY();

	public abstract LightArrayInt getPagesX();

	public abstract LightArrayInt getPagesY();

	public abstract int getX();

	public abstract int getY();

	/**
	 * Is scrolling enabled in x-axis
	 *
	 * @return true if scrolling is enabled
	 */
	public abstract boolean isScrollingEnabledX();

	/**
	 * Is scrolling enabled in y-axis
	 *
	 * @return true if scrolling is enabled
	 */
	public abstract boolean isScrollingEnabledY();

	/**
	 * Recalculate dimensions for scrolling
	 *
	 * (needs to be called when the content of the childarea changes without setting a new child)
	 */
	public abstract void refresh();

	/**
	 * Scroll to a given position in the specified time
	 *
	 * @param destX
	 *           the new position x
	 * @param destY
	 *           the new position y
	 * @param newDuration
	 *           the duration
	 */
	public abstract void scrollTo(int destX, int destY, int newDuration);

	public abstract void scrollTo(int x, int y, int time, boolean relative);

	public abstract void scrollToPage(int pageX, int pageY, int time);

	public abstract void scrollToPage(int pageX, int pageY, int time, boolean issueEvent);

	public abstract void setAutoHandleResize(boolean handle);

	public abstract void setBounce(boolean bounce);

	public abstract void setBounceFactor(double factor);

	public abstract void setHideScrollBar(boolean hideScrollBar);

	public abstract void setMaxScrollY(int y);

	public abstract void setMinScrollY(int y);

	public abstract void setMomentum(boolean momentum);

	public abstract void setOffSetMaxY(int height);

	public abstract void setOffSetY(int y);

	/**
	 * enable scrolling in x-axis
	 *
	 * @param scrollingEnabledX
	 *           true to enable scrolling
	 */
	public abstract void setScrollingEnabledX(boolean scrollingEnabledX);

	/**
	 * enable scrolling in y-axis
	 *
	 * @param scrollingEnabledY
	 *           a boolean.
	 */
	public abstract void setScrollingEnabledY(boolean scrollingEnabledY);

	public abstract void setScrollLock(boolean lock);

	public abstract void setShowHorizontalScrollBar(boolean show);

	public abstract void setShowVerticalScrollBar(boolean show);

	public abstract void setSnap(boolean snap);

	public abstract void setSnapSelector(String selector);

	public abstract void setSnapThreshold(int threshold);

	/**
	 * instruct the panel to use position absolute instead of translate3d
	 *
	 * on android devices input fields behave strange when used with translate3d
	 *
	 * take a look into the mgwt docs
	 *
	 * @param pos
	 *           true to use absolute position default: translate3d
	 */
	public abstract void setUsePos(boolean pos);

	/**
	 * set the content of the scrollable area
	 *
	 * @param child
	 *           the content of the scrollable area
	 */
	public abstract void setWidget(IsWidget child);
}
