package parul.practice.WAL;

public class InvalidCheckSumException extends Exception{

    public void printMsg(String str){
        System.out.println("Invalid CheckSum Exception :"+str);
    }
}
