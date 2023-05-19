package com.alibaba.nacos.config.server.service.dump.disk;

import com.alibaba.nacos.common.utils.MD5Utils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.config.server.utils.LogUtil;
import com.alibaba.nacos.sys.env.EnvUtil;
import org.rocksdb.BlockBasedTableConfig;
import org.rocksdb.ColumnFamilyOptions;
import org.rocksdb.DBOptions;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.alibaba.nacos.config.server.constant.Constants.ENCODE_UTF8;

/**
 * zunfei.lzf
 */
public class ConfigRocksDbDiskService implements ConfigDiskService {
    
    private static final String ROCKSDB_DATA = File.separator + "rocksdata" + File.separator;
    
    private static final String BASE_DIR = ROCKSDB_DATA + "config-data";
    
    private static final String BETA_DIR = ROCKSDB_DATA + "beta-data";
    
    private static final String TAG_DIR = ROCKSDB_DATA + "tag-data";
    
    private static final String BATCH_DIR = ROCKSDB_DATA + "batch-data";
    
    private static final long DEFAULT_WRITE_BUFFER_MB = 32;
    
    Map<String, RocksDB> rocksDBMap = new HashMap<>();
    
    private void createDirIfNotExist(String dir) {
        File roskDataDir = new File(EnvUtil.getNacosHome(), "rocksdata");
        if (!roskDataDir.exists()) {
            roskDataDir.mkdir();
        }
        File baseDir = new File(EnvUtil.getNacosHome(), dir);
        if (!baseDir.exists()) {
            baseDir.mkdir();
        }
    }
    
    private void deleteDirIfExist(String dir) {
        File rockskDataDir = new File(EnvUtil.getNacosHome(), "rocksdata");
        if (!rockskDataDir.exists()) {
            return;
        }
        File baseDir = new File(EnvUtil.getNacosHome(), dir);
        if (baseDir.exists()) {
            baseDir.delete();
        }
    }
    
    public ConfigRocksDbDiskService() {
        createDirIfNotExist(BASE_DIR);
        createDirIfNotExist(BETA_DIR);
        createDirIfNotExist(TAG_DIR);
        createDirIfNotExist(BATCH_DIR);
    }
    
    private byte[] getKeyByte(String dataId, String group, String tenant, String tag) throws IOException {
        String[] keys = new String[] {dataId, group, tenant, tag};
        StringBuilder stringBuilder = new StringBuilder();
        for (String key : keys) {
            if (StringUtils.isBlank(key)) {
                key = "";
            }
            urlEncode(key, stringBuilder);
            stringBuilder.append("+");
        }
        return stringBuilder.toString().getBytes(ENCODE_UTF8);
    }
    
    /**
     * + -> %2B % -> %25.
     */
    private static void urlEncode(String str, StringBuilder sb) {
        for (int idx = 0; idx < str.length(); ++idx) {
            char c = str.charAt(idx);
            if ('+' == c) {
                sb.append("%2B");
            } else if ('%' == c) {
                sb.append("%25");
            } else {
                sb.append(c);
            }
        }
    }
    
    /**
     * 保存配置信息到磁盘
     */
    public void saveToDiskInner(String type, String dataId, String group, String tenant, String tag, String content)
            throws IOException {
        try {
            initAndGetDB(type).put(getKeyByte(dataId, group, tenant, tag), content.getBytes(ENCODE_UTF8));
        } catch (RocksDBException e) {
            throw new IOException(e);
        }
    }
    
    /**
     * 保存配置信息到磁盘
     */
    public void saveToDiskInner(String type, String dataId, String group, String tenant, String content)
            throws IOException {
        saveToDiskInner(type, dataId, group, tenant, null, content);
    }
    
    /**
     * Save configuration information to disk.
     */
    public void saveToDisk(String dataId, String group, String tenant, String content) throws IOException {
        saveToDiskInner(BASE_DIR, dataId, group, tenant, content);
    }
    
    /**
     * Save beta information to disk.
     */
    public void saveBetaToDisk(String dataId, String group, String tenant, String content) throws IOException {
        saveToDiskInner(BETA_DIR, dataId, group, tenant, content);
        
    }
    
    /**
     * 保存配置信息到磁盘
     */
    public void saveBatchToDisk(String dataId, String group, String tenant, String content) throws IOException {
        saveToDiskInner(BATCH_DIR, dataId, group, tenant, content);
        
    }
    
    /**
     * Save tag information to disk.
     */
    public void saveTagToDisk(String dataId, String group, String tenant, String tag, String content)
            throws IOException {
        saveToDiskInner(TAG_DIR, dataId, group, tenant, tag, content);
        
    }
    
    /**
     * Deletes configuration files on disk.
     */
    public void removeConfigInfo(String dataId, String group, String tenant) {
        removeContentInner(BASE_DIR, dataId, group, tenant, null);
    }
    
    /**
     * Deletes beta configuration files on disk.
     */
    public void removeConfigInfo4Beta(String dataId, String group, String tenant) {
        removeContentInner(BETA_DIR, dataId, group, tenant, null);
    }
    
    /**
     * 删除磁盘上的配置文件
     */
    public void removeConfigInfo4Batch(String dataId, String group, String tenant) {
        removeContentInner(BATCH_DIR, dataId, group, tenant, null);
    }
    
    /**
     * Deletes tag configuration files on disk.
     */
    public void removeConfigInfo4Tag(String dataId, String group, String tenant, String tag) {
        removeContentInner(TAG_DIR, dataId, group, tenant, tag);
        
    }
    
    private String byte2String(byte[] bytes) throws IOException {
        if (bytes == null) {
            return null;
        }
        return new String(bytes, ENCODE_UTF8);
    }
    
    RocksDB initAndGetDB(String dir) throws IOException, RocksDBException {
        if (rocksDBMap.containsKey(dir)) {
            return rocksDBMap.get(dir);
        } else {
            synchronized (this) {
                if (rocksDBMap.containsKey(dir)) {
                    return rocksDBMap.get(dir);
                }
                createDirIfEmpty(EnvUtil.getNacosHome() + dir);
                rocksDBMap.put(dir, RocksDB.open(createOptions(dir), EnvUtil.getNacosHome() + dir));
                return rocksDBMap.get(dir);
            }
            
        }
    }
    
    private void createDirIfEmpty(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }
    
    private String getContentInner(String type, String dataId, String group, String tenant) throws IOException {
        byte[] bytes = null;
        try {
            bytes = initAndGetDB(type).get(getKeyByte(dataId, group, tenant, null));
            String string = byte2String(bytes);
            return string;
        } catch (RocksDBException e) {
            throw new IOException(e);
        }
    }
    
    private String getTagContentInner(String type, String dataId, String group, String tenant, String tag)
            throws IOException {
        byte[] bytes = null;
        try {
            bytes = initAndGetDB(type).get(getKeyByte(dataId, group, tenant, tag));
            return byte2String(bytes);
        } catch (RocksDBException e) {
            throw new IOException(e);
        }
    }
    
    private void removeContentInner(String type, String dataId, String group, String tenant, String tag) {
        try {
            initAndGetDB(type).delete(getKeyByte(dataId, group, tenant, tag));
        } catch (Exception e) {
            LogUtil.DEFAULT_LOG
                    .warn("Remove dir=[{}] config fail,dataId={},group={},tenant={},error={}", type, dataId, group,
                            tenant, e.getCause());
        }
    }
    
    /**
     * Returns the path of cache file in server.
     */
    public String getBetaContent(String dataId, String group, String tenant) throws IOException {
        return getContentInner(BETA_DIR, dataId, group, tenant);
    }
    
    /**
     * Returns the path of the tag cache file in server.
     */
    public String getTagContent(String dataId, String group, String tenant, String tag) throws IOException {
        return getTagContentInner(TAG_DIR, dataId, group, tenant, tag);
    }
    
    public String getContent(String dataId, String group, String tenant) throws IOException {
        return getContentInner(BASE_DIR, dataId, group, tenant);
    }
    
    public String getLocalConfigMd5(String dataId, String group, String tenant, String encode) throws IOException {
        return MD5Utils.md5Hex(getContentInner(BASE_DIR, dataId, group, tenant), encode);
    }
    
    Options createOptions(String dir) {
        DBOptions dbOptions = new DBOptions();
        dbOptions.setMaxBackgroundJobs(Runtime.getRuntime().availableProcessors());
        Options options = new Options(dbOptions, createColumnFamilyOptions(dir));
        options.setCreateIfMissing(true);
        return options;
    }
    
    ColumnFamilyOptions createColumnFamilyOptions(String dir) {
        ColumnFamilyOptions columnFamilyOptions = new ColumnFamilyOptions();
        BlockBasedTableConfig tableFormatConfig = new BlockBasedTableConfig();
        columnFamilyOptions.setTableFormatConfig(tableFormatConfig);
        //set more write buffer size to formal config-data, reduce flush to sst file frequency.
        columnFamilyOptions.setWriteBufferSize(getSuitFormalCacheSizeMB(dir) * 1024 * 1024);
        //once a stt file is flushed, compact it immediately to avoid too many sst file which will result in read latency.
        columnFamilyOptions.setLevel0FileNumCompactionTrigger(1);
        return columnFamilyOptions;
    }
    
    /**
     * get suit formal buffer size.
     *
     * @return
     */
    private long getSuitFormalCacheSizeMB(String dir) {
        
        boolean formal = BASE_DIR.equals(dir);
        long maxHeapSizeMB = Runtime.getRuntime().maxMemory() / 1024 / 1024;
        
        if (formal) {
            long formalWritebufferSizeMB = 0;
            
            if (maxHeapSizeMB < 8 * 1024) {
                formalWritebufferSizeMB = 32;
            } else if (maxHeapSizeMB < 16 * 1024) {
                formalWritebufferSizeMB = 64;
            } else {
                formalWritebufferSizeMB = 256;
            }
            LogUtil.DEFAULT_LOG.info("init formal rocksdb write buffer size {}M for dir {}, maxHeapSize={}M",
                    formalWritebufferSizeMB, dir, maxHeapSizeMB);
            return formalWritebufferSizeMB;
        } else {
            LogUtil.DEFAULT_LOG.info("init default rocksdb write buffer size {}M for dir {}, maxHeapSize={}M",
                    DEFAULT_WRITE_BUFFER_MB, dir, maxHeapSizeMB);
            return DEFAULT_WRITE_BUFFER_MB;
        }
        
    }
    
    /**
     * Clear all config file.
     */
    public void clearAll() {
        try {
            if (rocksDBMap.containsKey(BASE_DIR)) {
                rocksDBMap.get(BASE_DIR).close();
                RocksDB.destroyDB(EnvUtil.getNacosHome() + BASE_DIR, new Options());
            }
            deleteDirIfExist(BASE_DIR);
            LogUtil.DEFAULT_LOG.info("clear all config-info success.");
        } catch (RocksDBException e) {
            LogUtil.DEFAULT_LOG.warn("clear all config-info failed.", e);
        }
    }
    
    /**
     * Clear all beta config file.
     */
    public void clearAllBeta() {
        try {
            if (rocksDBMap.containsKey(BETA_DIR)) {
                rocksDBMap.get(BETA_DIR).close();
                RocksDB.destroyDB(EnvUtil.getNacosHome() + BETA_DIR, new Options());
            }
            deleteDirIfExist(BETA_DIR);
            LogUtil.DEFAULT_LOG.info("clear all config-info-beta success.");
        } catch (RocksDBException e) {
            LogUtil.DEFAULT_LOG.warn("clear all config-info-beta failed.", e);
        }
    }
    
    /**
     * Clear all tag config file.
     */
    public void clearAllTag() {
        
        try {
            if (rocksDBMap.containsKey(TAG_DIR)) {
                rocksDBMap.get(TAG_DIR).close();
                RocksDB.destroyDB(EnvUtil.getNacosHome() + TAG_DIR, new Options());
            }
            deleteDirIfExist(TAG_DIR);
            LogUtil.DEFAULT_LOG.info("clear all config-info-tag success.");
        } catch (RocksDBException e) {
            LogUtil.DEFAULT_LOG.warn("clear all config-info-tag failed.", e);
        }
    }
    
    
    public void clearAllBatch() {
        try {
            if (rocksDBMap.containsKey(BATCH_DIR)) {
                rocksDBMap.get(BATCH_DIR).close();
                RocksDB.destroyDB(EnvUtil.getNacosHome() + BATCH_DIR, new Options());
            }
            deleteDirIfExist(BATCH_DIR);
            LogUtil.DEFAULT_LOG.info("clear all config-info-batch success.");
        } catch (RocksDBException e) {
            LogUtil.DEFAULT_LOG.warn("clear all config-info-batch failed.", e);
        }
    }
    
    public String getBatchContent(String dataId, String group, String tenant) throws IOException {
        return getContentInner(BATCH_DIR, dataId, group, tenant);
    }
}
