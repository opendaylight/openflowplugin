package org.opendaylight.openflowplugin.openflow.md.core.translator;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.opendaylight.openflowplugin.openflow.md.core.IMDMessageTranslator;
import org.opendaylight.openflowplugin.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.GroupStatsResponseConvertor;
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev131103.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GroupDescStatsUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GroupFeaturesUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GroupStatisticsUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupFeatures.Types;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyGroupDesc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyGroupFeatures;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class converts multipart reply messages to the notification objects defined
 * by statistics provider (manager ).
 * 
 * @author avishnoi@in.ibm.com
 *
 */
public class MultipartReplyTranslator implements IMDMessageTranslator<OfHeader,  List<DataObject>> {

    protected static final Logger logger = LoggerFactory
            .getLogger(MultipartReplyTranslator.class);
    private static GroupStatsResponseConvertor convertor = new GroupStatsResponseConvertor();

    @Override
    public  List<DataObject> translate(SwitchConnectionDistinguisher cookie, SessionContext sc, OfHeader msg) {
        
        if(msg instanceof MultipartReplyMessage){
            MultipartReplyMessage mpReply = (MultipartReplyMessage)msg;
            List<DataObject> listDataObject = new CopyOnWriteArrayList<DataObject>();
            NodeId node = this.nodeIdFromDatapathId(sc.getFeatures().getDatapathId());
            switch (mpReply.getType()){
            case OFPMPGROUP:{
                logger.info("Received group statistics multipart reponse");
                GroupStatisticsUpdatedBuilder message = new GroupStatisticsUpdatedBuilder();
                message.setId(node);
                message.setMoreReplies(mpReply.getFlags().isOFPMPFREQMORE());
                message.setTransactionId(generateTransactionId(mpReply.getXid()));
                MultipartReplyGroup replyBody = (MultipartReplyGroup)mpReply.getMultipartReplyBody();
                message.setGroupStats(convertor.toSALGroupStatsList(replyBody.getGroupStats()));
                logger.debug("Converted group statistics : {}",message.toString());
                listDataObject.add(message.build());
                return listDataObject;
            }
            case OFPMPGROUPDESC:{
                logger.info("Received group description statistics multipart reponse");
                
                GroupDescStatsUpdatedBuilder message = new GroupDescStatsUpdatedBuilder();
                message.setId(node);
                message.setMoreReplies(mpReply.getFlags().isOFPMPFREQMORE());
                message.setTransactionId(generateTransactionId(mpReply.getXid()));
                MultipartReplyGroupDesc replyBody = (MultipartReplyGroupDesc)mpReply.getMultipartReplyBody();
                message.setGroupDescStats(convertor.toSALGroupDescStatsList(replyBody.getGroupDesc()));
                logger.debug("Converted group statistics : {}",message.toString());
                listDataObject.add(message.build());
                return listDataObject;
            }
            case OFPMPGROUPFEATURES: {
                logger.info("Received group features multipart reponse");
                GroupFeaturesUpdatedBuilder message = new GroupFeaturesUpdatedBuilder();
                message.setId(node);
                message.setMoreReplies(mpReply.getFlags().isOFPMPFREQMORE());
                message.setTransactionId(generateTransactionId(mpReply.getXid()));
                MultipartReplyGroupFeatures replyBody = (MultipartReplyGroupFeatures)mpReply.getMultipartReplyBody();
                message.setTypes(new Types(replyBody.getTypes().isOFPGTALL(),
                                            replyBody.getTypes().isOFPGTSELECT(),
                                            replyBody.getTypes().isOFPGTINDIRECT(),
                                            replyBody.getTypes().isOFPGTSELECT()));
                
                listDataObject.add(message.build());
                return listDataObject;
            }
            case OFPMPMETER:
                logger.info("Received meter statistics multipart reponse");
                 break;
            case OFPMPMETERCONFIG:
                logger.info("Received meter config statistics multipart reponse");
                break;
            case OFPMPMETERFEATURES:
                logger.info("Received meter features multipart reponse");
                break;
            default:
                logger.info(" Type : {}, not handled yet",mpReply.getType().name() );
                break;
            }
        }
        return null;
    }
    
    private NodeId nodeIdFromDatapathId(BigInteger datapathId) {
        String current = datapathId.toString();
        return new NodeId("openflow:" + current);
    }
    
    private TransactionId generateTransactionId(Long xid){
        String stringXid =xid.toString();
        BigInteger bigIntXid = new BigInteger( stringXid );
        return new TransactionId(bigIntXid);

    }


}
