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

import com.google.gwt.event.dom.client.TouchCancelEvent;
import com.google.gwt.event.dom.client.TouchCancelHandler;
import com.google.gwt.event.dom.client.TouchEndEvent;
import com.google.gwt.event.dom.client.TouchEndHandler;
import com.google.gwt.event.dom.client.TouchMoveEvent;
import com.google.gwt.event.dom.client.TouchMoveHandler;
import com.google.gwt.event.dom.client.TouchStartEvent;
import com.google.gwt.event.dom.client.TouchStartHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;
import com.googlecode.mgwt.dom.client.event.mouse.HandlerRegistrationCollection;
import com.googlecode.mgwt.dom.client.event.touch.TouchHandler;

/**
 * Supports Touch only (does not support mouse) i.e. mostly phones 
 */
public class TouchWidgetTouchOnlyImpl implements TouchWidgetImpl
{
  @Override
  public HandlerRegistration addTouchStartHandler(Widget w, TouchStartHandler handler) {
    return w.addDomHandler(handler, TouchStartEvent.getType());
  }

  @Override
  public HandlerRegistration addTouchMoveHandler(Widget w, TouchMoveHandler handler) {
    return w.addDomHandler(handler, TouchMoveEvent.getType());
  }

  @Override
  public HandlerRegistration addTouchCancelHandler(Widget w, TouchCancelHandler handler) {
    return w.addDomHandler(handler, TouchCancelEvent.getType());
  }

  @Override
  public HandlerRegistration addTouchEndHandler(Widget w, final TouchEndHandler handler) {
    return w.addDomHandler(handler, TouchEndEvent.getType());
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
