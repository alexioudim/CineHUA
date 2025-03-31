package gr.dit.hua.CineHua;

public class testMain {

    public static void main(String[] args) {
        String test  = "19:30";
        String replace = test.replace(":", "");

        int test_int = Integer.parseInt(replace);

        int runtime = 121;


        System.out.println(test_int);



    }
}
