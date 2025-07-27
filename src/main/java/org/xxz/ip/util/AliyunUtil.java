package org.xxz.ip.util;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.alidns.model.v20150109.DescribeDomainRecordsRequest;
import com.aliyuncs.alidns.model.v20150109.DescribeDomainRecordsResponse;
import com.aliyuncs.alidns.model.v20150109.UpdateDomainRecordRequest;
import com.aliyuncs.alidns.model.v20150109.UpdateDomainRecordResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

/**
 * @author tt
 */
public class AliyunUtil {

    private static final Logger log = LoggerFactory.getLogger(AliyunUtil.class);

    private static final AliyunUtil INSTANCE = new AliyunUtil();
    private final IAcsClient client;
    private final JsonObject config;

    private final Gson Json = new GsonBuilder().disableHtmlEscaping().create();

    private AliyunUtil() {
        config = IOUtil.readConfig();
        String ak = config.get("aliyun-ak").getAsString();
        String sk = config.get("aliyun-sk").getAsString();
        // 设置鉴权参数，初始化客户端
        DefaultProfile profile = DefaultProfile.getProfile(
                "cn-qingdao",// 地域ID
                ak,// 您的AccessKey ID
                sk);// 您的AccessKey Secret
        client = new DefaultAcsClient(profile);
    }

    public static AliyunUtil getInstance() {
        return INSTANCE;
    }

    public boolean updateDomainRecord(String ipaddr) {
        try {
            DescribeDomainRecordsRequest ddrr = new DescribeDomainRecordsRequest();
            String domain = config.get("aliyun-domain").getAsString();
            String rrKeyWord = config.get("aliyun-rrKeyWord").getAsString();
            String rrType = config.get("aliyun-rrType").getAsString();
            ddrr.setDomainName(domain);
            ddrr.setRRKeyWord(rrKeyWord);
            ddrr.setType(rrType);
            DescribeDomainRecordsResponse ddrresp = client.getAcsResponse(ddrr);
            List<DescribeDomainRecordsResponse.Record> domainRecords = ddrresp.getDomainRecords();
            if (domainRecords.size() != 0) {
                DescribeDomainRecordsResponse.Record record = domainRecords.get(0);
                // 记录ID
                String recordId = record.getRecordId();
                // 记录值
                String recordsValue = record.getValue();
                if (!Objects.equals(recordsValue, ipaddr)) {
                    log.info("oldIP=" + recordsValue + ",newIP=" + ipaddr);
                    // 调用SDK发送请求
                    UpdateDomainRecordRequest udrr = new UpdateDomainRecordRequest();
                    // 主机记录
                    udrr.setRR(rrKeyWord);
                    udrr.setRecordId(recordId);
                    udrr.setValue(ipaddr);
                    udrr.setType(rrType);
                    UpdateDomainRecordResponse response = client.getAcsResponse(udrr);
                    log.info("response={}",Json.toJson(response));
                    return true;
                }
            }
            return false;
        } catch (ClientException e) {
            log.error("updateDomainRecord error. [{}]", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

}
