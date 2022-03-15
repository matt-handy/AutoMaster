package handy.rp;

public class OutcomeNotification {

	public final String humanMessage;
	public final boolean outcome;
	
	public OutcomeNotification(String humanMessage, boolean outcome) {
		this.humanMessage = humanMessage;
		this.outcome = outcome;
	}
}
