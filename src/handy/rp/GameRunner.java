package handy.rp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public abstract class GameRunner {

	public void mainGameLoop(PrintWriter pw, BufferedReader br) {
		try {
		boolean stayInLoop = true;

		String nextCommand;
		while ((nextCommand = br.readLine()) != null && stayInLoop) {
			String args[] = nextCommand.split(" ");

			if (args.length < 1) {
				pw.println("Improper command");
				continue;
			}
			
			if(args[0].equalsIgnoreCase("quit")) {
				stayInLoop = false;
			}else if(args[0].equalsIgnoreCase("help")) {
				pw.println(getHelp());
			}else {
				processCommand(args, pw, br, nextCommand);
			}
		}
	} catch (IOException ex) {
			System.out.println("Something went wrong with interface, closing game.");
		}
	}
	
	public abstract String getHelp();
	
	public abstract void processCommand(String args[],PrintWriter pw, BufferedReader br, String rawCommand);
}
