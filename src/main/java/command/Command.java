package command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static consts.Patterns.*;

public class Command {
    private final String command;
    private CommandType type;
    private String error;
    private boolean isSyntaxicallyCorrect;
    private Matcher matcher;

    public Command(String command){
        this.command = command;
        type = CommandType.UNKNOWN;
        isSyntaxicallyCorrect = false;
        error = "Unknown command";
        checkCommand();
    }

    private void checkCommand() {
        if(IAMHERECMD.matcher(command).matches()) {
            type = CommandType.IAMHERE;
            isSyntaxicallyCorrect = checkIAmHereCmd();
        } else if(NOTIFYCMD.matcher(command).matches()) {
            type = CommandType.NOTIFY;
            isSyntaxicallyCorrect = checkNotifyCmd();
        } else if(CURCONFIGCMD.matcher(command).matches()) {
            type = CommandType.CURCONFIG;
            isSyntaxicallyCorrect = checkCurConfigCmd();
        } else if(STATEREQCMD.matcher(command).matches()) {
            type = CommandType.STATEREQ;
            isSyntaxicallyCorrect = checkStateReqCmd();
        } else if(STATERESPCMD.matcher(command).matches()) {
            type = CommandType.STATERESP;
            isSyntaxicallyCorrect = checkStateRespCmd();
        } else if(ADDSRVCMD.matcher(command).matches()) {
            type = CommandType.ADDSRV;
            isSyntaxicallyCorrect = checkAddSrvCmd();
        } else if(OKCMD.matcher(command).matches()) {
            type = CommandType.OK;
            isSyntaxicallyCorrect = checkOkCmd();
        } else if(ERRCMD.matcher(command).matches()) {
            type = CommandType.ERR;
            isSyntaxicallyCorrect = checkErrCmd();
        } else if(LISTSRVCMD.matcher(command).matches()) {
            type = CommandType.LISTSRV;
            isSyntaxicallyCorrect = checkListSrvCmd();
        } else if(SRVCMD.matcher(command).matches()) {
            type = CommandType.SRV;
            isSyntaxicallyCorrect = checkSrvCmd();
        } else if(STATESRVCMD.matcher(command).matches()) {
            type = CommandType.STATESRV;
            isSyntaxicallyCorrect = checkStateSrvCmd();
        } else if(STATECMD.matcher(command).matches()) {
            type = CommandType.STATE;
            isSyntaxicallyCorrect = checkStateCmd();
        }
    }

    public String getCommand() {
        return command;
    }

    public CommandType getType() {
        return type;
    }

    public boolean isSyntaxicallyCorrect() {
        return isSyntaxicallyCorrect;
    }

    public String getError() {
        return error;
    }

    public Matcher getMatcher() {
        return matcher;
    }

    private boolean checkIAmHereCmd() {
        if(!(matcher = Pattern.compile("IAMHERE" + sp + ".*").matcher(command)).matches()) return spaceMissing("IAMHERE");
        if(!(matcher = Pattern.compile("IAMHERE" + sp + protocol + ".*").matcher(command)).matches()) return badProtocol();
        if(!(matcher = Pattern.compile("IAMHERE" + sp + protocol + sp + ".*").matcher(command)).matches()) return spaceMissing("PROTOCOL");
        if(!(matcher = Pattern.compile("IAMHERE" + sp + protocol + sp + port).matcher(command)).matches()) return badPort();
        return true;
    }

    private boolean checkNotifyCmd() {
        if(!(matcher = Pattern.compile("NOTIFY" + sp + ".*").matcher(command)).matches()) return spaceMissing("NOTIFY");
        if(!(matcher = Pattern.compile("NOTIFY" + sp + protocol + ".*").matcher(command)).matches()) return badProtocol();
        if(!(matcher = Pattern.compile("NOTIFY" + sp + protocol + sp + ".*").matcher(command)).matches()) return spaceMissing("PROTOCOL");
        if(!(matcher = Pattern.compile("NOTIFY" + sp + protocol + sp + port).matcher(command)).matches()) return badPort();
        return true;
    }

    private boolean checkCurConfigCmd() {
        if(!(matcher = Pattern.compile("CURCONFIG" + "(" + sp + augmented_url + "){0,100}").matcher(command)).matches()) return badAugmentedUrl();
        return true;
    }

    private boolean checkStateReqCmd() {
        if(!(matcher = Pattern.compile("STATEREQ" + sp + ".*").matcher(command)).matches()) return spaceMissing("STATEREQ");
        if(!(matcher = Pattern.compile("STATEREQ" + sp + id).matcher(command)).matches()) return badId();
        return true;
    }

    private boolean checkStateRespCmd() {
        if(!(matcher = Pattern.compile("STATERESP" + sp + ".*").matcher(command)).matches()) return spaceMissing("STATERESP");
        if(!(matcher = Pattern.compile("STATERESP" + sp + id + ".*").matcher(command)).matches()) return badId();
        if(!(matcher = Pattern.compile("STATERESP" + sp + id + sp + ".*").matcher(command)).matches()) return spaceMissing("ID");
        if(!(matcher = Pattern.compile("STATERESP" + sp + id + sp + state).matcher(command)).matches()) return badState();
        return true;
    }

    private boolean checkAddSrvCmd() {
        if(!(matcher = Pattern.compile("ADDSRV" + sp + ".*").matcher(command)).matches()) return spaceMissing("ADDSRV");
        if(!(matcher = Pattern.compile("ADDSRV" + sp + augmented_url).matcher(command)).matches()) return badAugmentedUrl();
        return true;
    }

    private boolean checkOkCmd() {
        if(!(matcher = Pattern.compile("[\\u002B]OK" + "(" + sp + message + ")?").matcher(command)).matches()) return badOkResponse();
        return true;
    }

    private boolean checkErrCmd() {
        if(!(matcher = Pattern.compile("[\\u002D]ERR" + "(" + sp + message + ")?").matcher(command)).matches()) return badErrResponse();
        return true;
    }

    private boolean checkListSrvCmd() {
        if(!(matcher = Pattern.compile("LISTSRV").matcher(command)).matches()) return badListSrvCmd();
        return true;
    }

    private boolean checkSrvCmd() {
        if(!(matcher = Pattern.compile("SRV" + "(" + sp + id + "){0,100}").matcher(command)).matches()) return badSrvCmd();
        return true;
    }

    private boolean checkStateSrvCmd() {
        if(!(matcher = Pattern.compile("STATESRV" + sp + ".*").matcher(command)).matches()) return spaceMissing("STATESRV");
        if(!(matcher = Pattern.compile("STATESRV" + sp + id).matcher(command)).matches()) return badId();
        return true;
    }

    private boolean checkStateCmd() {
        if(!(matcher = Pattern.compile("STATE" + sp + ".*").matcher(command)).matches()) return spaceMissing("STATE");
        if(!(matcher = Pattern.compile("STATE" + sp + id + ".*").matcher(command)).matches()) return badId();
        if(!(matcher = Pattern.compile("STATE" + sp + id + sp + ".*").matcher(command)).matches()) return spaceMissing("ID");
        if(!(matcher = Pattern.compile("STATE" + sp + id + sp + url + ".*").matcher(command)).matches()) return badUrl();
        if(!(matcher = Pattern.compile("STATE" + sp + id + sp + url + sp + ".*").matcher(command)).matches()) return spaceMissing("URL");
        if(!(matcher = Pattern.compile("STATE" + sp + id + sp + url + sp + state).matcher(command)).matches()) return badState();
        return true;
    }

    private boolean badUrl() {
        error = "URL is not correctly formated";
        return false;
    }

    private boolean badOkResponse() {
        error = "OK response is not correctly formated";
        return false;
    }

    private boolean badErrResponse() {
        error = "ERR response is not correctly formated";
        return false;
    }

    private boolean badListSrvCmd() {
        error = "LISTSRV is not correctly formated";
        return false;
    }

    private boolean badSrvCmd() {
        error = "SRV is not correctly formated";
        return false;
    }

    private boolean badState() {
        error = "State is invalid. It should be \"OK\" or \"ALARM\" or \"DOWN\"";
        return false;
    }

    private boolean badId() {
        error = "ID is invalid. It should be 5 to 10 characters or digits";
        return false;
    }

    private boolean badAugmentedUrl() {
        error = "An augmented url is not correctly formed";
        return false;
    }

    private boolean badProtocol() {
        error = "Protocol invalid. It should be 3 to 15 characters or digits";
        return false;
    }

    private boolean badPort() {
        error = "Port invalid. It should be 1 to 5 digits";
        return false;
    }

    private boolean spaceMissing(String after) {
        error = "Space missing after " + after;
        return false;
    }
}
