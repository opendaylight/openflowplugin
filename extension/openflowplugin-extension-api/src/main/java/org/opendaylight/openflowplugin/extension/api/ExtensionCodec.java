package org.opendaylight.openflowplugin.extension.api;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.ExtensionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugNxMatchNodeTableFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugNxMatchNodeTableFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugNxMatchNotifPacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugNxMatchNotifPacketInBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugNxMatchRpcAddFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralExtensionListGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.VendorXxx1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.VendorXxx1Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.VendorXxx2;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.VendorXxx2Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.VendorXxxGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.ExtensionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.list.grouping.ExtensionList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.list.grouping.ExtensionListBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yangtools.yang.binding.Augmentation;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

/**
 *  initial proposal
 *  TODO: move to util (depends on openflowplugin-extension-api)
 */
public class ExtensionCodec {

    private static class EquivalencyGroup<GROUPPING, T extends Augmentable<T>> {

        Class<GROUPPING> commonInterface;
        Set<Class<? extends Augmentation<T>>> classes;
        
        /**
         * @param commonInterface 
         * @param common grouping Interface
         */
        public EquivalencyGroup(Class<GROUPPING> commonInterface) {
            this.commonInterface = commonInterface;
            classes = new HashSet<>();
        }

        <X extends Augmentation<T>> void add(Class<X> cls) {
            Preconditions.checkArgument(commonInterface.isAssignableFrom(cls), "oh man! I got "+cls);
            classes.add(cls);
        }

        @SuppressWarnings("unchecked")
        Optional<GROUPPING> getExtension(T data) {

            for (Class<? extends Augmentation<T>> cls : classes) {
                Augmentation<T> potential = data.getAugmentation(cls);
                if (potential != null) {
                    return Optional.of((GROUPPING) potential);
                }
            }

            return Optional.absent();
        }
    }

    
    private static class EquivalencyLooseGroup<GROUPPING> {

        Class<GROUPPING> commonInterface;
        Set<Class<? extends Augmentation<?>>> classes;
        
        /**
         * @param commonInterface 
         * @param common grouping Interface
         */
        public EquivalencyLooseGroup(Class<GROUPPING> commonInterface) {
            this.commonInterface = commonInterface;
            classes = new HashSet<>();
        }

        void add(Class<? extends Augmentation<?>> cls) {
            Preconditions.checkArgument(commonInterface.isAssignableFrom(cls), "oh man! I got "+cls);
            classes.add(cls);
        }

        @SuppressWarnings("unchecked")
        <T extends Augmentable<T>> Optional<GROUPPING> getExtension(T data) {

            for (Class<? extends Augmentation<?>> cls : classes) {
                Augmentation<T> potential = data.getAugmentation((Class<Augmentation<T>>) cls);
                if (potential != null) {
                    return Optional.of((GROUPPING) potential);
                }
            }

            return Optional.absent();
        }
    }

    
    public static void main(String[] args) {
        {
            // VENDOR
            EquivalencyGroup<VendorXxxGrouping, Extension> eqGroup = new EquivalencyGroup<>(VendorXxxGrouping.class);
            eqGroup.add(VendorXxx1.class);
            eqGroup.add(VendorXxx2.class);

            ExtensionBuilder eb1 = new ExtensionBuilder();
            VendorXxx1 vendorxxx1 = new VendorXxx1Builder().setDosEkis("one beer").build();
            Extension ext1 = eb1.addAugmentation(VendorXxx1.class, vendorxxx1).build();

            ExtensionBuilder eb2 = new ExtensionBuilder();
            VendorXxx2 vendorxxx2 = new VendorXxx2Builder().setDosEkis("three beers").build();
            Extension ext2 = eb2.addAugmentation(VendorXxx2.class, vendorxxx2).build();

            System.out.println(eqGroup.getExtension(ext1).get().getDosEkis());
            System.out.println(eqGroup.getExtension(ext2).get().getDosEkis());
        }
        {
            // OFP - easy case
            EquivalencyGroup<GeneralExtensionListGrouping, Match> eqGroup = new EquivalencyGroup<>(GeneralExtensionListGrouping.class);
            eqGroup.add(GeneralAugNxMatchNodeTableFlow.class);
            eqGroup.add(GeneralAugNxMatchRpcAddFlow.class);
            
            MatchBuilder mb1 = new MatchBuilder();
            ExtensionList extension1 = new ExtensionListBuilder().setType(JoachimToth.class).build();
            GeneralAugNxMatchNodeTableFlow odlxxx1 = new GeneralAugNxMatchNodeTableFlowBuilder().setExtensionList(Collections.singletonList(extension1)).build();
            Match match1 = mb1.addAugmentation(GeneralAugNxMatchNodeTableFlow.class, odlxxx1).build();
            
            MatchBuilder mb2 = new MatchBuilder();
            ExtensionList extension2 = new ExtensionListBuilder().setType(JoachimZapletal.class).build();
            GeneralAugNxMatchNodeTableFlow odlxxx2 = new GeneralAugNxMatchNodeTableFlowBuilder().setExtensionList(Collections.singletonList(extension2)).build();
            Match match2 = mb2.addAugmentation(GeneralAugNxMatchNodeTableFlow.class, odlxxx2).build();
            
            System.out.println(eqGroup.getExtension(match1).get().getExtensionList().get(0).getType());
            System.out.println(eqGroup.getExtension(match2).get().getExtensionList().get(0).getType());
        }
        
        {
            // OFP - ugly case
            EquivalencyLooseGroup<GeneralExtensionListGrouping> eqGroup = new EquivalencyLooseGroup<>(GeneralExtensionListGrouping.class);
            eqGroup.add(GeneralAugNxMatchNodeTableFlow.class);
            eqGroup.add(GeneralAugNxMatchNotifPacketIn.class);
            
            MatchBuilder mb1 = new MatchBuilder();
            ExtensionList extension1 = new ExtensionListBuilder().setType(JoachimToth.class).build();
            GeneralAugNxMatchNodeTableFlow odlxxx1 = new GeneralAugNxMatchNodeTableFlowBuilder().setExtensionList(Collections.singletonList(extension1)).build();
            Match match1 = mb1.addAugmentation(GeneralAugNxMatchNodeTableFlow.class, odlxxx1).build();
            
            org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.packet.received.MatchBuilder mb2 = new org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.packet.received.MatchBuilder();
            ExtensionList extension2 = new ExtensionListBuilder().setType(JoachimZapletal.class).build();
            GeneralAugNxMatchNotifPacketIn odlxxx2 = new GeneralAugNxMatchNotifPacketInBuilder().setExtensionList(Collections.singletonList(extension2)).build();
            org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.packet.received.Match match2 = mb2.addAugmentation(GeneralAugNxMatchNotifPacketIn.class, odlxxx2).build();
            
            System.out.println(eqGroup.getExtension(match1).get().getExtensionList().get(0).getType());
            System.out.println(eqGroup.getExtension(match2).get().getExtensionList().get(0).getType());
        }
    }
    
    private static class JoachimToth extends ExtensionKey {
        // nobody
    }
    
    private static class JoachimZapletal extends ExtensionKey {
        // nobody
    }


}
