package multiElevator;

public class InputException extends RuntimeException {
	/* @OVERVIEW: 用于产生无效输入的异常
	* */
	String message;
	InputException(String message) {
		this.message = message;
	}
}
