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
package com.googlecode.mgwt.ui.client.widget.button;

import com.google.gwt.event.dom.client.TouchCancelEvent;
import com.google.gwt.event.dom.client.TouchEndEvent;
import com.google.gwt.event.dom.client.TouchMoveEvent;
import com.google.gwt.event.dom.client.TouchStartEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.HasText;
import com.googlecode.mgwt.dom.client.event.mouse.SimulatedTouchEndEvent;
import com.googlecode.mgwt.dom.client.event.mouse.SimulatedTouchStartEvent;
import com.googlecode.mgwt.dom.client.event.touch.TouchHandler;
import com.googlecode.mgwt.ui.client.widget.touch.TouchWidget;

/**
 * Base class for all buttons
 */
@SuppressWarnings("deprecation")
public abstract class ButtonBase extends TouchWidget implements HasText {

	private boolean active;
	// a temp fix where we no longer add the default touch handlers to the button
	// until a call is made to set the element for the widget. This is required since
	// it is not possible to add a bitless dom handler until the element has been set
	private boolean defaultHandlersAdded;

	private final ButtonBaseAppearance baseAppearance;

	/**
	 * Construct a button with a given element and css
	 *
	 * @param element
	 *           the element to use
	 * @param css
	 *           the css to use
	 */
	public ButtonBase(final ButtonBaseAppearance appearance) {
		baseAppearance = appearance;
	}

	public ButtonBaseAppearance getAppearance() {
		return baseAppearance;
	}

	@Override
	public String getText() {
		return getElement().getInnerText();
	}

	public boolean isActive() {
		return active;
	}

	@Override
	public void setText(final String text) {
		getElement().setInnerText(text);
	}

	@Override
	protected void setElement(final Element elem) {
		super.setElement(elem);

		if (!defaultHandlersAdded) {

			addTouchHandler(new TouchHandler() {

				@Override
				public void onTouchCancel(final TouchCancelEvent event) {
					event.stopPropagation();
					event.preventDefault();
					removeStyleName(baseAppearance.css().active());
					active = false;
				}

				@Override
				public void onTouchEnd(final TouchEndEvent event) {
					event.stopPropagation();
					event.preventDefault();
					removeStyleName(baseAppearance.css().active());
					if (event instanceof SimulatedTouchEndEvent) {
						DOM.releaseCapture(getElement());
					}
					active = false;
				}

				@Override
				public void onTouchMove(final TouchMoveEvent event) {
					event.preventDefault();
					event.stopPropagation();
				}

				@Override
				public void onTouchStart(final TouchStartEvent event) {
					event.stopPropagation();
					event.preventDefault();
					addStyleName(baseAppearance.css().active());
					if (event instanceof SimulatedTouchStartEvent) {
						DOM.setCapture(getElement());
					}
					active = true;
				}
			});

			addTapHandler(event -> removeStyleName(baseAppearance.css().active()));
			defaultHandlersAdded = true;
		}
	}

}
