package com.treetank;

import java.io.File;

public interface ITestConstants {

    public static final File PATH1 = new File(new StringBuilder(File.separator)
            .append("tmp").append(File.separator).append("tnk").append(
                    File.separator).append("path1").toString());

    public static final File PATH2 = new File(new StringBuilder(File.separator)
            .append("tmp").append(File.separator).append("tnk").append(
                    File.separator).append("path2").toString());

    public static final File NON_EXISTING_PATH = new File(new StringBuilder(
            File.separator).append("tmp").append(File.separator).append("tnk")
            .append(File.separator).append("NonExistingSessionTest").toString());

    public static final File TEST_INSERT_CHILD_PATH = new File(
            new StringBuilder(File.separator).append("tmp").append(
                    File.separator).append("tnk").append(File.separator)
                    .append("InsertChildSessionTest").toString());

    public static final File TEST_REVISION_PATH = new File(new StringBuilder(
            File.separator).append("tmp").append(File.separator).append("tnk")
            .append(File.separator).append("RevisionSessionTest").toString());

    public static final File TEST_SHREDDED_REVISION_PATH = new File(
            new StringBuilder(File.separator).append("tmp").append(
                    File.separator).append("tnk").append(File.separator)
                    .append("ShreddedRevisionSessionTest").toString());

    public static final File TEST_EXISTING_PATH = new File(new StringBuilder(
            File.separator).append("tmp").append(File.separator).append("tnk")
            .append(File.separator).append("ExistingSessionTest").toString());

    public static final String REST_SERVICE1 = "test1";
    public static final String REST_SERVICE2 = "test2";
}
