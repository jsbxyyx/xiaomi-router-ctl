package org.xxz.ip.util;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.AmazonRoute53ClientBuilder;
import com.amazonaws.services.route53.model.Change;
import com.amazonaws.services.route53.model.ChangeAction;
import com.amazonaws.services.route53.model.ChangeBatch;
import com.amazonaws.services.route53.model.ChangeInfo;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsRequest;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsResult;
import com.amazonaws.services.route53.model.HostedZone;
import com.amazonaws.services.route53.model.ListHostedZonesResult;
import com.amazonaws.services.route53.model.ResourceRecord;
import com.amazonaws.services.route53.model.ResourceRecordSet;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AwsUtil {

    private static final Logger log = LoggerFactory.getLogger(AwsUtil.class);

    private static final AwsUtil INSTANCE = new AwsUtil();
    private final AmazonRoute53 client;
    private final JsonObject config;

    private AwsUtil() {
        config = IOUtil.readConfig();
        String ak = config.get("aws-ak").getAsString();
        String sk = config.get("aws-sk").getAsString();
        String region = config.get("aws-region").getAsString();
        client = AmazonRoute53ClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(ak, sk)))
                .build();
    }

    public static AwsUtil getInstance() {
        return INSTANCE;
    }


    public boolean updateDomainRecord(String ipaddr) {
        String domain = config.get("aws-domain").getAsString();
        ListHostedZonesResult listHostedZones = client.listHostedZones();
        List<HostedZone> hostedZones = listHostedZones.getHostedZones();
        String zoneid = null;
        for (HostedZone hostedZone : hostedZones) {
            if (hostedZone.getName().indexOf(domain) > -1) {
                zoneid = hostedZone.getId();
                break;
            }
        }
        if (zoneid == null) {
            throw new RuntimeException("zoneid not found");
        }

        ResourceRecord record = new ResourceRecord()
                .withValue(ipaddr);

        String sub = config.get("aws-rrkeyword").getAsString();
        String type = config.get("aws-rrtype").getAsString();

        ResourceRecordSet recordSet = new ResourceRecordSet()
                .withName(sub + "." + domain)
                .withType(type)
                .withTTL(300L)
                .withResourceRecords(record);

        Change change = new Change()
                .withAction(ChangeAction.UPSERT)
                .withResourceRecordSet(recordSet);

        ChangeBatch batch = new ChangeBatch()
                .withChanges(change);

        ChangeResourceRecordSetsRequest request = new ChangeResourceRecordSetsRequest()
                .withChangeBatch(batch)
                .withHostedZoneId(zoneid);

        try {
            ChangeResourceRecordSetsResult result = client.changeResourceRecordSets(request);
            ChangeInfo changeInfo = result.getChangeInfo();
            log.info("changeInfo={}", changeInfo);
        } catch (Exception e) {
            log.error("aws changeResourceRecordSets error.", e);
        }
        return true;
    }

}
