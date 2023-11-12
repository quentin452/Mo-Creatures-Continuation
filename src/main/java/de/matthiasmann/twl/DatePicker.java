package de.matthiasmann.twl;

import de.matthiasmann.twl.renderer.*;
import java.text.*;
import de.matthiasmann.twl.utils.*;
import java.util.*;
import de.matthiasmann.twl.model.*;

public class DatePicker extends DialogLayout
{
    public static final AnimationState.StateKey STATE_PREV_MONTH;
    public static final AnimationState.StateKey STATE_NEXT_MONTH;
    private final ArrayList<ToggleButton> dayButtons;
    private final MonthAdjuster monthAdjuster;
    private final Runnable modelChangedCB;
    private Locale locale;
    private DateFormatSymbols formatSymbols;
    String[] monthNamesLong;
    String[] monthNamesShort;
    Calendar calendar;
    private DateFormat dateFormat;
    private DateFormat dateParser;
    private ParseHook parseHook;
    private Callback[] callbacks;
    private DateModel model;
    private boolean cbAdded;
    
    public DatePicker() {
        this(Locale.getDefault(), DateFormat.getDateInstance());
    }
    
    public DatePicker(final Locale locale, final int style) {
        this(locale, DateFormat.getDateInstance(style, locale));
    }
    
    public DatePicker(final Locale locale, final DateFormat dateFormat) {
        this.dayButtons = new ArrayList<ToggleButton>();
        this.monthAdjuster = new MonthAdjuster();
        this.calendar = Calendar.getInstance();
        this.modelChangedCB = new Runnable() {
            @Override
            public void run() {
                DatePicker.this.modelChanged();
            }
        };
        this.setDateFormat(locale, dateFormat);
    }
    
    public DateModel getModel() {
        return this.model;
    }
    
    public void setModel(final DateModel model) {
        if (this.model != model) {
            if (this.cbAdded && this.model != null) {
                this.model.removeCallback(this.modelChangedCB);
            }
            this.model = model;
            if (this.cbAdded && this.model != null) {
                this.model.addCallback(this.modelChangedCB);
            }
            this.modelChanged();
        }
    }
    
    public DateFormat getDateFormat() {
        return this.dateFormat;
    }
    
    public Locale getLocale() {
        return this.locale;
    }
    
    public void setDateFormat(final Locale locale, final DateFormat dateFormat) {
        if (dateFormat == null) {
            throw new NullPointerException("dateFormat");
        }
        if (locale == null) {
            throw new NullPointerException("locale");
        }
        if (this.dateFormat != dateFormat || this.locale != locale) {
            final long time = (this.calendar != null) ? this.calendar.getTimeInMillis() : System.currentTimeMillis();
            this.locale = locale;
            this.dateFormat = dateFormat;
            this.dateParser = DateFormat.getDateInstance(3, locale);
            this.calendar = (Calendar)dateFormat.getCalendar().clone();
            this.formatSymbols = new DateFormatSymbols(locale);
            this.monthNamesLong = this.formatSymbols.getMonths();
            this.monthNamesShort = this.formatSymbols.getShortMonths();
            this.calendar.setTimeInMillis(time);
            this.create();
            this.modelChanged();
        }
    }
    
    public ParseHook getParseHook() {
        return this.parseHook;
    }
    
    public void setParseHook(final ParseHook parseHook) {
        this.parseHook = parseHook;
    }
    
    public void addCallback(final Callback callback) {
        this.callbacks = CallbackSupport.addCallbackToList(this.callbacks, callback, Callback.class);
    }
    
    public void removeCallback(final Callback callback) {
        this.callbacks = CallbackSupport.removeCallbackFromList(this.callbacks, callback);
    }
    
    public String formatDate() {
        return this.dateFormat.format(this.calendar.getTime());
    }
    
    public void parseDate(final String date) throws ParseException {
        this.parseDateImpl(date, true);
    }
    
    protected void parseDateImpl(final String text, final boolean update) throws ParseException {
        if (this.parseHook != null && this.parseHook.parse(text, this.calendar, update)) {
            return;
        }
        final ParsePosition position = new ParsePosition(0);
        final Date parsed = this.dateParser.parse(text, position);
        if (position.getIndex() > 0) {
            if (update) {
                this.calendar.setTime(parsed);
                this.calendarChanged();
            }
            return;
        }
        String lowerText = text.trim().toLowerCase(this.locale);
        final String[][] monthNamesStyles = { this.monthNamesLong, this.monthNamesShort };
        int month = -1;
        int year = -1;
        boolean hasYear = false;
        Label_0213: {
            for (final String[] monthNames : monthNamesStyles) {
                for (int i = 0; i < monthNames.length; ++i) {
                    final String name = monthNames[i].toLowerCase(this.locale);
                    if (name.length() > 0 && lowerText.startsWith(name)) {
                        month = i;
                        lowerText = TextUtil.trim(lowerText, name.length());
                        break Label_0213;
                    }
                }
            }
            try {
                year = Integer.parseInt(lowerText);
                if (year < 100) {
                    year = this.fixupSmallYear(year);
                }
                hasYear = true;
            }
            catch (IllegalArgumentException ex) {}
        }
        if (month < 0 && !hasYear) {
            throw new ParseException("Unparseable date: \"" + text + "\"", position.getErrorIndex());
        }
        if (update) {
            if (month >= 0) {
                this.calendar.set(2, month + this.calendar.getMinimum(2));
            }
            if (hasYear) {
                this.calendar.set(1, year);
            }
            this.calendarChanged();
        }
    }
    
    private int fixupSmallYear(int year) {
        final Calendar cal = (Calendar)this.calendar.clone();
        cal.setTimeInMillis(System.currentTimeMillis());
        final int futureYear = cal.get(1) + 20;
        final int tripPoint = futureYear % 100;
        if (year > tripPoint) {
            year -= 100;
        }
        year += futureYear - tripPoint;
        return year;
    }
    
    @Override
    protected void afterAddToGUI(final GUI gui) {
        super.afterAddToGUI(gui);
        if (!this.cbAdded && this.model != null) {
            this.model.addCallback(this.modelChangedCB);
        }
        this.cbAdded = true;
    }
    
    @Override
    protected void beforeRemoveFromGUI(final GUI gui) {
        if (this.cbAdded && this.model != null) {
            this.model.removeCallback(this.modelChangedCB);
        }
        this.cbAdded = false;
        super.beforeRemoveFromGUI(gui);
    }
    
    private void create() {
        final int minDay = this.calendar.getMinimum(5);
        final int maxDay = this.calendar.getMaximum(5);
        final int minDayOfWeek = this.calendar.getMinimum(7);
        final int maxDayOfWeek = this.calendar.getMaximum(7);
        final int daysPerWeek = maxDayOfWeek - minDayOfWeek + 1;
        final int numWeeks = (maxDay - minDay + daysPerWeek * 2 - 1) / daysPerWeek;
        this.setHorizontalGroup(null);
        this.setVerticalGroup(null);
        this.removeAllChildren();
        this.dayButtons.clear();
        final String[] weekDays = this.formatSymbols.getShortWeekdays();
        final Group daysHorz = this.createSequentialGroup();
        final Group daysVert = this.createSequentialGroup();
        final Group[] daysOfWeekHorz = new Group[daysPerWeek];
        Group daysRow = this.createParallelGroup();
        daysVert.addGroup(daysRow);
        for (int i = 0; i < daysPerWeek; ++i) {
            daysHorz.addGroup(daysOfWeekHorz[i] = this.createParallelGroup());
            final Label l = new Label(weekDays[i + minDay]);
            daysOfWeekHorz[i].addWidget(l);
            daysRow.addWidget(l);
        }
        for (int week = 0; week < numWeeks; ++week) {
            daysRow = this.createParallelGroup();
            daysVert.addGroup(daysRow);
            for (int day = 0; day < daysPerWeek; ++day) {
                final ToggleButton tb = new ToggleButton();
                tb.setTheme("daybutton");
                this.dayButtons.add(tb);
                daysOfWeekHorz[day].addWidget((Widget)tb);
                daysRow.addWidget((Widget)tb);
            }
        }
        this.setHorizontalGroup(this.createParallelGroup().addWidget(this.monthAdjuster).addGroup(daysHorz));
        this.setVerticalGroup(this.createSequentialGroup().addWidget(this.monthAdjuster).addGroup(daysVert));
    }
    
    void modelChanged() {
        if (this.model != null) {
            this.calendar.setTimeInMillis(this.model.getValue());
        }
        this.updateDisplay();
    }
    
    void calendarChanged() {
        if (this.model != null) {
            this.model.setValue(this.calendar.getTimeInMillis());
        }
        this.updateDisplay();
    }
    
    void updateDisplay() {
        this.monthAdjuster.syncWithModel();
        final Calendar cal = (Calendar)this.calendar.clone();
        final int minDay = this.calendar.getMinimum(5);
        final int maxDay = this.calendar.getActualMaximum(5);
        final int minDayOfWeek = cal.getMinimum(7);
        final int maxDayOfWeek = cal.getMaximum(7);
        final int daysPerWeek = maxDayOfWeek - minDayOfWeek + 1;
        int day = this.calendar.get(5);
        final int weekDay = this.calendar.get(7);
        if (weekDay > minDayOfWeek) {
            final int adj = minDayOfWeek - weekDay;
            day += adj;
            cal.add(5, adj);
        }
        while (day > minDay) {
            day -= daysPerWeek;
            cal.add(5, -daysPerWeek);
        }
        for (final ToggleButton tb : this.dayButtons) {
            final DayModel dayModel = new DayModel(day);
            tb.setText(Integer.toString(cal.get(5)));
            tb.setModel(dayModel);
            final de.matthiasmann.twl.AnimationState animState = tb.getAnimationState();
            animState.setAnimationState(DatePicker.STATE_PREV_MONTH, day < minDay);
            animState.setAnimationState(DatePicker.STATE_NEXT_MONTH, day > maxDay);
            dayModel.update();
            cal.add(5, 1);
            ++day;
        }
        if (this.callbacks != null) {
            for (final Callback cb : this.callbacks) {
                cb.calendarChanged(this.calendar);
            }
        }
    }
    
    static {
        STATE_PREV_MONTH = AnimationState.StateKey.get("prevMonth");
        STATE_NEXT_MONTH = AnimationState.StateKey.get("nextMonth");
    }
    
    class DayModel extends HasCallback implements BooleanModel
    {
        final int day;
        boolean active;
        
        DayModel(final int day) {
            this.day = day;
        }
        
        @Override
        public boolean getValue() {
            return this.active;
        }
        
        void update() {
            final boolean newActive = DatePicker.this.calendar.get(5) == this.day;
            if (this.active != newActive) {
                this.active = newActive;
                this.doCallback();
            }
        }
        
        @Override
        public void setValue(final boolean value) {
            if (value && !this.active) {
                DatePicker.this.calendar.set(5, this.day);
                DatePicker.this.calendarChanged();
            }
        }
    }
    
    class MonthAdjuster extends ValueAdjuster
    {
        private long dragStartDate;
        
        @Override
        protected void doDecrement() {
            DatePicker.this.calendar.add(2, -1);
            DatePicker.this.calendarChanged();
        }
        
        @Override
        protected void doIncrement() {
            DatePicker.this.calendar.add(2, 1);
            DatePicker.this.calendarChanged();
        }
        
        @Override
        protected String formatText() {
            return DatePicker.this.monthNamesLong[DatePicker.this.calendar.get(2)] + " " + DatePicker.this.calendar.get(1);
        }
        
        @Override
        protected void onDragCancelled() {
            DatePicker.this.calendar.setTimeInMillis(this.dragStartDate);
            DatePicker.this.calendarChanged();
        }
        
        @Override
        protected void onDragStart() {
            this.dragStartDate = DatePicker.this.calendar.getTimeInMillis();
        }
        
        @Override
        protected void onDragUpdate(int dragDelta) {
            dragDelta /= 5;
            DatePicker.this.calendar.setTimeInMillis(this.dragStartDate);
            DatePicker.this.calendar.add(2, dragDelta);
            DatePicker.this.calendarChanged();
        }
        
        @Override
        protected void onEditCanceled() {
        }
        
        @Override
        protected boolean onEditEnd(final String text) {
            try {
                DatePicker.this.parseDateImpl(text, true);
                return true;
            }
            catch (ParseException ex) {
                return false;
            }
        }
        
        @Override
        protected String onEditStart() {
            return this.formatText();
        }
        
        @Override
        protected boolean shouldStartEdit(final char ch) {
            return false;
        }
        
        @Override
        protected void syncWithModel() {
            this.setDisplayText();
        }
        
        @Override
        protected String validateEdit(final String text) {
            try {
                DatePicker.this.parseDateImpl(text, false);
                return null;
            }
            catch (ParseException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }
    
    public interface Callback
    {
        void calendarChanged(final Calendar p0);
    }
    
    public interface ParseHook
    {
        boolean parse(final String p0, final Calendar p1, final boolean p2) throws ParseException;
    }
}
