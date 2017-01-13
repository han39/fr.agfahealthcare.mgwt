package com.googlecode.mgwt.ui.client.widget.touch;

import com.google.gwt.core.shared.GWT;

public abstract class TouchSupport {
  
  private static TouchSupport impl = GWT.create(TouchSupport.class);

  private static final boolean hasTouchSupport;
  
  static {
    hasTouchSupport = hasTouch();
  }

  /**
   * If true this means the client could support touch but
   * the device it runs on may not support touch.
   * If false then it means the client does not support touch.
   * @return
   */
  private static native boolean hasTouch() /*-{
    return 'ontouchstart' in $doc.documentElement;
  }-*/;

  protected abstract boolean _isTouchEventsEmulatedUsingMouseEvents();

  protected abstract boolean _isTouchEventsEmulatedUsingPointerEvents();

  protected abstract boolean _isTouchEventsSupported();
  
  /**
   * Some devices support both touch and mouse simultaneously so a touch event maybe a
   * real touch event or a simulated one
   * @return
   */
  public static boolean isTouchEventsEmulatedUsingMouseEvents() {
    return impl._isTouchEventsEmulatedUsingMouseEvents();
  }

  public static boolean isTouchEventsEmulatedUsingPointerEvents() {
    return impl._isTouchEventsEmulatedUsingPointerEvents();
  }

  public static boolean isTouchEventsSupported() {
    return impl._isTouchEventsSupported();
  }
  
  /**
   * Touch is emulated via mouse events but the device may also support native touch events
   */
  public static class TouchSupportMouseAndTouch extends TouchSupport {

    @Override
    protected boolean _isTouchEventsEmulatedUsingMouseEvents() {
      return true;
    }

    @Override
    protected boolean _isTouchEventsEmulatedUsingPointerEvents() {
      return false;
    }

    @Override
    protected boolean _isTouchEventsSupported() {
      return hasTouchSupport;
    }
  }

  /**
   * Browser uses pointer model and so touch is emulated via pointer events 
   */
  public static class TouchSupportEmulatedPointer extends TouchSupport {
    @Override
    protected boolean _isTouchEventsEmulatedUsingMouseEvents() {
      return false;
    }

    @Override
    protected boolean _isTouchEventsEmulatedUsingPointerEvents() {
      return true;
    }

    @Override
    protected boolean _isTouchEventsSupported() {
      return false;
    }
  }

  /**
   * We believe device has touch support only - we assume this is the case
   * for phones only
   */
  public static class TouchSupportTouchOnly extends TouchSupport {
    @Override
    protected boolean _isTouchEventsEmulatedUsingMouseEvents() {
      return false;
    }

    @Override
    protected boolean _isTouchEventsEmulatedUsingPointerEvents() {
      return false;
    }

    @Override
    protected boolean _isTouchEventsSupported() {
      return true;
    }
  }

  /**
   * Only setup if touch and mouse could be supported. For example, for phones we assume only
   * touch is supported but this may change in time??
   * We need to cater for devices that support both touch and mouse
   * where we need to stopPropagation mouse events (except for a mouse click since a gesture)
   * when a touch is in progress so we do not call our touch handlers twice. Compatible mouse events
   * can be fired after a touch event completes so we cancel these events as well by cancelling all
   * mouse events for specified time period e.g. 2 seconds, after the touch completes
   */
  public static native void cancelMouseEventsDuringTouch() /*-{
    var cancelMouseEvents = false;
    var timerRunning = false;
    
    var touchStart = function(event) {
      cancelMouseEvents = true;
    }
    
    var timerFunction = function() {
      cancelMouseEvents = false;
      timerRunning = false;
    };
    
    var touchEndCancel = function(event) {
      if (!timerRunning && event.touches.length == 0) {
        timerRunning = true;
        $wnd.setTimeout(timerFunction,2000);
      }
    }
    
    var maybeCancel = function(event) {
      if (cancelMouseEvents == true) {
        event.stopPropagation();
      }
    }
    
    $doc.body.addEventListener("touchstart", touchStart, true);
    $doc.body.addEventListener("touchend", touchEndCancel, true);
    $doc.body.addEventListener("touchcancel", touchEndCancel, true);
    
    $doc.body.addEventListener("mousedown", maybeCancel, true);
    $doc.body.addEventListener("mousemove", maybeCancel, true);
    $doc.body.addEventListener("mouseup", maybeCancel, true);
    $doc.body.addEventListener("mouseover", maybeCancel, true);
    $doc.body.addEventListener("mouseout", maybeCancel, true);
  }-*/;
  

}
