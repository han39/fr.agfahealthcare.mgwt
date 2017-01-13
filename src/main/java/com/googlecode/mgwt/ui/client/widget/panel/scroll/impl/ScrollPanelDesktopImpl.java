package com.googlecode.mgwt.ui.client.widget.panel.scroll.impl;

import java.util.Iterator;
import java.util.logging.Logger;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.googlecode.mgwt.collection.shared.LightArrayInt;
import com.googlecode.mgwt.ui.client.widget.panel.scroll.ScrollEndEvent;
import com.googlecode.mgwt.ui.client.widget.panel.scroll.ScrollMoveEvent;
import com.googlecode.mgwt.ui.client.widget.panel.scroll.ScrollPanelAppearance;
import com.googlecode.mgwt.ui.client.widget.panel.scroll.ScrollPanelAppearance.ScrollPanelCss;
import com.googlecode.mgwt.ui.client.widget.panel.scroll.ScrollRefreshEvent;

public class ScrollPanelDesktopImpl extends ScrollPanelImpl {
	private static final String PARENT = "parentScrollPanel";

	private static final String FLEX_WRAPPER = "flexScrollPanel";

	private static final ScrollPanelAppearance SPA = GWT.create(ScrollPanelAppearance.class);

	private static Logger logger = Logger.getLogger(ScrollPanelTouchImpl.class.getName());

	public static native void console(String text)
	/*-{
	console.log(text);
	}-*/;

	private static native int getMouseWheelVelocityY(NativeEvent evt)
	/*-{
	var val =  evt.wheelDelta || evt.detail || 0;
	return Math.round(val);
	}-*/;

	private ScrollPanelCss css;
	private Widget scroller;
	private SimplePanel wrapper;

	private int wrapperHeight;
	private int wrapperWidth;

	private int maxScrollX;

	private int minScrollY;
	private int maxScrollY;
	private boolean hScroll;
	private boolean vScroll;

	private boolean vScrollDesired;
	private boolean hScrollDesired;

	private int x;
	private int y;

	private boolean once = true;

	public ScrollPanelDesktopImpl() {
		wrapper = new SimplePanel();

		css = SPA.css();
		css.ensureInjected();

		wrapper.addStyleName(css.scrollPanel());

		initWidget(wrapper);

		// setting standard options
		hScroll = true;
		vScroll = true;
		hScrollDesired = true;
		vScrollDesired = true;
		x = 0;
		y = 0;

		Event.sinkEvents(wrapper.getElement(), Event.ONSCROLL | Event.ONMOUSEWHEEL);
		wrapper.addHandler(new ScrollHandler() {
			@Override
			public void onScroll(ScrollEvent event) {
				move();
			}
		}, ScrollEvent.getType());

		// Hack pour le problème de smoothScrolling de IE11 !!!
		if (Window.Navigator.getUserAgent().toLowerCase().contains("trident")) {
			wrapper.addDomHandler(new MouseWheelHandler() {
				@Override
				public void onMouseWheel(MouseWheelEvent event) {
					event.preventDefault();
					int delta = getMouseWheelVelocityY(event.getNativeEvent());
					scrollTo(0, delta > 0 ? 100 : -100, 0, true);
					move();
				}
			}, MouseWheelEvent.getType());
		}
	}

	@Override
	public void add(Widget w) {
		if (scroller != null) {
			throw new IllegalStateException("scrollpanel can only have one child");
		}
		setWidget(w);
	}

	@Override
	public void clear() {
		setWidget(null);
	}

	@Override
	public int getCurrentPageX() {
		return 0;
	}

	@Override
	public int getCurrentPageY() {
		return 0;
	}

	@Override
	public int getMaxScrollY() {
		return maxScrollY;
	}

	@Override
	public int getMinScrollY() {
		return minScrollY;
	}

	@Override
	public LightArrayInt getPagesX() {
		return null;
	}

	@Override
	public LightArrayInt getPagesY() {
		return null;
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	@Override
	public boolean isScrollingEnabledX() {
		return hScroll;
	}

	@Override
	public boolean isScrollingEnabledY() {
		return vScroll;
	}

	@Override
	public Iterator<Widget> iterator() {
		return wrapper.iterator();
	}

	@Override
	public void refresh() {
		if (!isAttached()) {
			return;
		}

		wrapperHeight = getClientHeight(wrapper.getElement());
		if (wrapperHeight == 0) {
			wrapperHeight = 1;
		}
		wrapperWidth = getClientWidth(wrapper.getElement());
		if (wrapperWidth == 0) {
			wrapperWidth = 1;
		}

		minScrollY = 0;

		int scrollerWidth = Math.round((scroller.getOffsetWidth() + getMarginWidth(scroller.getElement())));
		int scrollerHeight = Math.round((scroller.getOffsetHeight() + minScrollY + getMarginHeight(scroller.getElement())));

		maxScrollX = wrapperWidth - scrollerWidth;
		maxScrollY = wrapperHeight - scrollerHeight + minScrollY;

		hScroll = (hScrollDesired && maxScrollX < 0);
		vScroll = vScrollDesired && (!hScroll || scrollerHeight > wrapperHeight);

		resetPos(200);

		// fire refresh event
		fireEvent(new ScrollRefreshEvent());
	}

	@Override
	public boolean remove(Widget w) {
		if (w == scroller) {
			scroller = null;
			return wrapper.remove(w);
		}
		return false;
	}

	@Override
	public void scrollTo(int destX, int destY, int newDuration) {
		scrollTo(destX, destY, newDuration, false);
	}

	@Override
	public void scrollTo(int x, int y, int time, boolean relative) {
		if (relative) {
			this.x = this.x - x;
			this.y = this.y - y;
		} else {
			this.x = Math.abs(x);
			this.y = Math.abs(y);
		}
		wrapper.getElement().setScrollTop(this.y);
		wrapper.getElement().setScrollLeft(this.x);
	}

	@Override
	public void scrollToPage(int pageX, int pageY, int time) {
		// NO-OP

	}

	@Override
	public void scrollToPage(int pageX, int pageY, int time, boolean issueEvent) {
		// NO-OP

	}

	@Override
	public void setAutoHandleResize(boolean handle) {
		// NO-OP

	}

	@Override
	public void setBounce(boolean bounce) {
		// NO-OP

	}

	@Override
	public void setBounceFactor(double factor) {
		// NO-OP

	}

	@Override
	public void setHideScrollBar(boolean hideScrollBar) {
		// NO-OP

	}

	@Override
	public void setMaxScrollY(int y) {
		maxScrollY = y;
	}

	@Override
	public void setMinScrollY(int y) {
		minScrollY = y;
	}

	@Override
	public void setMomentum(boolean momentum) {
		// NO-OP
	}

	@Override
	public void setOffSetMaxY(int height) {
		// NO-OP
	}

	@Override
	public void setOffSetY(int y) {
		// NO-OP
	}

	@Override
	public void setScrollingEnabledX(boolean scrollingEnabledX) {
		hScrollDesired = scrollingEnabledX;
	}

	@Override
	public void setScrollingEnabledY(boolean scrollingEnabledY) {
		vScrollDesired = scrollingEnabledY;
	}

	@Override
	public void setScrollLock(boolean lock) {
		// NO-OP
	}

	@Override
	public void setShowHorizontalScrollBar(boolean show) {
		// NO-OP

	}

	@Override
	public void setShowScrollBarX(boolean show) {
		// NO-OP

	}

	@Override
	public void setShowScrollBarY(boolean show) {
		// NO-OP

	}

	@Override
	public void setShowVerticalScrollBar(boolean show) {
		// NO-OP

	}

	@Override
	public void setSnap(boolean snap) {
		// NO-OP
	}

	@Override
	public void setSnapSelector(String selector) {
		// NO-OP
	}

	@Override
	public void setSnapThreshold(int threshold) {
		// NO-OP
	}

	@Override
	public void setUsePos(boolean pos) {
		// NO-OP
	}

	@Override
	public void setWidget(IsWidget child) {
		setWidget(child.asWidget() != null ? child.asWidget() : null);
	}

	@Override
	public void setWidget(Widget w) {
		if (scroller != null) {
			// clean up
			scroller.removeStyleName(css.container());
			remove(scroller);
		}

		scroller = w;

		if (scroller != null) {
			wrapper.setWidget(scroller);
			scroller.addStyleName(css.container());
			if (isAttached()) {
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override
					public void execute() {
						refresh();
					}
				});
			}
		}
	}

	@Override
	protected void onLoad() {
		if (once) {
			once = false;
			// Le parent n'est pas forcément un widget
			getElement().getParentElement().addClassName(PARENT);
			addStyleName(FLEX_WRAPPER);
		}

		if (scroller != null) {
			Scheduler.get().scheduleDeferred(new ScheduledCommand() {
				@Override
				public void execute() {
					refresh();
				}
			});
		}
	}

	private native int getClientHeight(Element element)
	/*-{
	return element.clientHeight || 0;
	}-*/;

	private native int getClientWidth(Element element)
	/*-{
	return element.clientWidth || 0;
	}-*/;

	private native int getMarginHeight(Element el)
	/*-{
	var top = 0;
	var bottom = 0;
	var style = $wnd.getComputedStyle(el);
	
	
	top = parseInt(style.marginTop, 10) || 0;
	bottom = parseInt(style.marginBottom, 10) || 0;
	
	
	return top + bottom;
	}-*/;

	private native int getMarginWidth(Element el)
	/*-{
	var left = 0;
	var right = 0;
	var style = $wnd.getComputedStyle(el);
	
	
	left = parseInt(style.marginLeft, 10) || 0;
	right = parseInt(style.marginRight, 10) || 0;
	
	
	return left + right;
	}-*/;

	private void move() {
		x = wrapper.getElement().getScrollLeft();
		y = wrapper.getElement().getScrollTop();

		fireEvent(new ScrollMoveEvent(null));
		fireEvent(new ScrollEndEvent());
	}

	private void resetPos(int time) {
		int resetX = x < 0 ? 0 : x < maxScrollX ? maxScrollX : x;
		int resetY = y < minScrollY || maxScrollY > 0 ? minScrollY : y < maxScrollY ? maxScrollY : y;
		if (resetX == x && resetY == y) {
			return;
		}

		scrollTo(resetX, resetY, time);
	}
}
