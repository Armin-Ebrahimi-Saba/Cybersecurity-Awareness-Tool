package runners;

import services.commands.ImportMissionsCommand;

import java.io.IOException;

public class RunImportMissionCommand {

    public static void main(String[] args) throws IOException {
        if (args.length > 1){
            ImportMissionsCommand.MISSIONS_PATH = args[0];
            ImportMissionsCommand.MISSIONS_INFORMATION_PATH = args[1];
            new ImportMissionsCommand();
        }
    }
}
