package multiElevator;

public class EndOfRequestsException extends Exception {
    /* @OVERVIEW: 为了让线程在无请求可处理的情况下利用 try catch 跳出仿真循环，设置此 Exception
    * */
    EndOfRequestsException() {

    }
}
