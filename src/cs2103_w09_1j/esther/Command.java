package cs2103_w09_1j.esther;

/**
 * ========= [ COMMAND OBJECT DEFINITIONS ] =========
 * This class contains the representation of the
 * command object that will be passed around by the
 * program.
 * 
 * @author Tay Guo Qiang
 */

import java.util.HashMap;
import java.util.Map;

import cs2103_w09_1j.esther.Task.TaskField;

public class Command {

	private String _commandName;
	private HashMap<String, String> _parameters;

	public enum CommandKey {
		ADD("add"), UPDATE("update"), DELETE("delete"), SEARCH("search"), SHOW("show"), SORT("sort"), COMPLETE(
				"complete"), UNDO("undo"), HELP("help"), SET("set");

		private String commandKeyName;
		private static final Map<String, CommandKey> lookup = new HashMap<String, CommandKey>();

		// @@author A0126000H
		private CommandKey(String _commandKeyName) {
			this.commandKeyName = _commandKeyName;
		}

		static {
			// Create reverse lookup hash map
			for (CommandKey _commandKeyName : CommandKey.values()) {
				lookup.put(_commandKeyName.getCommandKeyName(), _commandKeyName);
			}
		}

		// @@author A0126000H
		public String getCommandKeyName() {
			return commandKeyName;
		}

		/**
		 * This operations reversely gets the CommandKey from the value.
		 * 
		 * @param commandValue
		 *            The input given by the user.
		 * @return The command based on the input.
		 * @@author A0126000H
		 */
		public static CommandKey get(String commandKeyValue) {
			return lookup.get(commandKeyValue);
		}

	}

	// @@author A0126000H
	public Command() {
		this._commandName = "";
		this._parameters = new HashMap<String, String>();
	}

	/**
	 * Creates a Command object with the command to execute as well as the
	 * parameters needed to create a Task object.
	 * 
	 * @param command
	 *            the operation desired by the user
	 * @param parameters
	 *            the arguments supplied by the user
	 * @@author A0130749A
	 */
	public Command(String command, HashMap<String, String> parameters) {
		setCommand(command);
		setParameters(parameters);
	}

	/**
	 * Getter method for the command that user wishes to execute.
	 * 
	 * Logic will use this to determine the command to execute on the task.
	 * 
	 * @return the command to execute
	 * @@author A0130749A
	 */
	public String getCommand() {
		return _commandName;
	}

	/**
	 * Setter method for the command that user wishes to execute.
	 * 
	 * @param command
	 *            the command to execute
	 * @@author A0130749A
	 */
	public void setCommand(String command) {
		_commandName = command;
	}

	/**
	 * Getter method for user-supplied parameters.
	 * 
	 * @return the representation of user-supplied parameters
	 * @@author A0130749A
	 */
	public HashMap<String, String> getParameters() {
		return _parameters;
	}

	/**
	 * Returns the String value associated with the parameter key.
	 * 
	 * @param parameter
	 *            the parameter being requested
	 * @return the String value associated with the parameter
	 * @@author A0130749A
	 */
	public String getSpecificParameter(String parameter) {
		String value;
		TaskField field = TaskField.get(parameter);
		switch (field) {
		case NAME:
			value = _parameters.get(parameter);
			break;

		case UPDATENAME:
			value = _parameters.get(parameter);
			break;

		case STARTDATE:
			value = _parameters.get(parameter);
			break;

		case ENDDATE:
			value = _parameters.get(parameter);
			break;

		case STARTTIME:
			value = _parameters.get(parameter);
			break;

		case ENDTIME:
			value = _parameters.get(parameter);
			break;

		case PRIORITY:
			value = _parameters.get(parameter);
			break;

		case ID:
			value = _parameters.get(parameter);
			break;

		case COMPLETE:
			value = _parameters.get(parameter);
			break;

		case SHOW:
			value = _parameters.get(parameter);
			break;
		
		case KEYWORD:
			value = _parameters.get(parameter);
			break;
			
		case PATH:
			value = _parameters.get(parameter);
			break;

		default:
			value = "Unrecognized key.";
			break;
		}
		return value;
	}

	/**
	 * Checks if a parameter exists or not.
	 * 
	 * @param parameter
	 *            the parameter being requested
	 * @return true if parameter key does not map to null value; false
	 *         otherwise.
	 * @@author A0130749A
	 */
	public boolean hasParameter(String parameter) {
		return _parameters.containsKey(parameter);
	}

	/**
	 * Setter method for user-supplied parameters.
	 * 
	 * @param parameters
	 *            the representation of user-supplied parameters
	 * @@author A0130749A
	 */
	public void setParameters(HashMap<String, String> parameters) {
		_parameters = parameters;
	}

	// @@author A0126000H
	public void clear() {
		this._commandName = "";
		this._parameters.clear();
	}

	// @@author A0126000H
	public String addFieldToMap(String fieldName, String fieldValue) {
		return this._parameters.put(fieldName, fieldValue);
	}

}