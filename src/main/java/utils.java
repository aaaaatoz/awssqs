/**
 * Created by rafaxu on 7/29/17.
 */

import  java.io.BufferedReader;
import java .io.IOException;
import java.io.InputStreamReader;

public class utils {
    public static void Prompt(String promptinformation) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(System.in));
            while(true) {
                System.out.print("\n" + promptinformation + "Want to go ahead(Y/N):");
                String input = br.readLine();
                if (input.toLowerCase().equals("n")) {
                    System.out.print("Do you really want to exit(Yes):");
                    input = br.readLine();
                    if (input.toLowerCase().equals("yes")) {
                        System.out.println("Demo Finished... exit");
                        System.exit(-1);
                    } else {
                        System.out.println("exit not confirmed... asking again");
                    }
                } else if (input.equals("Y") || input.equals("y")) {
                    return;
                } else {
                    System.out.println("You should only input Y or N to indicate if you want to go ahead");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

     public static void main(String[] aregs) {
        Prompt("");
     }
 }
