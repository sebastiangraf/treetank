/**
 * Copyright (c) 2010, Distributed Systems Group, University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED AS IS AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 */

package org.treetank.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;


import org.slf4j.LoggerFactory;
import org.treetank.access.Database;
import org.treetank.api.IDatabase;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.exception.AbsTTException;

/**
 * This class acts as a commando line interface to navigate through a treetank
 * structure.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class TreeTankCommandoLineExplorer {

    /**
     * Log wrapper for better output.
     */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(LoggerFactory
        .getLogger(TreeTankCommandoLineExplorer.class));

    private TreeTankCommandoLineExplorer() {
        // Not used over here.
    }

    /**
     * DELIM-Constant for commands.
     */
    private static final String COMMANDDELIM = ":";

    /**
     * Finding file for a given command.z.
     * 
     * @param mCommandLine
     *            the line to be analysed
     * @return the corresponding file
     */
    private static File findFile(final String mCommandLine) {
        final String[] command = mCommandLine.split(COMMANDDELIM);
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
        IDatabase database = null;
        ISession session = null;
        IReadTransaction rtx = null;
        if (args.length > 0) {

            long revision = 0;
            if (args.length > 1) {
                revision = Long.parseLong(args[1]);
            }

            final File file = new File(args[0]);
            database = Database.openDatabase(file);
            session = database.getSession();
            if (revision != 0) {
                rtx = session.beginWriteTransaction();
            } else {
                rtx = session.beginReadTransaction(revision);
            }
        } else {
            System.out.println("Usage: java TreeTankCommandoLineExplorer \"tnk-file\" [revision] "
                + "(if revision not given, explorer works in write mode");
            System.exit(-1);
        }

        try {
            System.out.println("Welcome to TTCommand");
            System.out.println("Type in \"help\" for help for usage..");

            String line = null;
            final BufferedReader bufferIn = new BufferedReader(new InputStreamReader(System.in));
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
                        database = Database.openDatabase(file);
                        session = database.getSession();
                        rtx = session.beginReadTransaction();
                        System.out.println(command.executeCommand(rtx));
                    } else {
                        System.out.println("Invalid path to tt-file! Please use other!");
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
                        System.out.println(new StringBuilder("No database loaded!, Please use ").append(
                            Command.LOGIN.mCommand).append(" to load tt-database").toString());
                    } else {
                        System.out.println(command.executeCommand(rtx));
                    }
                    System.out.print(">");
                }
            }

        } catch (final Exception e) {
            LOGWRAPPER.error(e);
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
            String executeCommand(final IReadTransaction mCurrentRtx, final String mParameter) {
                final StringBuilder builder = new StringBuilder("Help for ");
                if (mParameter.equals(INFO.mCommand)) {
                    builder.append("info:\n");
                    builder.append("prints out nodeKey, child count, parent key, ").append(
                        "first child key, left sibling key, right sibling key\n");
                } else if (mParameter.equals(CONTENT.mCommand)) {
                    builder.append("content:\n");
                    builder.append("prints out kind of node plus relevant content\n");
                } else if (mParameter.equals(LOGOUT.mCommand)) {
                    builder.append("logout:\n");
                    builder.append("Logout from database\n");
                } else if (mParameter.equals(LOGIN.mCommand)) {
                    builder.append("login:\n");
                    builder.append("Parameter is the path to the tt-database").append("\"login:[path]\"\n");
                } else if (mParameter.equals(EXIT.mCommand)) {
                    builder.append("exit:\n");
                    builder.append("Exits the program\n");
                } else if (mParameter.equals(MOVE.mCommand)) {
                    builder.append("move:\n");
                    builder.append("Below a concrete parameter list\n");
                    builder.append("up\t\t:\tGo to father if possible\n");
                    builder.append("down\t\t:\tGo to first child if possible\n");
                    builder.append("left\t\t:\tGo to left sibling if possible\n");
                    builder.append("right\t\t:\tGo to right sibling if possible\n");
                    builder.append("root\t\t:\tGo to document root if possible\n");
                    builder.append("[nodekey]\t:\tGo to specified node key if possible, ").append(
                        "[nodekey] has to be a long\n");
                } else {
                    builder.append("common usage\n Usage: [COMMAND]:[PARAMETER]\n");
                    builder.append("For concrete parameter-list, type ").append(HELP.mCommand).append(
                        COMMANDDELIM).append("[COMMAND]\n");
                    builder.append("Below a list of all commands:\n");
                    builder.append(LOGIN.mCommand).append("\t:\t").append("Login into database.\n");
                    builder.append(LOGOUT.mCommand).append("\t:\t").append("Logout from database.\n");
                    builder.append(EXIT.mCommand).append("\t:\t").append("Exits the programm.\n");
                    builder.append(INFO.mCommand).append("\t:\t").append(
                        "Offers info about the current node.\n");
                    builder.append(MOVE.mCommand).append("\t:\t").append("Moving to given node.\n");
                }
                return builder.toString();
            }
        },
        CONTENT("content") {
            @Override
            String executeCommand(final IReadTransaction mCurrentRtx, final String mParameter) {
                final StringBuilder builder = new StringBuilder("Kind: ");
                switch (mCurrentRtx.getNode().getKind()) {
                case ELEMENT_KIND:
                    builder.append("Element\n");
                    builder.append(mCurrentRtx.nameForKey(mCurrentRtx.getNode().getNameKey()));
                    break;
                case ATTRIBUTE_KIND:
                    builder.append("Attribute\n");
                    builder.append(mCurrentRtx.nameForKey(mCurrentRtx.getNode().getNameKey()));
                    builder.append("=");
                    builder.append(TypedValue.parseString(mCurrentRtx.getNode().getRawValue()));
                    break;
                case TEXT_KIND:
                    builder.append("Text\n");
                    builder.append(TypedValue.parseString(mCurrentRtx.getNode().getRawValue()));
                    break;
                case NAMESPACE_KIND:
                    builder.append("Namespace\n");
                    if (mCurrentRtx.nameForKey(mCurrentRtx.getNode().getNameKey()).length() > 0) {
                        builder.append(mCurrentRtx.nameForKey(mCurrentRtx.getNode().getNameKey()));
                        builder.append("=");
                    }
                    builder.append(mCurrentRtx.nameForKey(mCurrentRtx.getNode().getURIKey()));
                    break;
                case PROCESSING_KIND:
                    builder.append("Processing instruction\n");
                    break;
                case COMMENT_KIND:
                    builder.append("Comment\n");
                    break;
                case ROOT_KIND:
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
            String executeCommand(final IReadTransaction mCurrentRtx, final String mParameter) {
                final StringBuilder builder = new StringBuilder();
                builder.append(mCurrentRtx.toString());
                return builder.toString();
            }
        },
        LOGIN("login") {
            @Override
            String executeCommand(final IReadTransaction mCurrentRtx, final String mParameter) {
                return new StringBuilder("Loggin into database ").append(mParameter).append("\n").toString();
            }
        },
        LOGOUT("logout") {
            @Override
            String executeCommand(final IReadTransaction mCurrentRtx, final String mParameter) {
                return new StringBuilder("Logout from database.").toString();
            }
        },
        EXIT("exit") {
            @Override
            String executeCommand(final IReadTransaction mCurrentRtx, final String mParameter) {
                return new StringBuilder("Exiting the program.").toString();
            }
        },
        MOVE("move") {
            @Override
            String executeCommand(final IReadTransaction mCurrentRtx, final String mParameter) {
                boolean succeed = false;
                final StringBuilder builder = new StringBuilder("Move to ");
                if (mParameter.equals("up")) {
                    builder.append("parent ");
                    succeed = mCurrentRtx.moveToParent();
                } else if (mParameter.equals("down")) {
                    builder.append("first child ");
                    succeed = mCurrentRtx.moveToFirstChild();
                } else if (mParameter.equals("right")) {
                    builder.append("right sibling ");
                    succeed = mCurrentRtx.moveToRightSibling();
                } else if (mParameter.equals("left")) {
                    builder.append("left sibling ");
                    succeed = mCurrentRtx.moveToLeftSibling();
                } else if (mParameter.equals("root")) {
                    builder.append("document root ");
                    succeed = mCurrentRtx.moveToDocumentRoot();
                } else {
                    try {
                        final long nodeKey = Long.parseLong(mParameter);
                        builder.append("node with key ").append(nodeKey).append(" ");
                        succeed = mCurrentRtx.moveTo(nodeKey);
                    } catch (final NumberFormatException e) {
                        LOGWRAPPER.error(e);
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
            String executeCommand(final IReadTransaction mCurrentRtx, final String mParameter) {
                final StringBuilder builder = new StringBuilder("Insert ");
                try {
                    if (mCurrentRtx instanceof IWriteTransaction) {
                        final IWriteTransaction wtx = (IWriteTransaction)mCurrentRtx;

                        if (mParameter.equals("commit")) {
                            wtx.commit();
                            builder.append(" operation: commit succeed. New revision-number is ").append(
                                wtx.getRevisionNumber());
                        } else if (mParameter.equals("abort")) {
                            wtx.abort();
                            builder.append(" operation: abort succeed. Old revision-number is ").append(
                                wtx.getRevisionNumber());
                        }

                    } else {
                        builder.append(" not succeed, Please login with write-right "
                            + "(that means without revision parameter");
                    }
                } catch (final AbsTTException exc) {
                    LOGWRAPPER.error(exc);
                    builder.append(" throws exception: ").append(exc);
                }
                return builder.toString();
            }
        },
        NOVALUE("") {
            @Override
            String executeCommand(final IReadTransaction mCurrentRtx, final String mParameter) {
                return new StringBuilder("Command not known. Try ").append(Command.HELP.getCommand()).append(
                    " for known commands!").toString();
            }
        };

        private final String mCommand;

        private String mParameter = "";

        Command(final String paramCommand) {
            mCommand = paramCommand;
        }

        private static Command toCommand(final String mCommandString) {
            try {
                final String[] commandStrings = mCommandString.split(COMMANDDELIM);
                final Command command = valueOf(commandStrings[0].toUpperCase());
                if (commandStrings.length == 2) {
                    command.setAdvise(commandStrings[1].toLowerCase());
                }

                return command;
            } catch (final Exception e) {
                LOGWRAPPER.error(e);
                return NOVALUE;
            }
        }

        private String executeCommand(final IReadTransaction read) {
            return executeCommand(read, mParameter);
        }

        /**
         * Executing a command.
         * 
         * @param mCurrentRtx
         *            on which the command should be executed
         * @param parameter
         *            Parameter to executed
         * @return a String as a result
         */
        abstract String executeCommand(final IReadTransaction mCurrentRtx, final String parameter);

        /**
         * Getter for field command.
         * 
         * @return the command
         */
        private String getCommand() {
            return mCommand;
        }

        /**
         * Setter for field advise.
         * 
         * @param paramParameter
         *            to be set.
         */
        private void setAdvise(final String paramParameter) {
            mParameter = paramParameter;
        }

    }
}
