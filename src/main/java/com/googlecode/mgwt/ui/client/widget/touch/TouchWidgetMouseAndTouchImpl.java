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
package com.googlecode.mgwt.ui.client.widget.touch;

import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.TouchCancelHandler;
import com.google.gwt.event.dom.client.TouchEndHandler;
import com.google.gwt.event.dom.client.TouchMoveHandler;
import com.google.gwt.event.dom.client.TouchStartHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;
import com.googlecode.mgwt.dom.client.event.mouse.HandlerRegistrationCollection;
import com.googlecode.mgwt.dom.client.event.mouse.TouchEndToMouseUpHandler;
import com.googlecode.mgwt.dom.client.event.mouse.TouchMoveToMouseMoveHandler;
import com.googlecode.mgwt.dom.client.event.mouse.TouchStartToMouseDownHandler;
import com.googlecode.mgwt.dom.client.event.touch.TouchHandler;
import com.googlecode.mgwt.ui.client.util.NoopHandlerRegistration;

/**
 * Supports mouse but also supports Touch simultaneously if touch possibly supported 
 */
public class TouchWidgetMouseAndTouchImpl implements TouchWidgetImpl
{
  private static final TouchWidgetImpl delegate = new TouchWidgetTouchOnlyImpl();

  @Override
  public HandlerRegistration addTouchStartHandler(Widget w, TouchStartHandler handler) {
    if (TouchSupport.isTouchEventsSupported()) {
      HandlerRegistrationCollection handlerRegistrations = new HandlerRegistrationCollection();
      handlerRegistrations.addHandlerRegistration(delegate.addTouchStartHandler(w, handler));
      handlerRegistrations.addHandlerRegistration(w.addDomHandler(new TouchStartToMouseDownHandler(handler), MouseDownEvent.getType()));
      return handlerRegistrations;
    }
    else {
      return w.addDomHandler(new TouchStartToMouseDownHandler(handler), MouseDownEvent.getType());
    }
  }

  @Override
  public HandlerRegistration addTouchMoveHandler(Widget w, TouchMoveHandler handler) {
    HandlerRegistrationCollection handlerRegistrations = new HandlerRegistrationCollection();
    if (TouchSupport.isTouchEventsSupported()) {
      handlerRegistrations.addHandlerRegistration(delegate.addTouchMoveHandler(w, handler));
    }
    TouchMoveToMouseMoveHandler touchMoveToMouseMoveHandler = new TouchMoveToMouseMoveHandler(handler);
    handlerRegistrations.addHandlerRegistration(w.addDomHandler(touchMoveToMouseMoveHandler, MouseDownEvent.getType()));
    handlerRegistrations.addHandlerRegistration(w.addDomHandler(touchMoveToMouseMoveHandler, MouseUpEvent.getType()));
    handlerRegistrations.addHandlerRegistration(w.addDomHandler(touchMoveToMouseMoveHandler, MouseMoveEvent.getType()));
    return handlerRegistrations;
  }

  @Override
  public HandlerRegistration addTouchCancelHandler(Widget w, TouchCancelHandler handler) {
    if (TouchSupport.isTouchEventsSupported()) {
      return delegate.addTouchCancelHandler(w, handler);
    }
    return new NoopHandlerRegistration();
  }

  @Override
  public HandlerRegistration addTouchEndHandler(Widget w, TouchEndHandler handler) {
    if (TouchSupport.isTouchEventsSupported()) {
      HandlerRegistrationCollection handlerRegistrations = new HandlerRegistrationCollection();
      handlerRegistrations.addHandlerRegistration(delegate.addTouchEndHandler(w, handler));
      handlerRegistrations.addHandlerRegistration(w.addDomHandler(new TouchEndToMouseUpHandler(handler), MouseUpEvent.getType()));
      return handlerRegistrations;
    }
    else {
      return w.addDomHandler(new TouchEndToMouseUpHandler(handler), MouseUpEvent.getType());
    }
  }

  @Override
  public HandlerRegistration addTouchHandler(Widget w, TouchHandler handler) {
    HandlerRegistrationCollection hrc = new HandlerRegistrationCollection();
    hrc.addHandlerRegistration(addTouchStartHandler(w, handler));
    hrc.addHandlerRegistration(addTouchMoveHandler(w, handler));
    hrc.addHandlerRegistration(addTouchEndHandler(w, handler));
    hrc.addHandlerRegistration(addTouchCancelHandler(w, handler));
    return hrc;
  }

}
