package com.treetank.bench.slidingSnapshot;


public class WindowModifier {

//    private final static int mProb = 50;
//    private static Properties props;
//
//    private static int MOD_SIZE = 3000;
//
//    private IWriteTransaction wtx;
//    private ISession session;
//    private IDatabase database;
//
//    @BeforeEachRun
//    public void setUp() {
//        try {
//            XMLShredder.main(CommonStuff.XMLPath.getAbsolutePath(),
//                    CommonStuff.PATH1.getAbsolutePath());
//        } catch (Exception exc) {
//            exc.printStackTrace();
//        }
//
//    }
//
//    @AfterEachRun
//    public void tearDown() {
//        try {
//            wtx.close();
//            session.close();
//            database.close();
//            Database.truncateDatabase(CommonStuff.PATH1);
//        } catch (TreetankException exc) {
//
//        }
//    }
//
//    @Bench
//    public void benchSeq() {
//        try {
//            final SessionConfiguration conf = new SessionConfiguration(
//                    CommonStuff.PATH1, props);
//            session = Session.beginSession(conf);
//            wtx = session.beginWriteTransaction();
//
//            for (int i = 0; i < MOD_SIZE; i++) {
//                do {
//                    long nextKey = 0;
//                    do {
//                        nextKey = CommonStuff.ran.nextLong();
//                        if (nextKey < 0) {
//                            nextKey = nextKey * -1;
//                        }
//                        nextKey = nextKey % wtx.getNodeCount();
//                    } while (nextKey == 0);
//
//                    wtx.moveTo(nextKey);
//
//                } while (!wtx.getNode().isElement());
//
//                final IAxis axis = new DescendantAxis(wtx);
//                while (axis.hasNext()) {
//                    axis.next();
//                    if (wtx.getNode().isElement()) {
//                        wtx.setName(CommonStuff.getString());
//                        i++;
//                        if (CommonStuff.ran.nextInt(100) < mProb) {
//                            wtx.commit();
//                        }
//                        if (i >= MOD_SIZE) {
//                            break;
//                        }
//
//                    }
//
//                }
//            }
//            wtx.commit();
//        } catch (TreetankException exc) {
//            exc.printStackTrace();
//        }
//    }
//
//    @Bench
//    public void benchRandom() {
//        try {
//            final SessionConfiguration conf = new SessionConfiguration(
//                    CommonStuff.PATH1, props);
//            session = Session.beginSession(conf);
//            wtx = session.beginWriteTransaction();
//            wtx.insertElementAsFirstChild(CommonStuff.getString(), "");
//            for (int i = 0; i < MOD_SIZE; i++) {
//                do {
//                    long nextKey = 0;
//                    do {
//                        nextKey = CommonStuff.ran.nextLong();
//                        if (nextKey < 0) {
//                            nextKey = nextKey * -1;
//                        }
//                        nextKey = nextKey % wtx.getNodeCount();
//                    } while (nextKey == 0);
//
//                    wtx.moveTo(nextKey);
//
//                } while (!wtx.getNode().isElement());
//                wtx.setName(CommonStuff.getString());
//                if (CommonStuff.ran.nextInt(100) < mProb) {
//                    wtx.commit();
//                }
//            }
//            wtx.commit();
//        } catch (TreetankException exc) {
//            exc.printStackTrace();
//        }
//    }
//
//    public static void main(final String[] args) throws TreetankUsageException {
//        CommonStuff.recursiveDelete(CommonStuff.RESULTFOLDER);
//        CommonStuff.RESULTFOLDER.mkdirs();
//        for (int i = 1; i <= 34; i++) {
//            props = new Properties();
//            props.setProperty(EDatabaseSetting.MILESTONE_REVISION.name(), Integer.toString(i));
//
//            final WindowModifier toBench = new WindowModifier();
//            final Benchmark benchmark = new Benchmark(new BenchmarkConfig());
//            benchmark.add(toBench);
//            final BenchmarkResult res = benchmark.run();
//            final StringBuilder pathBuilder = new StringBuilder(
//                    CommonStuff.RESULTFOLDER.getAbsolutePath())
//                    .append(File.separator);
//            if (i < 100) {
//                pathBuilder.append("0");
//                if (i < 10) {
//                    pathBuilder.append("0");
//                }
//            }
//            pathBuilder.append(i);
//            final File file = new File(pathBuilder.toString());
//            file.mkdirs();
//            final CSVOutput output = new CSVOutput(file);
//            // final TabularSummaryOutput tab = new TabularSummaryOutput();
//            output.visitBenchmark(res);
//            // tab.visitBenchmark(res);
//        }
//
//    }
//
//    private final static int RUNS = 5;
//    private final static AbstractMeter[] METERS = {
//            new TimeMeter(Time.MilliSeconds),
//            new FileSizeMeter(CommonStuff.PATH1) };
//    private final static AbstractOutput[] OUTPUT = {};
//    private final static KindOfArrangement ARRAN = KindOfArrangement.NoArrangement;
//    private final static double gcProb = 1.0d;
//
//    static class BenchmarkConfig extends AbstractConfig {
//
//        protected BenchmarkConfig() {
//            super(RUNS, METERS, OUTPUT, ARRAN, gcProb);
//
//        }
//    }
}
