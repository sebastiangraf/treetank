package com.treetank.cdl;

import java.io.File;

import javax.xml.stream.XMLEventReader;

import com.treetank.access.Database;
import com.treetank.api.IDatabase;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.service.xml.serialize.XMLSerializer;
import com.treetank.service.xml.serialize.XMLSerializer.XMLSerializerBuilder;
import com.treetank.service.xml.shredder.EShredderInsert;
import com.treetank.service.xml.shredder.XMLShredder;

public final class SerializeTNK {

    public static void main(String... args) throws Exception {
        if (args.length != 1) {
            System.out.println("Usage: java -jar CDL \"TTToStore.tnk\"");
            System.exit(-1);
        }
        System.out.print("Serializing '" + args[0] + "... ");
        final long time = System.currentTimeMillis();

        // File setup
        final File storeFile = new File(args[0]);

        // Wtx setup
        final IDatabase db = Database.openDatabase(storeFile);
        final ISession session = db.getSession();

        final XMLSerializerBuilder builder = new XMLSerializerBuilder(session, System.out);
        final XMLSerializer serializer = builder.build();

        serializer.call();
        session.close();
        db.close();

        System.out.println(" done [" + (System.currentTimeMillis() - time) + "ms].");
    }
}
