package com.googlecode.mgwt.ui.client.widget;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.text.shared.Parser;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ValueBoxBase;
import com.googlecode.mgwt.ui.client.MGWT;
import com.googlecode.mgwt.ui.client.MGWTStyle;
import com.googlecode.mgwt.ui.client.theme.base.InputCss;
import com.googlecode.mgwt.ui.client.widget.base.MValueBoxBase;

public class MDateBox extends MValueBoxBase<Date> {

	private static final DateTimeFormat DEFAULT_FORMAT = DateTimeFormat.getFormat("dd/mm/yy");
	private static final DateTimeFormat W3C_FORMAT = DateTimeFormat.getFormat("yyyy-MM-dd");

	public static class DateValueBoxBase extends ValueBoxBase<Date> {

		private final DateRenderer dateRenderer;
		private final DateParser dateParser;

		protected DateValueBoxBase(DateRenderer dateRenderer, DateParser dateParser) {
			super(DOM.createInputText(), dateRenderer, dateParser);
			this.dateRenderer = dateRenderer;
			this.dateParser = dateParser;

		}
		
		public DateParser getDateParser() {
			return dateParser;
		}
		
		public DateRenderer getDateRenderer() {
			return dateRenderer;
		}

	}

	public static class DateRenderer implements Renderer<Date> {

		private DateTimeFormat format;
		
		public DateRenderer() {
			setFormat(DEFAULT_FORMAT);
		}

		public void setFormat(DateTimeFormat format) {
			this.format = format;
		}

		@Override
		public String render(Date object) {
			return format.format(object);
		}

		@Override
		public void render(Date object, Appendable appendable) throws IOException {
			appendable.append(format.format(object));

		}
	}

	public static class DateParser implements Parser<Date> {

		public DateParser() {
			setFormat(DEFAULT_FORMAT);
		}
		
		private DateTimeFormat format;

		public void setFormat(DateTimeFormat format) {
			this.format = format;
		}

		@Override
		public Date parse(CharSequence text) throws ParseException {

			String string = text.toString();
			try{
				return format.parse(string);
			}catch (Exception e) {
				return null;
			}
			

		}

	}

	private Date lastValue;
	private DateTimeFormat format;

	public MDateBox() {
		this(MGWTStyle.getTheme().getMGWTClientBundle().getInputCss());
	}
	

	public MDateBox(InputCss css) {
		super(css, new DateValueBoxBase(new DateRenderer(), new DateParser()));

		
		
		format = DEFAULT_FORMAT;
		setPlaceHolder(DEFAULT_FORMAT.getPattern());

		addStyleName(css.textBox());

		//fix ios issue with onchange event

		if (MGWT.getOsDetection().isIOs()) {
			//only set input type to date if there is a native picker iOS >= 5
			impl.setType(box.getElement(), "date");
			//use w3c format
			format = W3C_FORMAT;
		}
		
		//apply format to parsers
		getBox().getDateParser().setFormat(format);
		getBox().getDateRenderer().setFormat(format);

		if (MGWT.getOsDetection().isIOs()) {
			addBlurHandler(new BlurHandler() {

				@Override
				public void onBlur(BlurEvent event) {
					Scheduler.get().scheduleDeferred(new ScheduledCommand() {

						@Override
						public void execute() {
							Date value = box.getValue();
							ValueChangeEvent.fireIfNotEqual(box, lastValue, value);
							lastValue = value;

						}
					});

				}
			});
			lastValue = null;
		}

	}
	
	protected DateValueBoxBase getBox(){
		return (DateValueBoxBase) box;
	}

	public void setFormat(String pattern) {
		format = DateTimeFormat.getFormat(pattern);

		if (!MGWT.getOsDetection().isIOs()) {
			setPlaceHolder(pattern);
		}

	}

}
