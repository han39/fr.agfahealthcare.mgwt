package com.google.gwt.user.client.impl;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Required by Safari permutation using pointer event model e.g. IEEdge  
 */
public class DOMImplWebkitPointer extends DOMImplWebkit
{
  static {
    DOMImplStandard.addCaptureEventDispatchers(getCaptureEventDispatchers());
    DOMImplStandard.addBitlessEventDispatchers(getBitlessEventDispatchers());
    capturePointerEvents();
  }
  
  /**
   * Lets have the same behaviour as IOS where the target element continues to receive Pointer events
   * even when the pointer has moved off the element up until PointerUp has occurred.
   * 
   *  Do not do pointer capture on native browser input elements, all sorts of problems arise if you do!
   *  For example if you type into a password field you cannot set the cursor to the end of
   *  the text when re-entering it and so you cannot edit your password.
   *  See issue 286 for an explanation why it is a problem doing pointer capture on a select element
   *  in ie edge mobile.
   */
  private native static void capturePointerEvents() /*-{
    $wnd.addEventListener('pointerdown',
      $entry(function(evt) {
        if ((evt.target.tagName !== 'INPUT') && (evt.target.tagName !== 'TEXTAREA') && (evt.target.tagName !== 'SELECT'))  {
          evt.target.setPointerCapture(evt.pointerId);
        }
      }), true);
  }-*/;

  
  public static native JavaScriptObject getCaptureEventDispatchers() /*-{
    return {
      pointerdown:   @com.google.gwt.user.client.impl.DOMImplStandard::dispatchCapturedMouseEvent(*),
      pointerup:     @com.google.gwt.user.client.impl.DOMImplStandard::dispatchCapturedMouseEvent(*),
      pointermove:   @com.google.gwt.user.client.impl.DOMImplStandard::dispatchCapturedMouseEvent(*),
      pointercancel: @com.google.gwt.user.client.impl.DOMImplStandard::dispatchCapturedMouseEvent(*)
    };
  }-*/;

  public static native JavaScriptObject getBitlessEventDispatchers() /*-{
    return {
      pointerdown:   @com.google.gwt.user.client.impl.DOMImplStandard::dispatchEvent(*),
      pointerup:     @com.google.gwt.user.client.impl.DOMImplStandard::dispatchEvent(*),
      pointermove:   @com.google.gwt.user.client.impl.DOMImplStandard::dispatchEvent(*),
      pointercancel: @com.google.gwt.user.client.impl.DOMImplStandard::dispatchEvent(*)
    };
  }-*/;

}
