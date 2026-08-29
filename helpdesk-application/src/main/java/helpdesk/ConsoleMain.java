package helpdesk;

public class ConsoleMain {
    public static void main(String[] args) {
        try {
            HelpDesk helpDesk = DemoData.createHelpDeskWithSampleData();
            ConsoleApp application = new ConsoleApp(helpDesk);
            application.run();
        } catch (HelpDeskException | IllegalArgumentException exception) {
            System.out.println("The console application could not start: " + exception.getMessage());
        }
    }
}
