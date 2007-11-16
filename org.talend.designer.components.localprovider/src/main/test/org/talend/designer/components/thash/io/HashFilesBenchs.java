// ============================================================================
//
// Copyright (C) 2006-2007 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//   
// ============================================================================
package org.talend.designer.components.thash.io;

import gnu.trove.THashMap;
import gnu.trove.TObjectHashingStrategy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.talend.designer.components.thash.Sizeof;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 * 
 * 
 * 
 * 
 * 
 * 
 */
public class HashFilesBenchs {

    public static MultiPointersMultiHashFiles hashFile;

    public static void main(String[] args) throws Exception {

        new HashFilesBenchs();

    }

    boolean randomRead = true;

    boolean readonly = false;

    boolean readAfterStore = true;

    String filePath = "/tmp/talend_hash";

    String folderStatsPath = "/home/amaumont/hash_benchs/";

    String fileHashBenchsBaseName = "HashBenchs";

    private THashMap hashMap;

    private long time1;

    private long time2;

    private FileOutputStream fileData;

    /**
     * DOC amaumont ReliabilityHashMapFileTest constructor comment.
     * 
     * @throws Exception
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public HashFilesBenchs() throws IOException, ClassNotFoundException, Exception {
        super();

        int[] nbItemsArray = new int[] { 
//                10000, 
//                100000, 
//                1000000, 
//                10000000, 
                30000000, 
                60000000, 
                100000000,
                };

        int[] nbFilesArray = new int[] { 
//                1, 
                10, 
                20, 
                40, 
                60, 
                80, 
                100, 
                150, 
                };// , };

        int[] pointersByFileArray = new int[] { 
                1, 
                10, 
                20, 
                40, 
                60, 
                80, 
                100, 
                200, 
                400, 
                800, 
                1000 };// , };

        // int loop = 1;
        // int loop = 10000;
        // int loop = 100000; // 100 000
        // int loop = 1000000; // 1 000 000
        // int loop = 10000000; // 10 000 000
        // int loop = 25000000;
        // int loop = 42000000;
        // int loop = 53000000;
        // int loop = 60000000;
        // int loop = 70000000;
        // int loop = 80000000;

        // change also in Bean.equals(...) class
        // hashFile = MultipleHashFile.getInstance();
        // hashFile = MultiplePointerSimpleHashFile.getInstance();
        // hashFile = DoubleHashFile.getInstance();

        StringBuilder sbPreview = new StringBuilder();
        
        NumberFormat nf = NumberFormat.getInstance(); 
        
        
        sbPreview.append("nbItems list: ");
        for (int i = 0; i < nbItemsArray.length; i++) {
            int j = nbItemsArray[i];
            sbPreview.append(nf.format(j));
            sbPreview.append("  |  ");
        }
        sbPreview.append("\n");
        
        sbPreview.append("nbFiles list: ");
        for (int i = 0; i < nbFilesArray.length; i++) {
            int j = nbFilesArray[i];
            sbPreview.append(nf.format(j));
            sbPreview.append("  |  ");
        }
        sbPreview.append("\n");
        
        sbPreview.append("pointersByFile list: ");
        for (int i = 0; i < pointersByFileArray.length; i++) {
            int j = pointersByFileArray[i];
            sbPreview.append(nf.format(j));
            sbPreview.append("  |  ");
        }
        sbPreview.append("\n");
        
        System.out.println(sbPreview.toString());
        
        openDataFile();

        try {
            for (int nbItemsIndex = 0; nbItemsIndex < nbItemsArray.length; nbItemsIndex++) {
                int nbItems = nbItemsArray[nbItemsIndex];

                System.out.println("Current items number: " + nf.format(nbItems));
                
                for (int j = 0; j < nbFilesArray.length; j++) {
                    int nbFiles = nbFilesArray[j];
                    System.out.println("Current files number: " + nf.format(nbFiles));
                    DataBench dataWrite = new DataBench();
                    dataWrite.setNbFiles(nbFiles);
                    hashFile = new MultiPointersMultiHashFiles(filePath, nbFiles);
                    try {
                        launchWriteBenchs(nbItems, nbFiles, dataWrite);
                        dataWrite.setWriteEndedWithSuccess(true);
                    } catch (Throwable e) {
                        e.printStackTrace();
                        dataWrite.setWriteError(e.getMessage() + ":" + e.getStackTrace()[0]);
                        writeData(dataWrite);
                        continue;
                    }

                    for (int pointersByFileIdx = 0; pointersByFileIdx < pointersByFileArray.length; pointersByFileIdx++) {
                        DataBench dataReadWrite = (DataBench) dataWrite.clone();
                        int pointersByFile = pointersByFileArray[pointersByFileIdx];
                        System.out.println("Current pointersByFile number: " + nf.format(pointersByFile));
                        try {
                            launchReadBenchs(nbItems, nbFiles, pointersByFile, dataReadWrite);
                            dataReadWrite.setReadEndedWithSuccess(true);
                        } catch (Throwable e) {
                            e.printStackTrace();
                            dataReadWrite.setReadError(e.getMessage() + ":" + e.getStackTrace()[0]);
                            continue;
                        } finally {
                            writeData(dataReadWrite);
                        }
                    }

                    hashMap = null;

                    // System.out.println("countReturnFalse1=" + Bean.countReturnFalse1);
                    // System.out.println("waiting for garbage collector...");
                    // Sizeof.runGC();
                    // long heap2 = Sizeof.usedMemory(); // Take a before heap snapshot
                    //
                    // final int size = Math.round(((float) (heap2 - heap1)) / loop);
                    // System.out.println("'before' heap: " + heap1 + " bytes, 'after' heap: " + heap2 + " bytes "); //
                    // not
                    // needed
                    // // here
                    // System.out.println("heap delta: " + (heap2 - heap1) + " bytes ");
                    // System.out.println("size by item: " + size + " bytes ");
                    // System.out.println("Number of loops: " + loop);
                    // System.out.println("Number of items: " + hashMap.size());
                    // System.out.println("Read randomized: " + randomRead);
                    // System.out.println("Hash class used: " + hashFile.getClass().getName());
                    // if (loop != hashMap.size()) {
                    // System.out.println("WARNING: loops number is different of items number !");
                    // }
                    // System.out.println("Time: " + ((time2 - time1) / 1000) + " s");

                }

            }
        } finally {
            closeDataFile();
        }

    }

    /**
     * DOC amaumont Comment method "openDataFile".
     * 
     * @throws IOException
     */
    private void openDataFile() throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH'h'mm'm'ss's'");

        String filePath = folderStatsPath
        // + sdf.format(new Date())
                + "_" + fileHashBenchsBaseName + ".csv";
        
        File file = new File(filePath);
        boolean exists = file.exists();
        
        try {
            this.fileData = new FileOutputStream(filePath, true);
            if(!exists) {
                this.fileData.write(DataBench.getFileHeader().getBytes());
                this.fileData.write('\n');
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * DOC amaumont Comment method "closeDataFile".
     */
    private void closeDataFile() {
        try {
            this.fileData.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * DOC amaumont Comment method "writeInFile".
     * 
     * @param dataReadWrite
     * @throws IOException
     */
    private void writeData(DataBench dataReadWrite) throws IOException {
        this.fileData.write(dataReadWrite.toFileRow().getBytes());
        this.fileData.write('\n');
    }

    private void launchWriteBenchs(int nbItems, int nbFiles, DataBench dataWrite) throws Exception, IOException,
            ClassNotFoundException {

        System.out.println("Write step");

        Sizeof.runGC();
        long heap1 = Sizeof.usedMemory(); // Take a before heap snapshot

        long start = System.currentTimeMillis();
        dataWrite.setStartWriteDate(new Date(start));

        // time1 = System.currentTimeMillis();

        TObjectHashingStrategy objectHashingStrategy = new TObjectHashingStrategy() {

            /*
             * (non-Javadoc)
             * 
             * @see gnu.trove.TObjectHashingStrategy#computeHashCode(java.lang.Object)
             */
            public int computeHashCode(Object o) {
                return o == null ? 0 : o.hashCode();
            }

            /*
             * (non-Javadoc)
             * 
             * @see gnu.trove.TObjectHashingStrategy#equals(java.lang.Object, java.lang.Object)
             */
            public boolean equals(Object o1, Object o2) {
                return o2 == null ? o1 == null : o2.equals(o1);
            }

        };

        // ################################################################################
        // Do not compare bytes number in this class with bytes number MemoryHashMapTest,
        // the purpose of this test class is not to measure heap memory !
        // ################################################################################

        // Map hashMap = new HashMap();
        // Map hashMap = new HashMap(loop, 1f);
        // Map hashMap = new HashMap(10000, 1f);
        // Map hashMap = new THashMap(objectHashingStrategy);
        // Map hashMap = new THashMap(loop, 0.1f, objectHashingStrategy);
        // Map hashMap = new THashMap(loop, 1f, objectHashingStrategy);
        // Map hashMap = new THashMap(loop + 10000, 1.0f, objectHashingStrategy);
        // Map hashMap = new THashMap(loop + 10000, 0.99f, objectHashingStrategy);
        // Map hashMap = new THashMap(10000, 1.0f, objectHashingStrategy);

        // Data in Memory, 119 bytes , all=34 s
        // Data in Sqlite DB, ?? bytes , all=?? s, write=2550 s (42 min), read=???? s (?? min)
        // Data in File, ?? bytes , all=?? s, write=?? s (?? min), read=???? s (?? min)

        dataWrite.setNbItems(nbItems);
        dataWrite.setNbFiles(nbFiles);

        hashMap = new THashMap(nbItems, 1.0f, objectHashingStrategy); // ??
        // Map hashMap = new THashMap(loop + (int)((float)loop * 0.1f), 0.1f, objectHashingStrategy);

        dataWrite.setInitialCapacityMap(nbItems);
        dataWrite.setLoadFactorMap(1.0f);

        THashMap localHashMap = hashMap;

        hashFile.readonly = readonly;

        try {
            hashFile.initPut(filePath);
            for (int i = 0; i < nbItems; i++) {
                // => bean from tFileInput for example...
                Bean bean = new Bean(i, String.valueOf(i));

                KeyForMap keyForMap = null;
                // => THash storing step
                if (readAfterStore) {
                    keyForMap = new KeyForMap((int) hashFile.put("buffer", bean), bean.hashCode());
                } else {
                    keyForMap = new KeyForMap(i, bean.hashCode());
                }
                localHashMap.put(keyForMap, keyForMap);

                if (i % 100000 == 0) {
                    System.out.println("Writing " + i);
                }

            }

            dataWrite.setWriteEndedWithSuccess(true);

        } finally {
            hashFile.endPut();
        }

        Sizeof.runGC();
        long heap2 = Sizeof.usedMemory(); // Take a before heap snapshot

        long memoryHeap = heap2 - heap1;

        dataWrite.setHeapMemoryWrite(memoryHeap);

        final int bytesPerItems = Math.round(((float) memoryHeap) / nbItems);
        System.out.println("'before' heap: " + heap1 + " bytes, 'after' heap: " + heap2 + " bytes "); //

        dataWrite.setBytesPerItemWrite(bytesPerItems);

        long end = System.currentTimeMillis();

        dataWrite.setEndWriteDate(new Date(end));

        long deltaTime = (end - start);

        dataWrite.setTimeWrite(deltaTime);

        int itemsPerSec = (int) ((float) nbItems / (float) deltaTime * 1000f);

        dataWrite.setItemsPerSecWrite(itemsPerSec);

        System.out.println(deltaTime + " milliseconds for " + nbItems + " objects to STORE. " + itemsPerSec
                + " items/s ");

        dataWrite.setTotalFilesSize(hashFile.getTotalSize());
    }

    /**
     * DOC amaumont Comment method "launchBenchs".
     * 
     * @param pointersByFile
     * @param nbFiles
     * @param dataReadWrite
     * 
     * @throws Exception
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void launchReadBenchs(int nbItems, int nbFiles, int pointersByFile, DataBench dataReadWrite)
            throws Exception, IOException, ClassNotFoundException {

        System.out.println("Starting with " + nbItems + " items ");

        Sizeof.runGC();
        long heap1 = Sizeof.usedMemory(); // Take a before heap snapshot

        long start = System.currentTimeMillis();
        dataReadWrite.setStartReadDate(new Date(start));

        Random rand = new Random(System.currentTimeMillis());

        hashFile.setFilePointersNumber(pointersByFile);

        dataReadWrite.setPointersByFile(pointersByFile);

        THashMap localHashMap = hashMap;

        if (readAfterStore) {
            System.out.println("Read step");
            long lastTime = start;
            long timeOut = 300000;
            try {
                hashFile.initGet(filePath);
                for (int i = 0; i < nbItems; i++) {
//                    System.out.println(i);
                    
                    Bean bean = null;
                    // => bean from main flow in tMap for example...
                    if (randomRead) {
                        int v = rand.nextInt(nbItems);
                        bean = new Bean(v, String.valueOf(v));
                    } else {
                        bean = new Bean(i, String.valueOf(i));
                    }

                    // => search properties of bean in lookup for example...
                    KeyForMap keyForMap = (KeyForMap) localHashMap.get(bean);

                    // validity test
                    if (keyForMap == null) {
                        throw new RuntimeException("keyForMap not found with id " + i);
                    }

                    // => bean found from DB
                    Bean beanFromDB = (Bean) hashFile.get("buffer", keyForMap.cursorPosition, keyForMap.hashcode);

                    // validity test
                    if (beanFromDB == null) {
                        throw new RuntimeException("Bean not found with cursorPosition " + keyForMap.cursorPosition);
                    }
                    // validity test
                    if (!beanFromDB.name.equals(bean.name) || beanFromDB.primitiveInt != bean.primitiveInt) {
                        throw new RuntimeException("Values of beans are different with cursorPosition "
                                + keyForMap.cursorPosition);
                    }

                    if(System.currentTimeMillis() - lastTime > timeOut) {
                        dataReadWrite.setTimeRead((int)(((double)(System.currentTimeMillis() - lastTime)* nbItems) / (double)i));
                        throw new RuntimeException("Timeout, read is too long !");
                    }
                    
                    if ((i + 1) % 100000 == 0) {
                        long currentTimeMillis = System.currentTimeMillis();
                        System.out.println("Reading " + i + ", time since last display: "
                                + (int) ((float) (currentTimeMillis - lastTime)) + " ms");
                        lastTime = currentTimeMillis;
                    }

                }
                dataReadWrite.setReadEndedWithSuccess(true);

            } catch (FileNotFoundException e) {
                throw e;
            } finally {
                hashFile.endGet(filePath);
            }
            long end = System.currentTimeMillis();

            Sizeof.runGC();
            long heap2 = Sizeof.usedMemory(); // Take a before heap snapshot

            long memoryHeap = heap2 - heap1;

            dataReadWrite.setHeapMemoryRead(memoryHeap);

            final int bytesPerItems = Math.round(((float) (heap2 - heap1)) / nbItems);
            System.out.println("'before' heap: " + heap1 + " bytes, 'after' heap: " + heap2 + " bytes "); //

            dataReadWrite.setBytesPerItemRead(bytesPerItems);

            dataReadWrite.setEndReadDate(new Date(end));

            long deltaTime = (end - start);

            dataReadWrite.setTimeRead(deltaTime);

            int itemsPerSec = (int) ((float) nbItems / (float) deltaTime * 1000f);
            
            dataReadWrite.setItemsPerSecRead(itemsPerSec);
            
            System.out.println(deltaTime + " milliseconds for " + nbItems + " objects to READ. "
                    + itemsPerSec + "  items/s ");

        }

        time2 = System.currentTimeMillis();

    }

}
