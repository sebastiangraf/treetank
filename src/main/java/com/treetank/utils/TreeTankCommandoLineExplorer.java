package com.treetank.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.session.Session;

/**
 * This class acts as a commando line interface to navigate through a treetank
 * structure.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class TreeTankCommandoLineExplorer {

    private TreeTankCommandoLineExplorer() {
        // Not used over here.
    }

    /**
     * DELIM-Constant for commands.
     */
    private static final String COMMANDDELIM = ":";

    /**
     * Finding file for a given command.z
     * 
     * @param commandLine
     *            the line to be analysed
     * @return the corresponding file
     */
    private static File findFile(final String commandLine) {
        final String[] command = commandLine.split(COMMANDDELIM);
        if (command.length != 2) {
            return null;
        }
        return new File(command[1]);
    }

    /**
     * Main for all the knowledge and input.
     * 
     * @param args
     *            only one arg allowed, the tnk file
     * @throws Exception
     *             of any kind
     */
    public static void main(final String[] args) throws Exception {
        ISession session = null;
        IReadTransaction rtx = null;
        if (args.length > 0) {

            long revision = 0;
            if (args.length > 1) {
                revision = Long.parseLong(args[1]);
            }

            final File file = new File(args[0]);
            session = Session.beginSession(file);
            if (revision != 0) {
                rtx = session.beginWriteTransaction();
            } else {
                rtx = session.beginReadTransaction(revision);
            }
        } else {
            System.out
                    .println("Usage: java TreeTankCommandoLineExplorer \"tnk-file\" [revision] (if revision not given, explorer works in write mode");
            System.exit(-1);
        }

        try {
            System.out.println("Welcome to TTCommand");
            System.out.println("Type in \"help\" for help for usage..");

            String line = null;
            final BufferedReader bufferIn = new BufferedReader(
                    new InputStreamReader(System.in));
            System.out.print(">");
            while ((line = bufferIn.readLine()) != null) {

                final Command command = Command.toCommand(line);
                switch (command) {
                case LOGIN:
                    if (rtx != null) {
                        rtx.close();
                    }
                    if (session != null) {
                        session.close();
                    }
                    final File file = findFile(line);
                    if (file != null) {
                        session = Session.beginSession(file);
                        rtx = session.beginReadTransaction();
                        System.out.println(command.executeCommand(rtx));
                    } else {
                        System.out
                                .println("Invalid path to tt-file! Please use other!");
                    }
                    break;
                case LOGOUT:
                    System.out.println(command.executeCommand(rtx));
                    if (rtx != null) {
                        rtx.close();
                    }
                    if (session != null) {
                        session.close();
                    }
                    break;
                case EXIT:
                    System.out.println(command.executeCommand(rtx));
                    if (rtx != null) {
                        rtx.close();
                    }
                    if (session != null) {
                        session.close();
                    }
                    System.exit(1);
                    break;
                default:
                    if (session == null || rtx == null) {
                        System.out.println(new StringBuilder(
                                "No database loaded!, Please use ").append(
                                Command.LOGIN.command).append(
                                " to load tt-database").toString());
                    } else {
                        System.out.println(command.executeCommand(rtx));
                    }
                    System.out.print(">");
                }
            }

        } catch (final Exception e) {
            e.printStackTrace();
            if (rtx != null) {
                rtx.close();
            }
            if (session != null) {
                session.close();
            }
            System.exit(-1);
        }

    }

    /**
     * Enums for known Commands.
     * 
     * @author Sebastian Graf, University of Konstanz
     * 
     */
    private enum Command {
        HELP("help") {
            @Override
            String executeCommand(final IReadTransaction currentRtx,
                    final String parameter) {
                final StringBuilder builder = new StringBuilder("Help for ");
                if (parameter.equals(INFO.command)) {
                    builder.append("info:\n");
                    builder
                            .append(
                                    "prints out nodeKey, child count, parent key, ")
                            .append(
                                    "first child key, left sibling key, right sibling key\n");
                } else if (parameter.equals(CONTENT.command)) {
                    builder.append("content:\n");
                    builder
                            .append("prints out kind of node plus relevant content\n");
                } else if (parameter.equals(LOGOUT.command)) {
                    builder.append("logout:\n");
                    builder.append("Logout from database\n");
                } else if (parameter.equals(LOGIN.command)) {
                    builder.append("login:\n");
                    builder.append("Parameter is the path to the tt-database")
                            .append("\"login:[path]\"\n");
                } else if (parameter.equals(EXIT.command)) {
                    builder.append("exit:\n");
                    builder.append("Exits the program\n");
                } else if (parameter.equals(MOVE.command)) {
                    builder.append("move:\n");
                    builder.append("Below a concrete parameter list\n");
                    builder.append("up\t\t:\tGo to father if possible\n");
                    builder
                            .append("down\t\t:\tGo to first child if possible\n");
                    builder
                            .append("left\t\t:\tGo to left sibling if possible\n");
                    builder
                            .append("right\t\t:\tGo to right sibling if possible\n");
                    builder
                            .append("root\t\t:\tGo to document root if possible\n");
                    builder
                            .append(
                                    "[nodekey]\t:\tGo to specified node key if possible, ")
                            .append("[nodekey] has to be a long\n");
                } else {
                    builder
                            .append("common usage\n Usage: [COMMAND]:[PARAMETER]\n");
                    builder.append("For concrete parameter-list, type ")
                            .append(HELP.command).append(COMMANDDELIM).append(
                                    "[COMMAND]\n");
                    builder.append("Below a list of all commands:\n");
                    builder.append(LOGIN.command).append("\t:\t").append(
                            "Login into database.\n");
                    builder.append(LOGOUT.command).append("\t:\t").append(
                            "Logout from database.\n");
                    builder.append(EXIT.command).append("\t:\t").append(
                            "Exits the programm.\n");
                    builder.append(INFO.command).append("\t:\t").append(
                            "Offers info about the current node.\n");
                    builder.append(MOVE.command).append("\t:\t").append(
                            "Moving to given node.\n");
                }
                return builder.toString();
            }
        },
        CONTENT("content") {
            @Override
            String executeCommand(final IReadTransaction currentRtx,
                    final String parameter) {
                final StringBuilder builder = new StringBuilder("Kind: ");
                switch (currentRtx.getNode().getKind()) {
                case IReadTransaction.ELEMENT_KIND:
                    builder.append("Element\n");
                    builder.append(currentRtx.nameForKey(currentRtx.getNode()
                            .getNameKey()));
                    break;
                case IReadTransaction.ATTRIBUTE_KIND:
                    builder.append("Attribute\n");
                    builder.append(currentRtx.nameForKey(currentRtx.getNode()
                            .getNameKey()));
                    builder.append("=");
                    builder.append(TypedValue.parseString(currentRtx.getNode()
                            .getRawValue()));
                    break;
                case IReadTransaction.TEXT_KIND:
                    builder.append("Text\n");
                    builder.append(TypedValue.parseString(currentRtx.getNode()
                            .getRawValue()));
                    break;
                case IReadTransaction.NAMESPACE_KIND:
                    builder.append("Namespace\n");
                    if (currentRtx
                            .nameForKey(currentRtx.getNode().getNameKey())
                            .length() > 0) {
                        builder.append(currentRtx.nameForKey(currentRtx
                                .getNode().getNameKey()));
                        builder.append("=");
                    }
                    builder.append(currentRtx.nameForKey(currentRtx.getNode()
                            .getURIKey()));
                    break;
                case IReadTransaction.PROCESSING_KIND:
                    builder.append("Processing instruction\n");
                    break;
                case IReadTransaction.COMMENT_KIND:
                    builder.append("Comment\n");
                    break;
                case IReadTransaction.ROOT_KIND:
                    builder.append("Document Root\n");
                    break;
                default:
                    builder.append("unknown!");
                }
                return builder.toString();
            }
        },
        INFO("info") {
            @Override
            String executeCommand(final IReadTransaction currentRtx,
                    final String parameter) {
                final StringBuilder builder = new StringBuilder();
                builder.append(currentRtx.toString());
                return builder.toString();
            }
        },
        LOGIN("login") {
            @Override
            String executeCommand(final IReadTransaction currentRtx,
                    final String parameter) {
                return new StringBuilder("Loggin into database ").append(
                        parameter).append("\n").toString();
            }
        },
        LOGOUT("logout") {
            @Override
            String executeCommand(final IReadTransaction currentRtx,
                    final String parameter) {
                return new StringBuilder("Logout from database.").toString();
            }
        },
        EXIT("exit") {
            @Override
            String executeCommand(final IReadTransaction currentRtx,
                    final String parameter) {
                return new StringBuilder("Exiting the program.").toString();
            }
        },
        MOVE("move") {
            @Override
            String executeCommand(final IReadTransaction currentRtx,
                    final String parameter) {
                boolean succeed = false;
                final StringBuilder builder = new StringBuilder("Move to ");
                if (parameter.equals("up")) {
                    builder.append("parent ");
                    succeed = currentRtx.moveToParent();
                } else if (parameter.equals("down")) {
                    builder.append("first child ");
                    succeed = currentRtx.moveToFirstChild();
                } else if (parameter.equals("right")) {
                    builder.append("right sibling ");
                    succeed = currentRtx.moveToRightSibling();
                } else if (parameter.equals("left")) {
                    builder.append("left sibling ");
                    succeed = currentRtx.moveToLeftSibling();
                } else if (parameter.equals("root")) {
                    builder.append("document root ");
                    succeed = currentRtx.moveToDocumentRoot();
                } else {
                    try {
                        final long nodeKey = Long.parseLong(parameter);
                        builder.append("node with key ").append(nodeKey)
                                .append(" ");
                        succeed = currentRtx.moveTo(nodeKey);
                    } catch (final NumberFormatException e) {
                        builder.append("invalid node ");
                        succeed = false;
                    }
                }
                if (succeed) {
                    builder.append("succeeded");
                } else {
                    builder.append("not succeeded");
                }

                return builder.toString();
            }
        },
        MODIFICATION("modification") {
            @Override
            String executeCommand(final IReadTransaction currentRtx,
                    final String parameter) {
                final StringBuilder builder = new StringBuilder("Insert ");
                try {
                    if (currentRtx instanceof IWriteTransaction) {
                        IWriteTransaction wtx = (IWriteTransaction) currentRtx;

                        if (parameter.equals("commit")) {
                            wtx.commit();
                            builder
                                    .append(
                                            " operation: commit succeed. New revision-number is ")
                                    .append(wtx.getRevisionNumber());
                        } else if (parameter.equals("abort")) {
                            wtx.abort();
                            builder
                                    .append(
                                            " operation: abort succeed. Old revision-number is ")
                                    .append(wtx.getRevisionNumber());
                        }

                    } else {
                        builder
                                .append(" not succeed, Please login with write-right (that means without revision parameter");
                    }
                } catch (final TreetankException exc) {
                    builder.append(" throws exception: ").append(exc);
                }
                return builder.toString();
            }
        },
        NOVALUE("") {
            @Override
            String executeCommand(final IReadTransaction currentRtx,
                    final String parameter) {
                return new StringBuilder("Command not known. Try ").append(
                        Command.HELP.getCommand()).append(
                        " for known commands!").toString();
            }
        };

        private final String command;

        private String parameter = "";

        Command(final String paramCommand) {
            command = paramCommand;
        }

        private static final Command toCommand(final String commandString) {
            try {
                final String[] commandStrings = commandString
                        .split(COMMANDDELIM);
                final Command command = valueOf(commandStrings[0].toUpperCase());
                if (commandStrings.length == 2) {
                    command.setAdvise(commandStrings[1].toLowerCase());
                }

                return command;
            } catch (Exception e) {
                return NOVALUE;
            }
        }

        private final String executeCommand(final IReadTransaction read) {
            return executeCommand(read, parameter);
        }

        /**
         * Executing a command.
         * 
         * @param currentRtx
         *            on which the command should be executed
         * @return a String as a result
         */
        abstract String executeCommand(final IReadTransaction currentRtx,
                final String parameter);

        /**
         * Getter for field command.
         * 
         * @return the command
         */
        private final String getCommand() {
            return command;
        }

        /**
         * Setter for field advise.
         * 
         * @param paramParameter
         *            to be set.
         */
        private final void setAdvise(final String paramParameter) {
            parameter = paramParameter;
        }

    }
}
