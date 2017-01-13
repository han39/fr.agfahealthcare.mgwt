package com.googlecode.mgwt.ui.client.widget.panel.scroll.impl;

import java.util.Iterator;
import java.util.logging.Logger;
import com.google.gwt.animation.client.AnimationScheduler;
import com.google.gwt.animation.client.AnimationScheduler.AnimationCallback;
import com.google.gwt.animation.client.AnimationScheduler.AnimationHandle;
import com.google.gwt.core.client.Duration;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Touch;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.event.dom.client.TouchCancelEvent;
import com.google.gwt.event.dom.client.TouchEndEvent;
import com.google.gwt.event.dom.client.TouchEvent;
import com.google.gwt.event.dom.client.TouchMoveEvent;
import com.google.gwt.event.dom.client.TouchStartEvent;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.googlecode.mgwt.collection.shared.CollectionFactory;
import com.googlecode.mgwt.collection.shared.LightArray;
import com.googlecode.mgwt.collection.shared.LightArrayInt;
import com.googlecode.mgwt.dom.client.event.animation.TransitionEndEvent;
import com.googlecode.mgwt.dom.client.event.animation.TransitionEndHandler;
import com.googlecode.mgwt.dom.client.event.mouse.SimulatedTouchMoveEvent;
import com.googlecode.mgwt.dom.client.event.mouse.SimulatedTouchStartEvent;
import com.googlecode.mgwt.dom.client.event.mouse.TouchStartToMouseDownHandler;
import com.googlecode.mgwt.dom.client.event.orientation.OrientationChangeEvent;
import com.googlecode.mgwt.dom.client.event.orientation.OrientationChangeHandler;
import com.googlecode.mgwt.dom.client.event.touch.TouchHandler;
import com.googlecode.mgwt.ui.client.MGWT;
import com.googlecode.mgwt.ui.client.util.CssUtil;
import com.googlecode.mgwt.ui.client.widget.panel.scroll.BeforeScrollEndEvent;
import com.googlecode.mgwt.ui.client.widget.panel.scroll.BeforeScrollMoveEvent;
import com.googlecode.mgwt.ui.client.widget.panel.scroll.BeforeScrollStartEvent;
import com.googlecode.mgwt.ui.client.widget.panel.scroll.ScrollAnimationEndEvent;
import com.googlecode.mgwt.ui.client.widget.panel.scroll.ScrollAnimationMoveEvent;
import com.googlecode.mgwt.ui.client.widget.panel.scroll.ScrollAnimationStartEvent;
import com.googlecode.mgwt.ui.client.widget.panel.scroll.ScrollEndEvent;
import com.googlecode.mgwt.ui.client.widget.panel.scroll.ScrollMoveEvent;
import com.googlecode.mgwt.ui.client.widget.panel.scroll.ScrollPanelAppearance;
import com.googlecode.mgwt.ui.client.widget.panel.scroll.ScrollPanelAppearance.ScrollPanelCss;
import com.googlecode.mgwt.ui.client.widget.panel.scroll.ScrollRefreshEvent;
import com.googlecode.mgwt.ui.client.widget.panel.scroll.ScrollStartEvent;
import com.googlecode.mgwt.ui.client.widget.touch.TouchDelegate;
import com.googlecode.mgwt.ui.client.widget.touch.TouchSupport;

public class ScrollPanelTouchImpl extends ScrollPanelImpl {

	private enum DIRECTION {
		HORIZONTAL, VERTICAL
	}

	private static class Momentum {

		public static final Momentum ZERO_MOMENTUM = new Momentum(0, 0);

		private final int time;
		private final int dist;

		/**
		 *
		 */
		public Momentum(int dist, int time) {
			this.dist = dist;
			this.time = time;

		}

		public int getDist() {
			return dist;
		}

		public int getTime() {
			return time;
		}

	}

	private static class Snap {
		private final int x;
		private final int y;
		private final int time;

		public Snap(int x, int y, int time) {
			this.x = x;
			this.y = y;
			this.time = time;

		}

		public int getTime() {
			return time;
		}

		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}
	}

	private static class Step {
		private final int x;

		private final int y;
		private int time;

		public Step(int x, int y, int time) {
			this.x = x;
			this.y = y;
			this.time = time;

		}

		public int getTime() {
			return time;
		}

		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}

		public void setTime(int i) {
			time = 0;

		}

		@Override
		public String toString() {
			return "Step [x=" + x + ", y=" + y + ", time=" + time + "]";
		}

	}

	private class TouchListener implements TouchHandler {

		@Override
		public void onTouchCancel(TouchCancelEvent event) {
			if (!listenForCancelEvent) {
				return;
			}
			end(event);
		}

		@Override
		public void onTouchEnd(TouchEndEvent event) {
			if (!listenForEndEvent) {
				return;
			}
			end(event);

		}

		@Override
		public void onTouchMove(TouchMoveEvent event) {
			if (!listenForMoveEvent) {
				return;
			}
			move(event);

		}

		@Override
		public void onTouchStart(TouchStartEvent event) {
			if (!listenForStart) {
				return;
			}
			start(event);

		}

	}

	// TODO fix this by refactoring
	private static final ScrollPanelAppearance SPA = GWT.create(ScrollPanelAppearance.class);

	private static Logger logger = Logger.getLogger(ScrollPanelTouchImpl.class.getName());

	private static double ZOOM_MIN = 1;

	private static double ZOOM_MAX = 4;
	private boolean enabled;
	private int x;
	private int y;

	private LightArray<Step> steps;
	private LightArrayInt pagesX;

	private LightArrayInt pagesY;
	private SimplePanel wrapper;
	private Widget scroller;

	private double scale;
	private double zoomMin;
	private double zoomMax;
	private int wrapperHeight;

	private int wrapperWidth;
	// offset from top
	private int topOffset;
	private int minScrollY;
	private int scrollerWidth;
	private int scrollerHeight;
	private int maxScrollX;
	private int maxScrollY;
	private int dirX;

	private int dirY;
	// enable disable horizontal scroll
	private boolean hScroll;
	// enable disable vertical scroll
	private boolean vScroll;
	private boolean bounceLock;
	private boolean hScrollbar;
	private boolean vScrollbar;
	private int wrapperOffsetLeft;
	private int wrapperOffsetTop;
	private boolean moved;
	private boolean zoomed;
	private boolean animating;
	private boolean useTransform;
	private boolean zoom;
	private boolean useTransistion;
	private int distX;
	private int distY;
	private int absDistX;
	private int absDistY;
	private double touchesDistStart;
	private int originX;
	private int originY;
	private boolean momentum;
	private int absStartX;
	private int absStartY;
	private int startX;
	private int startY;
	private int pointX;
	private int pointY;
	private double startTime;
	private double touchesDist;
	private double lastScale;
	private boolean bounce;
	private double bounceFactor;
	private boolean lockDirection;
	private Timer doubleTapTimer;
	private boolean snap;
	private int snapThreshold;
	private boolean wheelActionZoom;
	private int wheelZoomCount;
	protected AnimationHandle aniTime;
	private int currPageX;
	private int currPageY;
	private String snapSelector;
	private TouchListener touchListener;

	private HandlerRegistration transistionEndRegistration;

	private boolean fixedScrollbar;

	private boolean hideScrollBar;

	private boolean fadeScrollBar;

	private ScrollPanelCss css;

	private boolean shouldHandleResize;

	private boolean[] scrollBar;

	private Element[] scrollBarWrapper;

	private Element[] scrollBarIndicator;

	private int[] scrollBarSize;

	private int[] scrollbarIndicatorSize;;

	private int[] scrollbarMaxScroll;
	private double[] scrollbarProp;
	private boolean hScrollDesired;

	private boolean vScrollDesired;
	private LightArrayInt pagesActualX;
	private LightArrayInt pagesActualY;
	private HandlerRegistration touchStartRegistration;

	private HandlerRegistration orientationChangeRegistration;

	private TouchDelegate touchDelegate;

	private HandlerRegistration mouseOutRegistration;

	private HandlerRegistration mouseWheelRegistration;

	private HandlerRegistration touchCancelRegistration;

	private HandlerRegistration touchEndRegistration;

	private HandlerRegistration touchMoveRegistration;

	private boolean listenForStart;

	private boolean listenForCancelEvent;

	private boolean listenForEndEvent;

	private boolean listenForMoveEvent;

	private int offsetMaxY;

	public ScrollPanelTouchImpl() {

		wrapper = new SimplePanel();
		touchDelegate = new TouchDelegate(wrapper);

		touchListener = new TouchListener();
		setupEvents();

		css = SPA.css();
		css.ensureInjected();

		wrapper.addStyleName(css.scrollPanel());

		initWidget(wrapper);

		shouldHandleResize = true;

		enabled = true;
		steps = CollectionFactory.constructArray();
		scale = 1.0;
		currPageX = 0;
		currPageY = 0;
		pagesX = CollectionFactory.constructIntegerArray();
		pagesY = CollectionFactory.constructIntegerArray();
		wheelZoomCount = 0;

		// setup events!

		// setting standard options
		hScroll = true;
		vScroll = true;
		hScrollDesired = true;
		vScrollDesired = true;
		x = 0;
		y = 0;
		bounce = true;
		bounceFactor = 2.0;
		bounceLock = false;
		momentum = true;
		lockDirection = true;
		setUseTransform(true);
		setUseTransistion(false);
		topOffset = 0;

		// Zoom
		setZoom(false);
		zoomMin = ZOOM_MIN;
		zoomMax = ZOOM_MAX;

		// snap
		snap = false;
		snapSelector = null;
		snapThreshold = 1;

		fixedScrollbar = MGWT.getOsDetection().isAndroid() && !MGWT.getOsDetection().isAndroid4_4_OrHigher();
		hideScrollBar = true;
		fadeScrollBar = (MGWT.getOsDetection().isIOs() || MGWT.getOsDetection().isWindowsPhone()) && CssUtil.has3d();

		// array for scrollbars
		scrollBar = new boolean[2];
		scrollBarWrapper = new Element[2];
		scrollBarIndicator = new Element[2];
		scrollBarSize = new int[2];
		scrollbarIndicatorSize = new int[2];
		scrollbarMaxScroll = new int[2];
		scrollbarProp = new double[2];

		scrollBar[DIRECTION.HORIZONTAL.ordinal()] = hScroll;
		scrollBar[DIRECTION.VERTICAL.ordinal()] = vScroll;
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

	public void disable() {
		stop();
		resetPos(0);
		enabled = false;

		unbindMoveEvent();
		unbindEndEvent();
		unbindCancelEvent();
	}

	public void enable() {
		enabled = true;
	}

	@Override
	public int getCurrentPageX() {
		return currPageX;
	}

	@Override
	public int getCurrentPageY() {
		return currPageY;
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
		return pagesActualX;
	}

	@Override
	public LightArrayInt getPagesY() {
		return pagesActualY;
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	public boolean isReady() {
		return !moved && !zoomed && !animating;
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

		if (scale < zoomMin) {
			scale = zoomMin;
		}
		wrapperHeight = getClientHeight(wrapper.getElement());
		if (wrapperHeight == 0) {
			wrapperHeight = 1;
		}
		wrapperWidth = getClientWidth(wrapper.getElement());
		if (wrapperWidth == 0) {
			wrapperWidth = 1;
		}

		minScrollY = -topOffset;

		scrollerWidth = (int) Math.round((scroller.getOffsetWidth() + getMarginWidth(scroller.getElement())) * scale);
		scrollerHeight = (int) Math.round((scroller.getOffsetHeight() + minScrollY + +getMarginHeight(scroller.getElement())) * scale);

		maxScrollX = wrapperWidth - scrollerWidth;

		maxScrollY = wrapperHeight - scrollerHeight + minScrollY + offsetMaxY;

		dirX = 0;
		dirY = 0;

		hScroll = (hScrollDesired && maxScrollX < 0);
		vScroll = vScrollDesired && (!bounceLock && !hScroll || scrollerHeight > wrapperHeight);

		hScrollbar = hScroll && hScrollbar;
		vScrollbar = vScroll && vScrollbar && scrollerHeight > wrapperHeight;

		int[] offSet = offSet(ScrollPanelTouchImpl.this.wrapper.getElement());

		wrapperOffsetLeft = -offSet[0];
		wrapperOffsetTop = -offSet[1];

		// prep stuff
		if (ScrollPanelTouchImpl.this.snapSelector != null) {
			ScrollPanelTouchImpl.this.pagesX = CollectionFactory.constructIntegerArray();
			ScrollPanelTouchImpl.this.pagesY = CollectionFactory.constructIntegerArray();

			ScrollPanelTouchImpl.this.pagesActualX = CollectionFactory.constructIntegerArray();
			ScrollPanelTouchImpl.this.pagesActualY = CollectionFactory.constructIntegerArray();

			JsArray<com.google.gwt.dom.client.Element> elements = querySelectorAll(ScrollPanelTouchImpl.this.scroller.getElement(), snapSelector);

			for (int i = 0 ; i < elements.length() ; i++) {
				int[] pos = offSet(elements.get(i));
				int left = pos[0] + ScrollPanelTouchImpl.this.wrapperOffsetLeft;
				int top = pos[1] + ScrollPanelTouchImpl.this.wrapperOffsetTop;
				ScrollPanelTouchImpl.this.pagesX.push((int) (left < ScrollPanelTouchImpl.this.maxScrollX ? ScrollPanelTouchImpl.this.maxScrollX : left * ScrollPanelTouchImpl.this.scale));
				ScrollPanelTouchImpl.this.pagesY.push((int) (top < ScrollPanelTouchImpl.this.maxScrollY ? ScrollPanelTouchImpl.this.maxScrollY : top * ScrollPanelTouchImpl.this.scale));

				ScrollPanelTouchImpl.this.pagesActualX.push((int) (left * ScrollPanelTouchImpl.this.scale));
				ScrollPanelTouchImpl.this.pagesActualY.push((int) (top * ScrollPanelTouchImpl.this.scale));
			}
		} else {
			if (ScrollPanelTouchImpl.this.snap) {
				int pos = 0;
				int page = 0;
				ScrollPanelTouchImpl.this.pagesX = CollectionFactory.constructIntegerArray();

				while (pos >= ScrollPanelTouchImpl.this.maxScrollX) {
					ScrollPanelTouchImpl.this.pagesX.set(page, pos);
					pos = pos - ScrollPanelTouchImpl.this.wrapperWidth;
					page++;
				}
				if (ScrollPanelTouchImpl.this.maxScrollX % ScrollPanelTouchImpl.this.wrapperWidth != 0) {
					ScrollPanelTouchImpl.this.pagesX.set(ScrollPanelTouchImpl.this.pagesX.length(), ScrollPanelTouchImpl.this.maxScrollX
							- ScrollPanelTouchImpl.this.pagesX.get(ScrollPanelTouchImpl.this.pagesX.length() - 1) + ScrollPanelTouchImpl.this.pagesX.get(ScrollPanelTouchImpl.this.pagesX.length() - 1));
				}

				pos = 0;
				page = 0;
				ScrollPanelTouchImpl.this.pagesY = CollectionFactory.constructIntegerArray();
				while (pos >= ScrollPanelTouchImpl.this.maxScrollY) {
					ScrollPanelTouchImpl.this.pagesY.set(page, pos);
					pos = pos - ScrollPanelTouchImpl.this.wrapperHeight;
					page++;
				}
				if (ScrollPanelTouchImpl.this.maxScrollY % ScrollPanelTouchImpl.this.wrapperHeight != 0) {
					ScrollPanelTouchImpl.this.pagesY.set(ScrollPanelTouchImpl.this.pagesY.length(), ScrollPanelTouchImpl.this.maxScrollY
							- ScrollPanelTouchImpl.this.pagesY.get(ScrollPanelTouchImpl.this.pagesY.length() - 1) + ScrollPanelTouchImpl.this.pagesY.get(ScrollPanelTouchImpl.this.pagesY.length() - 1));
				}

				ScrollPanelTouchImpl.this.pagesActualX = pagesX;
				ScrollPanelTouchImpl.this.pagesActualY = pagesY;
			}
		}

		scrollBar(DIRECTION.HORIZONTAL);
		scrollBar(DIRECTION.VERTICAL);

		if (!ScrollPanelTouchImpl.this.zoomed) {
			CssUtil.setTransitionDuration(ScrollPanelTouchImpl.this.scroller.getElement(), 0);
			resetPos(200);
		}

		updateDefaultStyles();

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
	public void scrollTo(int x, int y, int time) {
		scrollTo(x, y, time, false);

	}

	@Override
	public void scrollTo(int x, int y, int time, boolean relative) {
		scrollTo(x, y, time, relative, true);
	}

	public void scrollTo(int x, int y, int time, boolean relative, boolean issueEvent) {
		stop();

		int destX;
		int destY;

		if (relative) {
			destX = this.x - x;
			destY = this.y - y;
		} else {
			destX = x;
			destY = y;
		}

		Step step = new Step(destX, destY, time);

		steps.push(step);

		startAnimation(issueEvent);

	}

	public void scrollToElement(com.google.gwt.dom.client.Element el, int time) {

		int[] offSet = offSet(el);
		int left = offSet[0] + wrapperOffsetLeft;
		int top = offSet[1] + wrapperOffsetTop;

		left = left > 0 ? 0 : left < maxScrollX ? maxScrollX : left;
		top = top > minScrollY ? minScrollY : top < maxScrollY ? maxScrollY : top;

		scrollTo(left, top, time);
	}

	/*
	 * Helpers!
	 */

	public void scrollToPage(int pageX, int pageY) {
		scrollToPage(pageX, pageY, 400);
	}

	@Override
	public void scrollToPage(int pageX, int pageY, int time) {
		scrollToPage(pageX, pageY, time, true);
	}

	@Override
	public void scrollToPage(int pageX, int pageY, int time, boolean issueEvent) {
		if (issueEvent) {
			fireEvent(new ScrollStartEvent(null));
		}

		int x, y;
		if (snap || snapSelector != null) {

			pageX = pageX < 0 ? 0 : pageX > pagesX.length() - 1 ? pagesX.length() - 1 : pageX;
			pageY = pageY < 0 ? 0 : pageY > pagesY.length() - 1 ? pagesY.length() - 1 : pageY;

			currPageX = pageX;
			currPageY = pageY;
			x = pagesX.get(pageX);
			y = pagesY.get(pageY);
		} else {
			x = -wrapperWidth * pageX;
			y = -wrapperHeight * pageY;
			if (x < maxScrollX) {
				x = maxScrollX;
			}
			if (y < maxScrollY) {
				y = maxScrollY;
			}
		}

		scrollTo(x, y, time, false, issueEvent);
	}

	@Override
	public void setAutoHandleResize(boolean handle) {
		shouldHandleResize = handle;

	}

	/*
	 * GWT stuff
	 */

	@Override
	public void setBounce(boolean bounce) {
		this.bounce = bounce;

	}

	@Override
	public void setBounceFactor(double factor) {
		bounceFactor = factor;
	}

	@Override
	public void setHideScrollBar(boolean hideScrollBar) {
		this.hideScrollBar = hideScrollBar;
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
		this.momentum = momentum;

	}

	@Override
	public void setOffSetMaxY(int height) {
		offsetMaxY = height;

	}

	@Override
	public void setOffSetY(int y) {
		topOffset = y;

	}

	@Override
	public void setScrollingEnabledX(boolean scrollingEnabledX) {
		hScrollDesired = scrollingEnabledX;
		// this.hScroll = scrollingEnabledX;
		// this.scrollBar[DIRECTION.HORIZONTAL.ordinal()] = scrollingEnabledX;

	}

	@Override
	public void setScrollingEnabledY(boolean scrollingEnabledY) {
		vScrollDesired = scrollingEnabledY;
		// this.vScroll = scrollingEnabledY;
		// this.scrollBar[DIRECTION.VERTICAL.ordinal()] = scrollingEnabledY;
	}

	@Override
	public void setScrollLock(boolean lock) {
		lockDirection = lock;
	}

	@Override
	public void setShowHorizontalScrollBar(boolean show) {
		hScrollbar = show;
		scrollBar[DIRECTION.HORIZONTAL.ordinal()] = show;
	}

	@Deprecated
	@Override
	public void setShowScrollBarX(boolean show) {
		hScrollbar = show;
		scrollBar[DIRECTION.VERTICAL.ordinal()] = show;
	}

	@Deprecated
	@Override
	public void setShowScrollBarY(boolean show) {
		vScrollbar = show;
		scrollBar[DIRECTION.HORIZONTAL.ordinal()] = show;
	}

	@Override
	public void setShowVerticalScrollBar(boolean show) {
		vScrollbar = show;
		scrollBar[DIRECTION.VERTICAL.ordinal()] = show;
	}

	@Override
	public void setSnap(boolean snap) {
		this.snap = snap;

	}

	@Override
	public void setSnapSelector(String selector) {
		snapSelector = selector;

	}

	@Override
	public void setSnapThreshold(int threshold) {
		snapThreshold = threshold;

	}

	@Override
	public void setUsePos(boolean pos) {
		useTransform = !pos;
	}

	public void setUseTransform(boolean useTransform) {
		this.useTransform = CssUtil.hasTransform() && useTransform;
	}

	public void setUseTransistion(boolean useTransistion) {
		this.useTransistion = CssUtil.hasTransistionEndEvent() && useTransistion;
	}

	@Override
	public void setWidget(IsWidget child) {
		setWidget(child.asWidget() != null ? child.asWidget() : null);

	}

	@Override
	public void setWidget(Widget w) {
		// clear old event handlers
		unbindStartEvent();
		unbindResizeEvent();
		if (TouchSupport.isTouchEventsEmulatedUsingMouseEvents() || TouchSupport.isTouchEventsEmulatedUsingPointerEvents()) {
			unbindMouseoutEvent();
			unbindMouseWheelEvent();
		}

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
				bindResizeEvent();
				bindStartEvent();
				if (TouchSupport.isTouchEventsEmulatedUsingMouseEvents() || TouchSupport.isTouchEventsEmulatedUsingPointerEvents()) {
					bindMouseoutEvent();
					bindMouseWheelEvent();
				}
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {

					@Override
					public void execute() {
						refresh();
					}
				});

			}

		}

	}

	public void setZoom(boolean zoom) {
		this.zoom = zoom && useTransform;
	}

	public void stop() {
		if (useTransistion) {
			unbindTransistionEnd();
		} else {
			if (aniTime != null) {
				aniTime.cancel();
			}
		}

		steps = CollectionFactory.constructArray();
		moved = false;
		animating = false;
	}

	public void zoom(int x, int y, double scale, int time) {

		if (!useTransform) {
			return;
		}

		double relScale = scale / this.scale;

		zoomed = true;

		x = x - wrapperOffsetLeft - this.x;
		y = y - wrapperOffsetTop - this.y;

		this.x = (int) Math.round(x - x * relScale + this.x);
		this.y = (int) Math.round(y - y * relScale + this.y);

		this.scale = scale;
		refresh();

		this.x = this.x > 0 ? 0 : this.x < maxScrollX ? maxScrollX : this.x;
		this.y = this.y > minScrollY ? minScrollY : this.y < maxScrollY ? maxScrollY : this.y;

		CssUtil.setTransitionDuration(scroller.getElement(), time);
		CssUtil.setTranslateAndZoom(scroller.getElement(), x, y, scale);
		zoomed = true;

	}

	@Override
	protected void onAttach() {
		super.onAttach();

		if (scroller != null) {

			// bind events
			bindResizeEvent();
			bindStartEvent();
			if (TouchSupport.isTouchEventsEmulatedUsingMouseEvents() || TouchSupport.isTouchEventsEmulatedUsingPointerEvents()) {
				bindMouseoutEvent();
				bindMouseWheelEvent();
			}
			Scheduler.get().scheduleDeferred(new ScheduledCommand() {

				@Override
				public void execute() {
					refresh();

				}
			});

		}

	}

	@Override
	protected void onDetach() {
		super.onDetach();
		unbindResizeEvent();
	}

	private void bindCancelEvent() {
		listenForCancelEvent = true;
	}

	private void bindEndEvent() {
		listenForEndEvent = true;

	}

	private void bindMouseoutEvent() {
		mouseOutRegistration = wrapper.addDomHandler(new MouseOutHandler() {

			@Override
			public void onMouseOut(MouseOutEvent event) {
				mouseOut(event);

			}
		}, MouseOutEvent.getType());

	}

	private void bindMouseWheelEvent() {
		mouseWheelRegistration = scroller.addDomHandler(new MouseWheelHandler() {

			@Override
			public void onMouseWheel(MouseWheelEvent event) {
				int wheelDeltaX = 0;
				int wheelDeltaY = 0;

				if (isScrollingEnabledX()) {
					wheelDeltaX = getMouseWheelVelocityX(event.getNativeEvent()) / 10;
				}

				if (isScrollingEnabledY()) {
					wheelDeltaY = getMouseWheelVelocityY(event.getNativeEvent()) / 10;
				}
				wheel(wheelDeltaX, wheelDeltaY, event);

			}
		}, MouseWheelEvent.getType());

	}

	private void bindMoveEvent() {
		listenForMoveEvent = true;
	}

	/**
	 *
	 */
	private void bindResizeEvent() {
		if (!MGWT.getFormFactor().isDesktop()) {
			orientationChangeRegistration = MGWT.addOrientationChangeHandler(new OrientationChangeHandler() {

				@Override
				public void onOrientationChanged(OrientationChangeEvent event) {
					if (shouldHandleResize) {
						resize();
					}

				}
			});
		} else {
			orientationChangeRegistration = Window.addResizeHandler(new ResizeHandler() {

				@Override
				public void onResize(ResizeEvent event) {
					if (shouldHandleResize) {
						resize();
					}

				}
			});
		}

	}

	private void bindStartEvent() {
		listenForStart = true;
	}

	private void bindTransistionEndEvent(final boolean issueEvent) {
		if (CssUtil.hasTransistionEndEvent()) {
			transistionEndRegistration = scroller.addDomHandler(new TransitionEndHandler() {

				@Override
				public void onTransitionEnd(TransitionEndEvent event) {
					onTransistionEnd(event, issueEvent);

				}
			}, TransitionEndEvent.getType());
		}

	}

	private void cancelAnimationFrame() {
		if (aniTime != null) {
			aniTime.cancel();
			aniTime = null;
		}

	}

	private void checkDOMChanges() {
		if (moved || zoomed || animating || (Math.abs(scrollerWidth - scroller.getOffsetWidth() * scale) < 0.01 && Math.abs(scrollerHeight - scroller.getOffsetHeight() * scale) < 0.01)) {
			return;
		}

		refresh();
	}

	private void end(final TouchEvent<?> event) {
		if (event != null && event.getTouches().length() != 0) {
			return;
		}

		double duration = Duration.currentTimeMillis() - startTime;
		int newPosX = x;
		int newPosY = y;
		Momentum momentumX = Momentum.ZERO_MOMENTUM;
		Momentum momentumY = Momentum.ZERO_MOMENTUM;

		unbindMoveEvent();
		unbindEndEvent();
		unbindCancelEvent();

		// fire on before scroll end
		fireEvent(new BeforeScrollEndEvent(event));

		if (zoomed) {
			double scale = this.scale * lastScale;
			scale = Math.max(zoomMin, scale);
			scale = Math.min(zoomMax, scale);
			lastScale = scale / this.scale;

			x = (int) Math.round(originX - originX * lastScale + x);
			y = (int) Math.round(originY - originY * lastScale + y);

			CssUtil.setTransitionDuration(scroller.getElement(), 200);
			CssUtil.setTranslateAndZoom(scroller.getElement(), x, y, this.scale);

			zoomed = false;
			refresh();

			// TODO fire onzoomend

			return;
		}

		if (!moved) {
			if (doubleTapTimer != null && zoom) {
				doubleTapTimer.cancel();
				doubleTapTimer = null;

				// TODO fire on zoom start

				// TODO fire zoom end after duration
			} else {
				doubleTapTimer = new Timer() {

					@Override
					public void run() {
						doubleTapTimer = null;

						// TODO dispatch tap event

					}
				};
				doubleTapTimer.schedule(zoom ? 250 : 1);
			}

			resetPos(200);

			// TODO fire touchend!
			return;
		}

		if (duration < 300 && momentum) {
			if (newPosX != 0) {
				momentumX = momentum(newPosX - startX, duration, -x, scrollerWidth - wrapperWidth + x, bounce ? wrapperWidth : 0);
			}
			if (newPosY != 0) {
				momentumY = momentum(newPosY - startY, duration, -y, (maxScrollY < 0 ? scrollerHeight - wrapperHeight + y - minScrollY : 0), bounce ? wrapperHeight : 0);
			}

			newPosX = x + momentumX.getDist();
			newPosY = y + momentumY.getDist();

			if ((x > 0 && newPosX > 0) || (x < maxScrollX && newPosX < maxScrollX)) {
				momentumX = Momentum.ZERO_MOMENTUM;
			}

			if ((y > minScrollY && newPosY > minScrollY) || (y < maxScrollY && newPosY < maxScrollY)) {
				momentumY = Momentum.ZERO_MOMENTUM;
			}
		}

		int distX = 0;
		int distY = 0;

		if (momentumX.getDist() != 0 || momentumY.getDist() != 0) {

			int newDuration = Math.max(Math.max(momentumX.getTime(), momentumY.getTime()), 10);

			if (snap) {
				distX = newPosX - absStartX;
				distY = newPosY - absStartY;

				if (Math.abs(distX) < snapThreshold && Math.abs(distY) < snapThreshold) {
					scrollTo(absStartX, absStartY, 200);
				} else {
					Snap snap = snap(newPosX, newPosY);
					newPosX = snap.getX();
					newPosY = snap.getY();
					newDuration = Math.max(snap.getTime(), newDuration);
				}
			}

			scrollTo(newPosX, newPosY, newDuration);

			// TODO fire touch end!
			return;
		}

		if (snap) {
			distX = newPosX - absStartX;
			distY = newPosY - absStartY;

			if (Math.abs(distX) < snapThreshold && Math.abs(distY) < snapThreshold) {
				scrollTo(absStartX, absStartY, 200);
			} else {
				Snap snap = snap(x, y);
				if (snap.x != x || snap.y != y) {
					scrollTo(snap.x, snap.y, snap.time);
				}
			}

			// fire on touch end
			return;
		}

		resetPos(200);
		// TODO fire on touch end
	}

	// TODO move in util
	private native int getClientHeight(Element element)/*-{
																		return element.clientHeight || 0;
																		}-*/;

	private native int getClientWidth(Element element) /*-{
																		return element.clientWidth || 0;
																		}-*/;

	private native int getMarginHeight(Element el)/*-{

																	var top = 0;
																	var bottom = 0;
																	var style = $wnd.getComputedStyle(el);

																	top = parseInt(style.marginTop, 10) || 0;
																	bottom = parseInt(style.marginBottom, 10) || 0;

																	return top + bottom;
																	}-*/;

	private native int getMarginWidth(Element el)/*-{
																var left = 0;
																var right = 0;
																var style = $wnd.getComputedStyle(el);

																left = parseInt(style.marginLeft, 10) || 0;
																right = parseInt(style.marginRight, 10) || 0;

																return left + right;
																}-*/;

	private native int getMouseWheelVelocityX(NativeEvent evt)/*-{
																					return Math.round(evt.wheelDeltaX) || 0;
																					}-*/;

	private native int getMouseWheelVelocityY(NativeEvent evt)/*-{

																					var val = (evt.detail * 40) || evt.wheelDeltaY || 0;
																					return Math.round(val);
																					}-*/;

	private Momentum momentum(int dist, double time, int maxDistUpper, int maxDistLower, int size) {
		double deceleration = 0.0006;
		double speed = ((Math.abs(dist))) / time;
		double newDist = (speed * speed) / (2 * deceleration);
		double newTime = 0;
		double outSideDist = 0;

		// Proportinally reduce speed if we are outside of the boundaries
		if (dist > 0 && newDist > maxDistUpper) {
			outSideDist = size / (6 / (newDist / speed * deceleration));
			maxDistUpper = (int) (maxDistUpper + outSideDist);
			speed = speed * maxDistUpper / newDist;
			newDist = maxDistUpper;
		} else if (dist < 0 && newDist > maxDistLower) {
			outSideDist = size / (6 / (newDist / speed * deceleration));
			maxDistLower = (int) (maxDistLower + outSideDist);
			speed = speed * maxDistLower / newDist;
			newDist = maxDistLower;
		}

		newDist = newDist * (dist < 0 ? -1 : 1);
		newTime = speed / deceleration;

		return new Momentum((int) Math.round(newDist), (int) Math.round(newTime));
	}

	private void mouseOut(MouseOutEvent event) {

		EventTarget relatedTarget = event.getRelatedTarget();

		if (relatedTarget == null) {

			end(null);
			return;
		}

		if (!Node.is(relatedTarget)) {
			end(null);
			return;
		}

		Node tmp = relatedTarget.cast();

		while (true) {

			tmp = tmp.getParentNode();

			if (tmp == wrapper.getElement()) {
				return;
			}

			if (tmp == null) {
				break;
			}

		}

		end(null);

	}

	private void move(TouchMoveEvent event) {
		// old android needs to prevent default on move
		if (MGWT.getOsDetection().isAndroid()) {
			event.preventDefault();
		}

		JsArray<Touch> touches = event.getTouches();
		int deltaX = touches.get(0).getPageX() - pointX;
		int deltaY = touches.get(0).getPageY() - pointY;
		int newX = x + deltaX;
		int newY = y + deltaY;
		double timeStamp = Duration.currentTimeMillis();

		// fire onbeforescroll event
		fireEvent(new BeforeScrollMoveEvent(event));

		if (zoom && touches.length() > 1) {
			int c1 = Math.abs(touches.get(0).getPageX() - touches.get(1).getPageX());
			int c2 = Math.abs(touches.get(0).getPageY() - touches.get(1).getPageY());
			touchesDist = Math.sqrt(c1 * c1 + c2 * c2);
			zoomed = true;

			double scale = 1 / touchesDistStart * touchesDist * this.scale;
			if (scale < zoomMin) {
				scale = 0.5 * zoomMin * Math.pow(2.0, scale / zoomMin);
			} else {
				if (scale > zoomMax) {
					scale = 2.0 * zoomMax * Math.pow(0.5, zoomMax / scale);
				}
			}
			lastScale = scale / this.scale;

			newX = (int) Math.round(originX - originX * lastScale + x);
			newY = (int) Math.round(originY - originY * lastScale + y);

			CssUtil.setTranslateAndZoom(scroller.getElement(), newX, newY, scale);

			// TODO call on zoom
			return;
		}

		pointX = touches.get(0).getPageX();
		pointY = touches.get(0).getPageY();

		// slower outside the bounds!
		if (newX > 0 || newX < maxScrollX) {
			if (bounce) {
				newX = (int) (x + Math.round(deltaX / bounceFactor));
			} else {
				if (newX >= 0 || maxScrollX >= 0) {
					newX = 0;
				} else {
					newX = maxScrollX;
				}
			}
		}

		if (newY > minScrollY || newY < maxScrollY) {
			if (bounce) {
				newY = (int) (y + Math.round(deltaY / bounceFactor));
			} else {
				if (newY >= minScrollY || maxScrollY >= 0) {
					newY = minScrollY;
				} else {
					newY = maxScrollY;
				}
			}
		}

		distX += deltaX;
		distY += deltaY;
		absDistX = Math.abs(distX);
		absDistY = Math.abs(distY);

		if (absDistX < 6 && absDistY < 6) {
			return;
		}

		if (lockDirection) {
			if (absDistX > absDistY + 5) {
				newY = y;
				deltaY = 0;
			} else {
				if (absDistY > absDistX + 5) {
					newX = x;
					deltaX = 0;
				}
			}
		}

		moved = true;
		pos(newX, newY);

		dirX = deltaX > 0 ? -1 : deltaX < 0 ? 1 : 0;
		dirY = deltaY > 0 ? -1 : deltaY < 0 ? 1 : 0;

		if (timeStamp - startTime > 300) {
			startTime = timeStamp;
			startX = x;
			startY = y;
		}

		if (dirX != 0 && isScrollingEnabledX() || dirY != 0 && isScrollingEnabledY()) {
			fireEvent(new ScrollMoveEvent(event));
		}
	}

	private int[] offSet(com.google.gwt.dom.client.Element el) {
		int left = -el.getOffsetLeft();
		int top = -el.getOffsetTop();

		com.google.gwt.dom.client.Element domElem = el;
		while (true) {
			domElem = domElem.getOffsetParent();
			if (domElem == null) {
				break;
			}
			left -= domElem.getOffsetLeft();
			top -= domElem.getOffsetTop();
		}

		if (el != wrapper.getElement()) {
			left *= scale;
			top *= scale;
		}

		return new int[] { left, top };
	}

	private void onTransistionEnd(TransitionEndEvent event, boolean issueEvent) {
		EventTarget eventTarget = event.getNativeEvent().getEventTarget();
		if (Node.is(eventTarget)) {
			if (Element.is(eventTarget)) {
				Element target = eventTarget.cast();
				Element scrollerElement = scroller.getElement();
				// reference id should be okay according to
				// http://google-web-toolkit.googlecode.com/svn/javadoc/latest/com/google/gwt/user/client/DOM.html#compare(com.google.gwt.user.client.Element,
				// com.google.gwt.user.client.Element)
				if (target != scrollerElement) {
					return;
				}

			}
		}

		unbindTransistionEnd();

		startAnimation(issueEvent);

	}

	private void pos(int x, int y) {

		x = hScroll ? x : 0;
		y = vScroll ? y : 0;

		if (useTransform) {
			CssUtil.translate(scroller.getElement(), x, y);
		} else {
			// TODO
			scroller.getElement().getStyle().setLeft(x, Unit.PX);
			scroller.getElement().getStyle().setTop(y, Unit.PX);
		}

		this.x = x;
		this.y = y;

		scrollbarPos(DIRECTION.HORIZONTAL, false);
		scrollbarPos(DIRECTION.VERTICAL, false);
	}

	private native JsArray<com.google.gwt.dom.client.Element> querySelectorAll(Element el, String selector)/*-{
																																				return el.querySelectorAll(selector);
																																				}-*/;

	private void resetPos(int time) {

		int resetX = x >= 0 ? 0 : x < maxScrollX ? maxScrollX : x;

		int resetY = y >= minScrollY || maxScrollY > 0 ? minScrollY : y < maxScrollY ? maxScrollY : y;

		if (resetX == x && resetY == y) {
			if (moved) {
				moved = false;
				// fire on scroll end
				fireEvent(new ScrollEndEvent());
			}

			if (scrollBar[DIRECTION.HORIZONTAL.ordinal()] && hideScrollBar) {
				CssUtil.setTransitionsDelay(scrollBarWrapper[DIRECTION.HORIZONTAL.ordinal()], 300);
				CssUtil.setOpacity(scrollBarWrapper[DIRECTION.HORIZONTAL.ordinal()], 0);
			}

			if (scrollBar[DIRECTION.VERTICAL.ordinal()] && hideScrollBar) {
				CssUtil.setTransitionsDelay(scrollBarWrapper[DIRECTION.VERTICAL.ordinal()], 300);
				CssUtil.setOpacity(scrollBarWrapper[DIRECTION.VERTICAL.ordinal()], 0);
			}
			return;
		}

		scrollTo(resetX, resetY, time);
	}

	private void resize() {
		int delay = MGWT.getOsDetection().isAndroid() ? 200 : 1;
		new Timer() {
			@Override
			public void run() {
				refresh();
			}
		}.schedule(delay);
	}

	private void scrollBar(final DIRECTION direction) {
		final int dir = direction.ordinal();

		if (!scrollBar[dir]) {
			if (scrollBarWrapper[dir] != null) {
				if (CssUtil.hasTransform()) {
					CssUtil.resetTransForm(scrollBarIndicator[dir]);
				}

				if (scrollBarWrapper[dir].getParentNode() != null) {
					scrollBarWrapper[dir].getParentNode().removeChild(scrollBarWrapper[dir]);
				}

				scrollBarWrapper[dir] = null;
				scrollBarIndicator[dir] = null;
			}
			return;
		}

		Element bar;

		if (scrollBarWrapper[dir] == null) {
			// Create the scrollbar wrapper
			bar = DOM.createDiv();

			CssUtil.setTransitionDuration(bar, (fadeScrollBar ? 350 : 0));
			bar.getStyle().setOpacity(hideScrollBar ? 0 : 1);

			scrollBarWrapper[dir] = bar;
			scrollBarWrapper[dir].addClassName(css.scrollBar());
			if (direction == DIRECTION.HORIZONTAL) {
				scrollBarWrapper[dir].addClassName(css.scrollBarHorizontal());
				scrollBarWrapper[dir].getStyle().setRight(scrollBar[DIRECTION.VERTICAL.ordinal()] ? 7 : 2, Unit.PX);
			} else {
				scrollBarWrapper[dir].addClassName(css.scrollBarVertical());
				scrollBarWrapper[dir].getStyle().setBottom(scrollBar[DIRECTION.HORIZONTAL.ordinal()] ? 7 : 2, Unit.PX);
			}

			// Create the scrollbar indicator
			bar = DOM.createDiv();
			bar.addClassName(css.scrollBarBar());

			if (direction == DIRECTION.HORIZONTAL) {
				bar.getStyle().setHeight(100, Unit.PCT);
			} else {
				bar.getStyle().setWidth(100, Unit.PCT);
			}

			scrollBarWrapper[dir].appendChild(bar);
			scrollBarIndicator[dir] = bar;
			scrollBarIndicator[dir].addClassName(css.scrollBarBar());
		}

		// only append if size fits!
		if (direction == DIRECTION.HORIZONTAL) {
			if (wrapperWidth < scrollerWidth) {
				wrapper.getElement().appendChild(scrollBarWrapper[dir]);

			}
		} else {
			if (wrapperHeight < scrollerHeight) {
				wrapper.getElement().appendChild(scrollBarWrapper[dir]);
			}
		}

		int delay = MGWT.getOsDetection().isAndroid() ? 200 : 1;
		new Timer() {
			@Override
			public void run() {
				switch (direction) {
				case HORIZONTAL:
					scrollBarSize[dir] = scrollBarWrapper[dir].getClientWidth();
					scrollbarIndicatorSize[dir] = (int) Math.max(Math.round((double) (scrollBarSize[dir] * scrollBarSize[dir]) / scrollerWidth), 8);
					scrollBarIndicator[dir].getStyle().setWidth(scrollbarIndicatorSize[dir], Unit.PX);

					scrollbarMaxScroll[dir] = scrollBarSize[dir] - scrollbarIndicatorSize[dir];
					scrollbarProp[dir] = ((double) (scrollbarMaxScroll[dir])) / maxScrollX;
					break;
				case VERTICAL:
					scrollBarSize[dir] = scrollBarWrapper[dir].getClientHeight();

					scrollbarIndicatorSize[dir] = (int) Math.max(Math.round((double) (scrollBarSize[dir] * scrollBarSize[dir]) / scrollerHeight), 8);
					scrollBarIndicator[dir].getStyle().setHeight(scrollbarIndicatorSize[dir], Unit.PX);
					scrollbarMaxScroll[dir] = scrollBarSize[dir] - scrollbarIndicatorSize[dir];
					scrollbarProp[dir] = ((double) (scrollbarMaxScroll[dir])) / maxScrollY;

					break;

				default:
					break;
				}

				// Reset position
				scrollbarPos(direction, true);
			}
		}.schedule(delay);
	}

	private void scrollbarPos(DIRECTION direction, boolean hidden) {

		double pos = direction == DIRECTION.HORIZONTAL ? x : y;
		int size;
		int dir = direction.ordinal();

		if (!scrollBar[dir]) {
			return;
		}

		pos = scrollbarProp[dir] * pos;

		if (pos < 0) {
			if (!fixedScrollbar) {
				size = (int) (scrollbarIndicatorSize[dir] + Math.round(pos * 3));
				if (size < 8) {
					size = 8;
				}
				if (direction == DIRECTION.HORIZONTAL) {
					scrollBarIndicator[dir].getStyle().setWidth(size, Unit.PX);
				} else {
					scrollBarIndicator[dir].getStyle().setHeight(size, Unit.PX);
				}
			}
			pos = 0;
		} else {
			if (pos > scrollbarMaxScroll[dir]) {
				if (!fixedScrollbar) {
					size = (int) (scrollbarIndicatorSize[dir] - Math.round((pos - scrollbarMaxScroll[dir]) * 3));

					if (size < 8) {
						size = 8;
					}

					if (direction == DIRECTION.HORIZONTAL) {
						scrollBarIndicator[dir].getStyle().setWidth(size, Unit.PX);
					} else {
						scrollBarIndicator[dir].getStyle().setHeight(size, Unit.PX);
					}
					pos = scrollbarMaxScroll[dir] + (scrollbarIndicatorSize[dir] - size);

				} else {
					pos = scrollbarMaxScroll[dir];
				}
			}
		}

		CssUtil.setTransitionsDelay(scrollBarWrapper[dir], 0);
		CssUtil.setOpacity(scrollBarWrapper[dir], hidden && hideScrollBar ? 0 : 1);
		if (direction == DIRECTION.HORIZONTAL) {
			CssUtil.translate(scrollBarIndicator[dir], (int) pos, 0);
		} else {
			CssUtil.translate(scrollBarIndicator[dir], 0, (int) pos);
		}
	}

	private void setTransistionTime(int time) {

		CssUtil.setTransitionDuration(scroller.getElement(), time);

		if (vScrollbar) {
			CssUtil.setTransitionDuration(scrollBarIndicator[DIRECTION.VERTICAL.ordinal()], time);
		}

		if (hScrollbar) {
			CssUtil.setTransitionDuration(scrollBarIndicator[DIRECTION.HORIZONTAL.ordinal()], time);
		}

	}

	private void setupEvents() {
		touchMoveRegistration = touchDelegate.addTouchMoveHandler(touchListener);
		touchStartRegistration = touchDelegate.addTouchStartHandler(touchListener);
		touchCancelRegistration = touchDelegate.addTouchCancelHandler(touchListener);
		touchEndRegistration = touchDelegate.addTouchEndHandler(touchListener);

	}

	private Snap snap(int x, int y) {

		// Check page X
		int page = pagesX.length() - 1;
		for (int i = 0, l = pagesX.length() ; i < l ; i++) {
			if (x >= pagesX.get(i)) {
				page = i;
				break;
			}
		}
		if (page == currPageX && page > 0 && dirX < 0) {
			page--;
		}
		x = pagesX.get(page);
		int sizeX = Math.abs(x - pagesX.get(currPageX));
		sizeX = sizeX != 0 ? Math.abs(this.x - x) / sizeX * 500 : 0;
		currPageX = page;

		// Check page Y
		page = pagesY.length() - 1;
		for (int i = 0 ; i < page ; i++) {
			if (y >= pagesY.get(i)) {
				page = i;
				break;
			}
		}
		if (page == currPageY && page > 0 && dirY < 0) {
			page--;
		}
		y = pagesY.get(page);
		int sizeY = Math.abs(y - pagesY.get(currPageY));
		sizeY = sizeY != 0 ? Math.abs(this.y - y) / sizeY * 500 : 0;
		currPageY = page;

		// Snap with constant speed (proportional duration)
		int time = Math.max(sizeX, sizeY);
		if (time == 0) {
			time = 200;
		}

		return new Snap(x, y, time);
	}

	private void start(TouchStartEvent event) {
		int x, y;
		if (!enabled) {
			return;
		}

		fireEvent(new BeforeScrollStartEvent(event));

		if (useTransistion || zoom) {
			setTransistionTime(0);
		}

		moved = false;
		animating = false;
		zoomed = false;
		distX = 0;
		distY = 0;
		absDistX = 0;
		absDistY = 0;
		dirX = 0;
		dirY = 0;

		JsArray<Touch> touches = event.getTouches();

		if (zoom && touches.length() > 1) {
			int c1 = Math.abs(touches.get(0).getPageX() - touches.get(1).getPageX());
			int c2 = Math.abs(touches.get(0).getPageY() - touches.get(1).getPageY());
			touchesDistStart = Math.sqrt(c1 * c1 + c2 * c2);

			originX = Math.abs(touches.get(0).getPageX() + touches.get(1).getPageX() - wrapperOffsetLeft * 2 / 2 - this.x);
			originY = Math.abs(touches.get(0).getPageY() + touches.get(1).getPageY() - wrapperOffsetTop * 2 / 2 - this.y);

			// TODO call on zoom start
		}

		if (momentum) {
			if (useTransform) {
				int[] pos = CssUtil.getPositionFromTransForm(scroller.getElement());
				x = pos[0];
				y = pos[1];
			} else {
				x = CssUtil.getLeftPositionFromCssPosition(scroller.getElement());
				y = CssUtil.getTopPositionFromCssPosition(scroller.getElement());
			}

			if (x != this.x || y != this.y) {
				if (useTransistion) {
					unbindTransistionEnd();
				} else {
					cancelAnimationFrame();
				}
				steps = CollectionFactory.constructArray();
				pos(x, y);
			}
		}

		absStartX = this.x;
		absStartY = this.y;

		startX = this.x;
		startY = this.y;
		pointX = touches.get(0).getPageX();
		pointY = touches.get(0).getPageY();

		startTime = System.currentTimeMillis();

		fireEvent(new ScrollStartEvent(event));

		bindMoveEvent();
		bindEndEvent();
		bindCancelEvent();
	}

	private void startAnimation(final boolean issueEvent) {
		if (animating) {
			return;
		}

		final int startX = x;
		final int startY = y;

		if (steps.length() == 0) {
			resetPos(400);
			return;
		}
		fireEvent(new ScrollAnimationStartEvent());
		final Step step = steps.shift();

		if (step.getX() == startX && step.getY() == startY) {
			step.setTime(0);
		}

		animating = true;
		moved = true;

		if (useTransistion) {
			setTransistionTime(step.getTime());
			pos(step.getX(), step.getY());
			animating = false;
			if (step.getTime() != 0) {
				bindTransistionEndEvent(issueEvent);
			} else {
				resetPos(0);
			}
			return;
		}

		final double startTime = Duration.currentTimeMillis();

		final AnimationCallback animationCallback = new AnimationCallback() {

			@Override
			public void execute(double now) {

				if (now >= startTime + step.getTime()) {
					ScrollPanelTouchImpl.this.pos(step.x, step.y);
					animating = false;
					if (issueEvent) {
						fireEvent(new ScrollAnimationEndEvent());
					}
					ScrollPanelTouchImpl.this.startAnimation(issueEvent);
					return;
				}

				now = (now - startTime) / step.getTime() - 1;
				double easeOut = Math.sqrt(1 - now * now);
				int newX = (int) Math.round((step.getX() - startX) * easeOut + startX);
				int newY = (int) Math.round((step.getY() - startY) * easeOut + startY);
				ScrollPanelTouchImpl.this.pos(newX, newY);
				fireEvent(new ScrollAnimationMoveEvent());
				if (animating) {
					aniTime = AnimationScheduler.get().requestAnimationFrame(this);
				}

			}
		};

		animationCallback.execute(startTime);

	}

	private void unbindCancelEvent() {
		listenForCancelEvent = false;

	}

	private void unbindEndEvent() {
		listenForEndEvent = false;
	}

	private void unbindMouseoutEvent() {
		if (mouseOutRegistration != null) {
			mouseOutRegistration.removeHandler();
			mouseOutRegistration = null;
		}

	}

	private void unbindMouseWheelEvent() {
		if (mouseWheelRegistration != null) {
			mouseWheelRegistration.removeHandler();
			mouseWheelRegistration = null;
		}

	}

	private void unbindMoveEvent() {
		listenForMoveEvent = false;
	}

	private void unbindResizeEvent() {
		if (orientationChangeRegistration != null) {
			orientationChangeRegistration.removeHandler();
			orientationChangeRegistration = null;
		}
	}

	private void unbindStartEvent() {
		listenForStart = false;

	}

	private void unbindTransistionEnd() {
		if (transistionEndRegistration != null) {
			transistionEndRegistration.removeHandler();
			transistionEndRegistration = null;
		}

	}

	private void updateDefaultStyles() {
		if (scroller != null) {

			CssUtil.setTransistionProperty(scroller.getElement(), useTransform ? CssUtil.getTransformProperty() : "top left");
			CssUtil.setTransitionDuration(scroller.getElement(), 0);
			CssUtil.setTransFormOrigin(scroller.getElement(), 0, 0);
			if (useTransistion) {
				CssUtil.setTransistionTimingFunction(scroller.getElement(), "cubic-bezier(0.33,0.66,0.66,1)");
			}
			if (useTransform) {
				CssUtil.translate(scroller.getElement(), x, y);
			} else {
				scroller.getElement().getStyle().setPosition(Position.ABSOLUTE);
				scroller.getElement().getStyle().setLeft(x, Unit.PX);
				scroller.getElement().getStyle().setTop(y, Unit.PX);

			}

			if (useTransistion) {
				fixedScrollbar = true;
			}

		}

	}

	private void wheel(int wheelDeltaX, int wheelDeltaY, MouseWheelEvent event) {

		int pageX = event.getClientX();
		int pageY = event.getClientY();

		if (wheelActionZoom) {
			double deltaScale = scale * Math.pow(2, 1.0 / 3 * (wheelDeltaY != 0 ? wheelDeltaY / Math.abs(wheelDeltaY) : 0));
			if (deltaScale < zoomMin) {
				deltaScale = zoomMin;
			}
			if (deltaScale > zoomMax) {
				deltaScale = zoomMax;
			}

			if (Math.abs(deltaScale - scale) < 0.00001) {
				if (wheelZoomCount == 0) {
					// TODO maybe fire on zoom start
				}
				wheelZoomCount++;

				zoom(pageX, pageY, deltaScale, 400);

				new Timer() {

					@Override
					public void run() {
						wheelZoomCount--;
						if (wheelZoomCount == 0) {
							// TODO maybe fire zoom end
						}
					}

				}.schedule(400);
			}
			return;
		}

		int deltaX = x + wheelDeltaX;
		int deltaY = y + wheelDeltaY;

		if (deltaX > 0) {
			deltaX = 0;
		} else if (deltaX < maxScrollX) {
			deltaX = maxScrollX;
		}

		if (deltaY > minScrollY) {
			deltaY = minScrollY;
		} else if (deltaY < maxScrollY) {
			deltaY = maxScrollY;
		}

		SimulatedTouchStartEvent simulatedTouchStartEvent = new SimulatedTouchStartEvent(x, y, x + getAbsoluteLeft(), y + getAbsoluteTop(), TouchStartToMouseDownHandler.lastTouchId,
				event.getNativeEvent(), this);
		fireEvent(new ScrollStartEvent(simulatedTouchStartEvent));

		scrollTo(deltaX, deltaY, 0);

		SimulatedTouchMoveEvent simulatedTouchMoveEvent = new SimulatedTouchMoveEvent(deltaX, deltaY, deltaX + getAbsoluteLeft(), deltaY + getAbsoluteTop(), TouchStartToMouseDownHandler.lastTouchId,
				event.getNativeEvent(), this);
		fireEvent(new ScrollMoveEvent(simulatedTouchMoveEvent));

		fireEvent(new ScrollEndEvent());
	}
}
