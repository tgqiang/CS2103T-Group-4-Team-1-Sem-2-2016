package cs2103_w09_1j.esther;

/**
 * ========== [ TASK OBJECT DEFINITIONS ] ==========
 * This class contains the representation of the
 * task object that will be used by the program.
 * 
 * ============= [ IMPORTANT NOTICES ] =============
 * NOTE: Date (java.util.Date) class methods are
 * largely deprecated and it has been recommended
 * by Java that we use Calendar class instead.
 * 
 * CHANGES MADE: Added to TaskField, STARTDATE, ENDDATE, STARTTIME, ENDTIME, 
 * 				 Removed date to cater start and end date.
 * 
 * @author Tay Guo Qiang
 *         (add your name to list of authors if you made
 *         changes to this class definition)
 */

// import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import cs2103_w09_1j.esther.Status;

import sun.util.resources.cldr.id.LocaleNames_id;

public class Task implements Comparable<Task> {
	public enum TaskField {
		NAME("taskName"), ID("taskID"), PRIORITY("priority"), STARTDATE("startDate"), ENDDATE("endDate"), STARTTIME(
				"startTime"), ENDTIME("endTime"), SORT("order"), UPDATENAME(
						"updateName"), SHOW("order"), UNDO("undo"), HELP("help"), COMPLETED("completed");

		private String taskKeyName;
		private static final Map<String, TaskField> lookup = new HashMap<String, TaskField>();

		private TaskField(String _taskKeyName) {
			this.taskKeyName = _taskKeyName;
		}

		public String getTaskKeyName() {
			return taskKeyName;
		}

		/**
		 * This operations reversely gets the CommandKey from the value.
		 * 
		 * @param commandValue
		 *            The input given by the user.
		 * @return The command based on the input.
		 */
		public static TaskField get(String taskKeyValue) {
			return lookup.get(taskKeyValue);
		}

		static {
			// Create reverse lookup hash map
			for (TaskField _taskKeyName : TaskField.values()) {
				lookup.put(_taskKeyName.getTaskKeyName(), _taskKeyName);
			}
		}
	}

	public static final String SORT_BY_DATE_KEYWORD = "date";
	public static final String SORT_BY_START_DATE_KEYWORD = "startDate";
	public static final String SORT_BY_END_DATE_KEYWORD = "endDate";
	public static final String SORT_BY_NAME_KEYWORD = "taskName";
	public static final String SORT_FLOATING_BY_NAME_KEYWORD = "float_taskName";
	public static final String SORT_BY_PRIORITY_KEYWORD = "priority";
	public static final String SORT_FLOATING_BY_PRIORITY_KEYWORD = "float_priority";
	public static final String SORT_BY_ID_KEYWORD = "id";
	private static final int DEFAULT_STARTING_ID = 0;
	private static final int DEFAULT_TASK_PRIORITY = 5;
	private static final int HIGHEST_TASK_PRIORITY = 1;

	// TODO for Jeremy: attributes have been changed (added _startDate &
    // _endDate).
    private String _name;
    private Date _startDate;
    private Date _endDate;
    private int _priority; // for now, lower number indicates higher priority
    private int _id;
    private boolean _isCompleted;
    private boolean _isValid = false;

    private static String _sortCriterion = SORT_BY_PRIORITY_KEYWORD;
    private static int _assignId = DEFAULT_STARTING_ID;

    private final static SimpleDateFormat _dateOnlyFormatter = new SimpleDateFormat("dd/MM/yyyy");
    private final static SimpleDateFormat _dateAndTimeFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private final static Logger taskLogger = Logger.getLogger("taskLogger");
    private final static int NUM_FIELDS = 6;
    private final static String completedStr = "Completed";
    private final static String notCompletedStr = "Incomplete";

	// TODO for Jeremy: Might affect your regex-es.
	private final static String delimiterPattern = "\\|";
	private final static String idnoString = "ID\\: (\\d+)";
	private final static String dateString = "\\[([^\\]]+)\\] ";
	private final static String nameString = "([^\\|]+)";
	private final static String prioString = "Priority: (\\d+)";
	private final static String compString = "(" + completedStr + "|" + notCompletedStr + ")";
	private final static String[] regexArray = {	idnoString,
													dateString,
													dateString,
													nameString,
													prioString,
													compString };

	/**
	 * Constructs an empty Task object.
	 * 
	 * @@author A0129660A
	 */
	public Task() {

	}

	/**
	 * Constructs a Task with reference to a Command object.
	 * 
	 * @param command
	 *            the Command object containing the required parameters
	 * @throws ParseException
	 * @return a Task with the attributes set with the parameters
	 * @@author A0129660A
	 */
	public Task(Command command) throws ParseException {
		this();
		Date today = new Date();
		Date startDate = null;
		Date endDate = null;
		String taskName = command.getSpecificParameter(TaskField.NAME.getTaskKeyName());

		String startDateString = command.hasParameter(TaskField.STARTDATE.getTaskKeyName())
				? command.getSpecificParameter(TaskField.STARTDATE.getTaskKeyName())
				: null;

		String startTimeString = command.hasParameter(TaskField.STARTTIME.getTaskKeyName())
				? command.getSpecificParameter(TaskField.STARTTIME.getTaskKeyName())
				: null;
		startDate = parseDateTimeToString(today, startDateString, startTimeString);

		String endDateString = command.hasParameter(TaskField.ENDDATE.getTaskKeyName())
				? command.getSpecificParameter(TaskField.ENDDATE.getTaskKeyName())
				: null;

		String endTimeString = command.hasParameter(TaskField.ENDTIME.getTaskKeyName())
				? command.getSpecificParameter(TaskField.ENDTIME.getTaskKeyName())
				: null;
		endDate = parseDateTimeToString(today, endDateString, endTimeString);

		int priority = command.hasParameter(TaskField.PRIORITY.getTaskKeyName())
				? Integer.parseInt(command.getSpecificParameter(TaskField.PRIORITY.getTaskKeyName()))
				: DEFAULT_TASK_PRIORITY;
		this.setName(taskName);
		this.setStartDate(startDate);
		this.setEndDate(endDate);
		this.setPriority(priority);
		this.setCompleted(false);
		this.setID(_assignId);
		this.setIsValid(true);
		_assignId++;
	}

	/**
	 * Builds a task from a String with specific format
	 * "ID: {id}| [{dd/MM/yyyy}] {name}| Priority: {prio}| Completed: {com}"
	 * 
	 * @param string
	 * @author Jeremy Hon
	 * @throws ParseException
	 */
	// TODO for Jeremy: Task attributes have been changed. Need to revise this
	// method.
	public Task(String string) throws ParseException {
		this();
		String[] resultsArray = new String[NUM_FIELDS];
		String[] matcherInput = string.split(delimiterPattern);

		if (matcherInput.length != NUM_FIELDS) {
			taskLogger.severe(
					"Task constructor expected " + NUM_FIELDS + " arguments but received " + matcherInput.length + ".");
			return;
		}

		for (int i = 0; i < regexArray.length; i++) {
			resultsArray[i] = findMatch(regexArray[i], matcherInput[i]);
			if (resultsArray[i] == null) {
				taskLogger.warning("Task builder could not parse " + i + "th element for task " + resultsArray[0]);
				resultsArray[i] = "";
			}
		}

		if (resultsArray[0] == "") {
			taskLogger.severe("Task constructor cannot find an ID");
			return;
		}

		int localID = Integer.parseInt(resultsArray[0]);
		Date sDate = parseDate(resultsArray[1]);
		Date eDate = parseDate(resultsArray[2]);
		String taskName = resultsArray[3];
		int priority = Integer.parseInt(resultsArray[4]);
		boolean complete = parseCompleted(resultsArray[5]);

		this.setID(localID);
		this.setName(taskName.trim());
		this.setStartDate(sDate);
		this.setEndDate(eDate);
		this.setPriority(priority);
		this.setCompleted(complete);
		this.setIsValid(true);
	}

	/**
	 * Returns the matching string given a regex and a string
	 * 
	 * @param regex
	 * @param input
	 * @return
	 */
	// TODO for Jeremy: regex changes may affect this too.
	public static String findMatch(String regex, String input) {
		Matcher matcher = Pattern.compile(regex).matcher(input);
		if (matcher.find()) {
			return matcher.group(1);
		} else {
			return null;
		}
	}

	/**
	 * Returns a human-readable String representation of a Task.
	 * 
	 * @return a String representation of the Task
	 * @author Jeremy Hon
	 */
	// TODO for Jeremy: core attribute changes will affect this.
	@Override
	public String toString() {
		String taskString = "";
		taskString += "ID: " + _id + " | ";
		taskString += "[" + sDateToString() + "] | ";
		taskString += "[" + eDateToString() + "] | ";
		taskString += _name + " | ";
		taskString += "Priority: " + _priority + " | ";
		taskString += completedToString();
		taskString += "\n";
		return taskString;
	}

	/**
	 * Gets the name of the Task.
	 * 
	 * @return the name of the task
	 * @@author A0129660A
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Sets the name of the Task.
	 * 
	 * @param name
	 *            the desired task name
	 * @@author A0129660A
	 */
	public void setName(String name) {
		_name = name;
	}

	/**
	 * Gets the starting deadline of the Task.
	 * 
	 * @return the deadline of the task
	 * @@author A0129660A
	 */
	public Date getStartDate() {
		return _startDate;
	}

	/**
	 * Sets the starting deadline of the Task.
	 * 
	 * @param date
	 *            the desired task deadline
	 * @@author A0129660A
	 */
	public void setStartDate(Date date) {
		_startDate = date;
	}

	/**
	 * Gets the latest deadline of the Task.
	 * 
	 * @return the deadline of the task
	 * @@author A0129660A
	 */
	public Date getEndDate() {
		return _endDate;
	}

	/**
	 * Sets the latest deadline of the Task.
	 * 
	 * @param date
	 *            the desired task deadline
	 * @@author A0129660A
	 */
	public void setEndDate(Date date) {
		_endDate = date;
	}

	/**
	 * 
	 * @return
	 * @author Jeremy Hon
	 */
	public String sDateToString() {
		return dateToString(_startDate);
	}

	/**
	 * 
	 * @return
	 * @author Jeremy Hon
	 */
	public String eDateToString() {
		return dateToString(_endDate);
	}

	private String dateToString(Date date) {
		if (date == null) {
			return "";
		} else {
			return _dateAndTimeFormatter.format(date);
		}
	}

	private Date parseDateTimeToString(Date today, String dateString, String timeString)
			throws ParseException {
		Date date = null;
		if (dateString != null && timeString != null) {
			//System.out.println("Date and time parts are modified.");
			date = _dateAndTimeFormatter.parse(dateString + " " + timeString);
		} else if (dateString != null && timeString == null) {
			date = _dateAndTimeFormatter.parse(dateString + " 23:59");
		} else if (dateString == null && timeString != null) {
			//System.out.println("Time part is modified.");
			date = _dateAndTimeFormatter.parse(_dateOnlyFormatter.format(today) + " " + timeString);
		} 
		return date;
	}

	/**
	 * 
	 * @@author Jeremy Hon
	 * @param dateStr
	 * @return
	 * @throws ParseException
	 */
	public Date parseDate(String dateStr) throws ParseException {
		if (dateStr == null || dateStr.length() == 0) {
			return null;
		}
		return _dateAndTimeFormatter.parse(dateStr);
	}

	/**
	 * Gets the sorting criterion to sort Tasks by.
	 * 
	 * The default sorting criterion is by task priority.
	 * 
	 * @see Task#compareTo(Task)
	 * @return a String representing the sorting criterion
	 * @@author A0129660A
	 */
	public static String getSortCriterion() {
		return _sortCriterion;
	}

    /**
     * Sets the sorting criterion to sort Tasks by.
     * 
     * @see Task#compareTo(Task)
     * @param sortCriterion
     *            the criteria to sort tasks by
     * @@author A0129660A
     */
    public static void setSortCriterion(String sortCriterion) {
    	_sortCriterion = sortCriterion;
    }

    /**
     * Gets the priority of the Task.
     * 
     * @return the priority level of the task
     * @@author A0129660A
     */
	public int getPriority() {
		return _priority;
	}

	/**
	 * Sets the priority of the Task.
	 * 
	 * @param priority
	 *            the desired task's priority level
	 * @@author A0129660A
	 */
	public void setPriority(int priority) {
		_priority = priority;
	}

	/**
	 * Gets the ID of the Task.
	 * 
	 * @return the task ID
	 * @@author A0129660A
	 */
	public int getId() {
		return _id;
	}

	/**
	 * Sets the ID of the Task.
	 * 
	 * @param id
	 *            the task ID
	 * @@author A0129660A
	 */
	public void setID(int id) {
		_id = id;
	}

	/**
	 * Gets the global ID variable for system usage.
	 * 
	 * @return the global ID variable in this class
	 * @@author A0129660A
	 */
	public static int getGlobalId() {
		return _assignId;
	}

	/**
	 * Sets the global ID variable for system usage.
	 * 
	 * @return the global ID variable in this class
	 * @@author A0129660A
	 */
	public static void setGlobalId(int newId) {
		_assignId = newId;
	}

	/**
	 * Gets completion status of the Task.
	 * 
	 * @return task status (whether it is completed or not)
	 * @@author A0129660A
	 */
	public boolean isCompleted() {
		return _isCompleted;
	}

	/**
	 * Sets completion status of the Task.
	 * 
	 * @param isCompleted
	 *            the status of the task (completed or not)
	 * @@author A0129660A
	 */
	public void setCompleted(boolean isCompleted) {
		_isCompleted = isCompleted;
	}

	public boolean isFloatingTask() {
		return (_startDate == null && _endDate == null) ? true : false;
	}
	
	public boolean isEvent() {
		return (_startDate != null && _endDate != null) ? true : false;
	}

	/**
	 * @author Jeremy Hon
	 */
	public String completedToString() {
		if (isCompleted()) {
			return completedStr;
		} else {
			return notCompletedStr;
		}
	}

	public boolean parseCompleted(String completeStr) {
		return completeStr.contains(completedStr);
	}

	public boolean isValid() {
		return _isValid;
	}

	public void setIsValid(boolean _isValid) {
		this._isValid = _isValid;
	}

    /**
     * Creates a copy of this Task object.
     * 
     * @return a copy of the Task object
     * @@author A0129660A
     */
	public Task clone() {
		Task copy = new Task();
		copy.setName(_name);
		copy.setStartDate(_startDate);
		copy.setEndDate(_endDate);
		copy.setID(_id);
		copy.setPriority(_priority);
		copy.setCompleted(_isCompleted);
		copy.setIsValid(_isValid);
		return copy;
	}

    /**
     * Updates the state of the Task object based on the Command object
     * parameters.
     * 
     * @param command
     *            the Command object containing the required parameters
     * @throws ParseException
     * @@author A0129660A
     */
	public boolean updateTask(Command command) throws ParseException {
		String startDate = null;
		String startTime = null;
		String endDate = null;
		String endTime = null;

		if (command.hasParameter(TaskField.NAME.getTaskKeyName())) {
			this.setName(command.getSpecificParameter(TaskField.NAME.getTaskKeyName()));
		}

		if (command.hasParameter(TaskField.UPDATENAME.getTaskKeyName())) {
			this.setName(command.getSpecificParameter(TaskField.UPDATENAME.getTaskKeyName()));
		}

		// DATE AND TIME HANDLING
		if (command.hasParameter(TaskField.STARTDATE.getTaskKeyName())) {
			startDate = command.getSpecificParameter(TaskField.STARTDATE.getTaskKeyName());
		}

		if (command.hasParameter(TaskField.STARTTIME.getTaskKeyName())) {
			startTime = command.getSpecificParameter(TaskField.STARTTIME.getTaskKeyName());
		}

		if (command.hasParameter(TaskField.ENDDATE.getTaskKeyName())) {
			endDate = command.getSpecificParameter(TaskField.ENDDATE.getTaskKeyName());
		}

		if (command.hasParameter(TaskField.ENDTIME.getTaskKeyName())) {
			endTime = command.getSpecificParameter(TaskField.ENDTIME.getTaskKeyName());
		}

		String oldStartTime = (dateToString(_startDate).equals("")) ? null : dateToString(_startDate).substring(11);
		String oldEndTime = (dateToString(_endDate).equals("")) ? null : dateToString(_endDate).substring(11);
		Date oldStartDate = _startDate;
		Date oldEndDate = _endDate;
		Date newStartDate = null;
		Date newEndDate = null;
		
		if (_startDate == null) {
			newStartDate = parseDateTimeToString(new Date(), startDate, startTime);
			this.setStartDate(newStartDate);
		} else if (startTime == null) {
			newStartDate = parseDateTimeToString(_startDate, startDate, oldStartTime);
			this.setStartDate(newStartDate);
		} else {
			newStartDate = parseDateTimeToString(_startDate, startDate, startTime);
			this.setStartDate(newStartDate);
		}
		
		if (_endDate == null) {
			newEndDate = parseDateTimeToString(new Date(), endDate, endTime);
			this.setEndDate(newEndDate);
		} else if (endTime == null) {
			newEndDate = parseDateTimeToString(_endDate, endDate, oldEndTime);
			this.setEndDate(newEndDate);
		} else {
			newEndDate = parseDateTimeToString(_endDate, endDate, endTime); 
			this.setEndDate(newEndDate);
		}
		
		if (isAcceptableDateChange(newStartDate, newEndDate)) {
			// do nothing
		} else {
			this.setStartDate(oldStartDate);
			this.setEndDate(oldEndDate);
			Status._errorCode = Status.ErrorCode.UPDATE_START_END_VIOLATE;
			return false;
		}
		//System.out.println(dateToString(_startDate));
		//System.out.println(dateToString(_endDate));

		if (command.hasParameter(TaskField.PRIORITY.getTaskKeyName())) {
			int newPriority = Integer.parseInt(command.getSpecificParameter(TaskField.PRIORITY.getTaskKeyName())); 
			if (newPriority < HIGHEST_TASK_PRIORITY || newPriority > DEFAULT_TASK_PRIORITY) {
				Status._errorCode = Status.ErrorCode.UPDATE_INVALID_PRIORITY;
				return false;
			} else {
				this.setPriority(newPriority);
			}
		}

		if (command.hasParameter(TaskField.ID.getTaskKeyName())) {
			this.setID(Integer.parseInt(command.getSpecificParameter(TaskField.ID.getTaskKeyName())));
		}

		if (command.hasParameter(TaskField.COMPLETED.getTaskKeyName())) {
			this.setCompleted(Boolean.parseBoolean(command.getSpecificParameter(TaskField.COMPLETED.getTaskKeyName())));
		}
		
		return true;
	}
	
	private boolean isAcceptableDateChange(Date startDate, Date endDate) {
		if (startDate != null && endDate == null) {
			return false;
		} else if (startDate == null && endDate == null) {
			return true;
		} else if (startDate == null && endDate != null) {
			return true;
		} else if (startDate.compareTo(endDate) < 0) {
			return true;
		} else {
			return false;
		}
	}

    /**
     * The comparison method for comparing tasks. This method is used for
     * sorting tasks in certain order. The default sorting order is by task
     * priority, then by task deadline and finally by name of task. However,
     * other sorting criteria, such as by name or by date, is also supported.
     * 
     * @param task
     *            the Task object to compare to
     * @return 0 if the Task compared to is equal to itself; a value less than 0
     *         if the Task compared to comes after itself; and a value more than
     *         0 if the Task compared to comes before itself.
     * @@author A0129660A
     */
    @Override
	public int compareTo(Task task) {
		switch (_sortCriterion) {
		case SORT_BY_DATE_KEYWORD:
			// System.out.println("Sorting by date.");
			return compareByDate(task);

		case SORT_BY_NAME_KEYWORD:
			// System.out.println("Sorting by name.");
			return compareByName(task);
			
		case SORT_FLOATING_BY_NAME_KEYWORD:
			return compareFloatingByName(task);
			
		case SORT_BY_PRIORITY_KEYWORD:
			return compareByPriority(task);
			
		case SORT_FLOATING_BY_PRIORITY_KEYWORD:
			return compareFloatingByPriority(task);

		case SORT_BY_ID_KEYWORD:
			// System.out.println("Sorting by ID.");
			return compareById(task);
		
		case SORT_BY_START_DATE_KEYWORD:
			return compareByStartDate(task);
			
		case SORT_BY_END_DATE_KEYWORD:
			return compareByDate(task);

		default:
			// System.out.println("Sorting by priority.");
			return compareByPriority(task);
		}
	}

    /**
     * The comparison method invoked when sorting criteria is by task deadline.
     * 
     * Comparison order is by date, then by priority and then by name.
     * 
     * @param task
     *            the Task object to compare to
     * @return 0 if the Task compared to is equal to itself; a value less than 0
     *         if the Task compared to comes after itself; and a value more than
     *         0 if the Task compared to comes before itself.
     * @@author A0129660A
     */
	private int compareByDate(Task task) {
		if (_endDate.equals(task.getEndDate())) {
			if (_priority == task.getPriority()) {
				return _name.compareTo(task.getName());
			} else {
				return Integer.compare(_priority, task.getPriority());
			}
		} else {
			return _endDate.compareTo(task.getEndDate());
		}
	}
	
	/**
     * The comparison method invoked when sorting criteria is by task deadline.
     * 
     * Comparison order is by date, then by priority and then by name.
     * 
     * @param task
     *            the Task object to compare to
     * @return 0 if the Task compared to is equal to itself; a value less than 0
     *         if the Task compared to comes after itself; and a value more than
     *         0 if the Task compared to comes before itself.
     * @@author A0129660A
     */
	private int compareByStartDate(Task task) {
		if (_startDate.equals(task.getStartDate())) {
			if (_priority == task.getPriority()) {
				return _name.compareTo(task.getName());
			} else {
				return Integer.compare(_priority, task.getPriority());
			}
		} else {
			return _startDate.compareTo(task.getStartDate());
		}
	}

    /**
     * The comparison method invoked when sorting criteria is by task name.
     * 
     * Comparison order is by name, then by priority and then by date.
     * 
     * @param task
     *            the Task object to compare to
     * @return 0 if the Task compared to is equal to itself; a value less than 0
     *         if the Task compared to comes after itself; and a value more than
     *         0 if the Task compared to comes before itself.
     * @@author A0129660A
     */
	private int compareByName(Task task) {
		if (_name.equals(task.getName())) {
			if (_priority == task.getPriority()) {
				return _endDate.compareTo(task.getEndDate());
			} else {
				return Integer.compare(_priority, task.getPriority());
			}
		} else {
			return _name.compareTo(task.getName());
		}
	}
	
	/**
     * The comparison method invoked when sorting criteria is by task name.
     * This comparison method is used only on floating tasks.
     * 
     * Comparison order is by name, then by priority and then by date.
     * 
     * @param task
     *            the Task object to compare to
     * @return 0 if the Task compared to is equal to itself; a value less than 0
     *         if the Task compared to comes after itself; and a value more than
     *         0 if the Task compared to comes before itself.
     * @@author A0129660A
     */
	private int compareFloatingByName(Task task) {
		if (_name.equals(task.getName())) {
			return Integer.compare(_priority, task.getPriority());
		} else {
			return _name.compareTo(task.getName());
		}
	}

    /**
     * The comparison method invoked when sorting criteria is by task priority.
     * 
     * @param task
     *            the Task object to compare to
     * @return 0 if the Task compared to is equal to itself; a value less than 0
     *         if the Task compared to comes after itself; and a value more than
     *         0 if the Task compared to comes before itself.
     * @@author A0129660A
     */
	private int compareByPriority(Task task) {
		if (_priority == task.getPriority()) {
			if (_endDate.equals(task.getEndDate())) {
				return _name.compareTo(task.getName());
			} else {
				return _endDate.compareTo(task.getEndDate());
			}
		} else {
			return Integer.compare(_priority, task.getPriority());
		}
	}
	
	/**
     * The comparison method invoked when sorting criteria is by task priority.
     * This comparison method is used only on floating tasks.
     * 
     * @param task
     *            the Task object to compare to
     * @return 0 if the Task compared to is equal to itself; a value less than 0
     *         if the Task compared to comes after itself; and a value more than
     *         0 if the Task compared to comes before itself.
     * @@author A0129660A
     */
	private int compareFloatingByPriority(Task task) {
		if (_priority == task.getPriority()) {
			return _name.compareTo(task.getName());
		} else {
			return Integer.compare(_priority, task.getPriority());
		}
	}
    
    /**
     * The comparison method invoked when sorting criteria is by task ID.
     * 
     * @param task
     *            the Task object to compare to
     * @return 0 if the Task compared to is equal to itself; a value less than 0
     *         if the Task compared to comes after itself; and a value more than
     *         0 if the Task compared to comes before itself.
     * @@author A0129660A
     */
    private int compareById(Task task) {
    	return Integer.compare(_id, task.getId());
    }

}